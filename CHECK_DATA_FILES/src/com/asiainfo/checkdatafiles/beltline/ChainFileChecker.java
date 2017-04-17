package com.asiainfo.checkdatafiles.beltline;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.flame.check.FileCharsetDetector;
import com.alibaba.fastjson.JSON;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.test.NextMultiThreadDownLoad.ChildThread;
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.asiainfo.checkdatafiles.util.LineNumberConfigReader;
import com.google.gson.JsonSyntaxException;

/**
 * Class Name: ChainFileChecker.java Description:
 * 
 * @author Stefan_xiepj DateTime 2017年4月17日 下午4:49:41
 * @company asiainfo
 * @email xiepj@asiainfo.com.cn
 * @version 1.0
 */
public class ChainFileChecker {

	private static volatile ChainFileChecker instance = null;

	private static String SRC_FILE_PATH;
	private static String ERROR_LOG_PATH;
	private static String ERROR_COLUMNS_TITLE;
	private static Map<String, String> ERROR_CODE_MAP;
	private static Map<String, FilePojo> FILE_POJO_MAP;

	//双锁单例模式
	private ChainFileChecker() {
	}

	public static ChainFileChecker getInstance() {
		if (instance == null) {
			synchronized (ChainFileChecker.class) {
				if (instance == null) {
					instance = new ChainFileChecker();
				}
			}
		}
		return instance;
	}
	
	//待校验文件名
	String fileName;
	//文件接口
	private FilePojo filePojo;
	//当前校验文件
	private File checkingFile;
	//字段实体
	private FieldPojo[] fieldPojos;
	//错误计数器
	private int errorCount = 0;
	//文件最后上传时间
	private long lastModified;
	//当前系统内核数
	private int core;
	private boolean statusError;

	private Logger logger = Logger.getLogger(ChainFileChecker.class);

	// 初始化静态代码块
	static {
		FileInputStream configIn = null;
		try {
			configIn = new FileInputStream("conf\\__init__.json");
			byte[] buf = new byte[1024];
			String initConfig = "";
			int length = 0;
			while ((length = configIn.read(buf)) != -1) {
				initConfig += new String(buf, 0, length);

			}

			instance = JSON.parseObject(initConfig, ChainFileChecker.class);

			FILE_POJO_MAP = new HashMap<String, FilePojo>();
			List<FilePojo> filePojoList = FilePojo.getInstance();
			for (FilePojo filePojo : filePojoList) {
				FILE_POJO_MAP.put(filePojo.getInterfaceName(), filePojo);
			}

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(configIn);
		}
	}

	public static void main(String[] args) throws Exception {

		// 获取校验路径下所有文件集,并校验
		File files = new File(SRC_FILE_PATH);
		File[] listFiles = files.listFiles();

		for (int i = 0; i < listFiles.length; i++) {
			ChainFileChecker.getInstance().execute(listFiles[i]);
		}
		
		//Thread.sleep(1000*60*5);
	}

	// 执行校验
	public void execute(File file) throws Exception {

		long startTimeMillis = System.currentTimeMillis();

		this.fileName = file.getName();
		// 第一步，过滤掉正在上传已校验以及正在校验的文件
		if (!file.exists() || !file.isFile()) {
			logger.info(file.getName() + " doesn't exist or is not a file");
			return;
		}
		if (file.getName().endsWith(".ing")) {
			return;
		}
		if (file.getName().endsWith(".checking")) {
			return;
		}
		if (file.getName().endsWith(".checked")) {
			return;
		}

		// 第二步，文件校验状态修改
		this.lastModified = file.lastModified();
		this.checkingFile = new File(file.getAbsolutePath() + ".checking");
		file.renameTo(checkingFile);

		// 第三步，获取接口抽象模板映射类,根据接口名称获得接口抽象模板
		this.filePojo = fileToFilePojoMapping(file);

		if (filePojo == null) {
			return;
		}

		this.fieldPojos = filePojo.getFields();

		// 第四步， 获取文件校验等级，并校验
		String checkLevel = filePojo.getCheckLevel();

		if ("NAME_ENCODING_COUNT_FIELD".equals(checkLevel)) {
			checker(filePojo, checkingFile);
			if (statusError) {
				logger.info(fileName + " is a Legal File!!!");
			} else {
				logger.info(fileName + " is a Illegal File!!!");
			}
		}
		if ("ENCODING_COUNT_FIELD".equals(checkLevel)) {
			// 待开发
		}
		if ("COUNT_FIELD".equals(checkLevel)) {
			// 待开发
		}
		if ("FIELD".equals(checkLevel)) {
			// 待开发
		}

		// 第五步，校验完毕，更改文件状态
		checkingFile.renameTo(new File(checkingFile.getAbsolutePath().replaceAll("checking", "checked")));
		long endTimeMillis = System.currentTimeMillis();
		System.out.println(fileName + " 校验所用时长为：" + (endTimeMillis - startTimeMillis));
	}

	// 获取文件接口类型
	private FilePojo fileToFilePojoMapping(File file) {
		String fileName = file.getName();
		String interfaceName = fileName.substring(20, 28);

		return FILE_POJO_MAP.get(interfaceName);
	}

	// 校验
	private void checker(FilePojo filePojo, File checkingFile) {
		// 错误信息
		String errorMsg = "";
		// 错误数量
		int errorCount = 0;
		// 错误编码
		String checkOutFlag = "";

		ChildThreadChecker[] childThreadChecker;

		try {

			String fileName = checkingFile.getName().substring(0, checkingFile.getName().lastIndexOf("."));
			// 分割符
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();
			// 获取上传时间
			Calendar cd = Calendar.getInstance();
			cd.setTimeInMillis(lastModified);
			String uploadtime = DateFormat.getTimeInstance().format(cd.getTime());

			// 校验文件名
			checkOutFlag = BaseUtil.isLegalFileName(filePojo, fileName);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// 校验文件延迟上传
			checkOutFlag = BaseUtil.isUploadTooLate(filePojo, uploadtime);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// 编码校验
			FileCharsetDetector fileCharsetDetector = new FileCharsetDetector();
			String encoding = fileCharsetDetector.guessFileEncoding(checkingFile);
			System.out.println("encoding:" + encoding);
			checkOutFlag = BaseUtil.isLegalEncoding(filePojo, encoding);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
				this.statusError = false;
				return;

			}

			Reader in = new FileReader(checkingFile);
			LineNumberConfigReader reader = new LineNumberConfigReader(in);

			// 总行数
			Integer row_count = BaseUtil.totalLines(checkingFile.getAbsolutePath());
			// 首行值
			String readAppointedLineNumber = BaseUtil.readAppointedLineNumber(reader, 0);
			Integer topRowValue = Integer.parseInt(readAppointedLineNumber);
			// 第二行值
			String secondRowValue = BaseUtil.readAppointedLineNumber(reader, 1);

			// 文件记录行数校验
			checkOutFlag = BaseUtil.isRowsEqual(topRowValue, row_count);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "1" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// 文件标题行校验
			checkOutFlag = BaseUtil.isLegalHederLine(filePojo, secondRowValue);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "2" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// 数据集校验
			Integer startRowNumber;
			core = Runtime.getRuntime().availableProcessors();
			CountDownLatch start = new CountDownLatch(1);
			CountDownLatch end = new CountDownLatch(core);
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(core, core + 1, 10, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(10));

			System.out.println("core:" + core);
			int blockSize = row_count / core;
			childThreadChecker = new ChildThreadChecker[core];

			for (int i = 0; i < core; i++) {
				System.out.println("线程" + i + "准备就绪");

				startRowNumber = 2 + i * blockSize;
				System.out.println("startRowNumber:" + startRowNumber);
				if (i == (core - 1)) {
					blockSize = row_count - startRowNumber + 1;
				}
				ChildThreadChecker checker = new ChildThreadChecker(i, startRowNumber, blockSize, start, end);
				childThreadChecker[i] = checker;
				threadPoolExecutor.submit(checker);
			}

			start.countDown();
			System.out.println("所有校验线程开始校验");
			end.await();
			System.out.println("所有校验线程完成校验");
			threadPoolExecutor.shutdown();
			//合并日志文件
			
			tmpLogFileToTargetLogFile(childThreadChecker);

			if (errorCount != 0) {
				this.statusError = false;
			} else {
				this.statusError = true;
			}

		} catch (Exception e) {
			logger.error(Level.ERROR, e);
			this.statusError = false;
			return;
		} finally {
			// 合并日志文件
			if (errorCount != 0) {
				FileWriter fw;
				try {
					fw = new FileWriter(errorFile);
					fw.write(String.valueOf(errorCount) + "\n");
					fw.write(ERROR_COLUMNS_TITLE + filePojo.getColumnsTitle() + "\n");
					fw.write(errorMsg);
					fw.flush();
					fw.close();
				} catch (IOException e) {
					logger.error(Level.ERROR, e);
				}
			} else {
			}

		}

	}
	
	//合并日志文件
	private void tmpLogFileToTargetLogFile(ChildThreadChecker[] childThreadChecker){
		  
        try { 
        	if(errorCount > 0){
        		BufferedOutputStream outputStream = new BufferedOutputStream(  
                        new FileOutputStream(ChainFileChecker.ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.error")); 
                //写入首行
                outputStream.write(String.valueOf(errorCount).getBytes());
                // 遍历所有子线程创建的临时文件，按顺序把下载内容写入目标文件中  
                for (int i = 0; i < core; i++) { 
                    if (statusError) {
                        for (int k = 0; k < core; k++) {
                            if (childThreadChecker[k].tmpLogFile.length() == 0)  
                            	childThreadChecker[k].tmpLogFile.delete();  
                        }
                        logger.log(Level.INFO, fileName+"校验成功。");
                        break;  
                    }
          
                    BufferedInputStream inputStream = new BufferedInputStream(  
                            new FileInputStream(childThreadChecker[i].tmpLogFile));  
                    logger.log(Level.INFO, "Now is file " + childThreadChecker[i].id);
                    int len = 0;  
                    long count = 0;  
                    byte[] b = new byte[1024];  
                    while ((len = inputStream.read(b)) != -1) {
                        count += len;  
                        outputStream.write(b, 0, len);  
                        if ((count % 4096) == 0) {  
                            outputStream.flush();  
                        }  
          
                        // b = new byte[1024];  
                    }  
          
                    inputStream.close();  
                    // 删除临时文件  
                    if (childThreadChecker[i].status == ChildThread.STATUS_HAS_FINISHED) {  
                    	childThreadChecker[i].tmpLogFile.delete();  
                    }  
                }  
          
                outputStream.flush();  
                outputStream.close(); 
        	}
        	
             
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
		
	}

	class ChildThreadChecker extends Thread {
		// 校验状态码
		public static final int STATUS_HASNOT_FINISHED = 0;
		public static final int STATUS_HAS_FINISHED = 1;
		public static final int STATUS_HTTPSTATUS_ERROR = 2;
		private File tmpLogFile;
		private int id;
		private Integer startRowNumber;
		private Integer blockSize;
		private CountDownLatch start;
		private CountDownLatch end;

		private FileWriter writer;
		// 线程状态码
		private int status = ChildThread.STATUS_HASNOT_FINISHED;

		public ChildThreadChecker() {
			super();
		}

		public ChildThreadChecker(int id, Integer startRowNumber, Integer blockSize, CountDownLatch start,
				CountDownLatch end) {
			super();
			this.id = id;
			this.startRowNumber = startRowNumber;
			this.blockSize = blockSize;
			this.start = start;
			this.end = end;

			try {
				tmpLogFile = new File(ChainFileChecker.ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.log" + id);
				if (tmpLogFile.exists()) {
					tmpLogFile.delete();
					tmpLogFile.createNewFile();
				}
				this.writer = new FileWriter(tmpLogFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			// 错误信息
			String errorMsg = "";
			// 错误编码
			String checkOutFlag = "";
			String rowValue;
			String fieldValue;
			FieldPojo fieldPojo;

			try {
				start.await();

				Integer column_Count = filePojo.getColumnsTitle().split("\\|#\\|").length;
				String columnsTitleSplit = filePojo.getColumnsTitleSplit();

				long readChars = BaseUtil.getFileAppointLinePointer(checkingFile.getAbsolutePath(), startRowNumber);

				@SuppressWarnings("resource")
				RandomAccessFile randomAccessFile = new RandomAccessFile(checkingFile, "rw");
				randomAccessFile.seek(readChars);
				String[] fieldsArray;
				// 数据集校验
				for (int i = 0; i < blockSize; i++) {
					rowValue = randomAccessFile.readLine();
					fieldsArray = rowValue.split("\\|#\\|");
					if (column_Count == fieldsArray.length) {
						for (int j = 0; j < column_Count; j++) {
							fieldPojo = fieldPojos[j];
							fieldValue = fieldsArray[j];
							// 非空校验
							checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
										+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
										+ columnsTitleSplit + fieldValue + "\n");
								errorCounter();
								continue;
							}
							// 字段长度校验
							checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
										+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
										+ columnsTitleSplit + fieldValue + "\n");
								errorCounter();
								continue;
							}

							// 数字格式校验
							if ("Number".equals(fieldPojo.getType())) {
								checkOutFlag = BaseUtil.isNumber(fieldValue);
								if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
									errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
											+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
											+ columnsTitleSplit + fieldValue + "\n");
									errorCounter();
								}
								continue;
							}

							// 日期格式校验
							if ("Date".equals(fieldPojo.getType())) {
								checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
								if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
									errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
											+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
											+ columnsTitleSplit + fieldValue + "\n");
									errorCounter();
								}
								continue;
							}

							// 邮箱格式校验
							if ("Email".equals(fieldPojo.getType())) {
								checkOutFlag = BaseUtil.isEmail(fieldValue);
								if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
									errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
											+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
											+ columnsTitleSplit + fieldValue + "\n");
									errorCounter();
								}
								continue;
							}

							// 手机号码格式校验
							if ("Telephone".equals(fieldPojo.getType())) {
								checkOutFlag = BaseUtil.isTelephoneNumber(fieldValue);
								if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
									errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
											+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
											+ columnsTitleSplit + fieldValue + "\n");
									errorCounter();
								}
								continue;
							}

						}

					} else if (1 == fieldsArray.length) {
						// 空行校验
						checkOutFlag = "CHK010";
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
									+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
									+ columnsTitleSplit + "\n");
							errorCounter();
						}
						continue;
					} else {
						// 数据集字段数量不匹配
						checkOutFlag = "CHK007";
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
									+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
									+ columnsTitleSplit + "\n");
							errorCounter();
						}
						continue;
					}

					{
						// 书写日志，清空errorMsg
						writeLogMsg(writer, errorMsg);
						errorMsg = "";
					}

					if (errorCount > 10000) {
						break;
					}

				}

				// 日志输出
				System.out.println(this.getName() + "的错误信息数量为：" + errorCount);

				writeLogMsg(writer, errorMsg);
				if (errorCount > 0) {
					status = ChildThread.STATUS_HASNOT_FINISHED;
				}
				end.countDown();
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if (writer != null) {
					try {
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}

	}

	public void writeLogMsg(FileWriter writer, String errorMsg) {
		try {

			writer.write(errorMsg);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	//错误计数器
	public synchronized int errorCounter() {

		return ChainFileChecker.this.errorCount++;
	}

	public void setSRC_FILE_PATH(String sRC_FILE_PATH) {
		SRC_FILE_PATH = sRC_FILE_PATH;
	}

	public void setERROR_LOG_PATH(String eRROR_LOG_PATH) {
		ERROR_LOG_PATH = eRROR_LOG_PATH;
	}

	public void setERROR_COLUMNS_TITLE(String eRROR_COLUMNS_TITLE) {
		ERROR_COLUMNS_TITLE = eRROR_COLUMNS_TITLE;
	}

	public void setERROR_CODE_MAP(Map<String, String> eRROR_CODE_MAP) {
		ERROR_CODE_MAP = eRROR_CODE_MAP;
	}

	public static void setFilePojoMap(Map<String, FilePojo> fILE_POJO_MAP) {
		ChainFileChecker.FILE_POJO_MAP = fILE_POJO_MAP;
	}

	public static void set_instance(ChainFileChecker _instance) {
		ChainFileChecker.instance = _instance;
	}



}