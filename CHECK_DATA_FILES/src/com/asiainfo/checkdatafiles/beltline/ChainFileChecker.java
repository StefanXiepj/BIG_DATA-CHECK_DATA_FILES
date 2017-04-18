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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.test.NextMultiThreadDownLoad.ChildThread;
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.asiainfo.checkdatafiles.util.FileCharsetDetector;
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
	private static String ERROR_THRESHOLD;

	// 双锁单例模式
	private ChainFileChecker() {
	}

	public static ChainFileChecker getInstance() {
		if (instance == null) {
			synchronized (ChainFileChecker.class) {
				if (instance == null) {
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
			}
		}
		return instance;
	}

	// 待校验文件名
	String fileName;
	// 文件接口
	private FilePojo filePojo;
	// 当前校验文件
	private File checkingFile;
	// 字段实体
	private FieldPojo[] fieldPojos;
	// 错误计数器
	private int errorCount = 0;
	// 文件最后上传时间
	private long lastModified;
	// 当前系统内核数
	private int core;
	private boolean statusError = false;

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
				logger.info(fileName + ": Legal File!!!");
			} else {
				logger.info(fileName + ": Illegal File!!!");
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
		String checkedName = SRC_FILE_PATH + fileName + ".checked";

		/*File target = new File(checkedName);
		if (target.exists()) { //新文件若存在，则删掉
		  target.delete();
		}
		boolean result = checkingFile.renameTo(target); //将旧文件更名
		if(result) {
			System.out.println(fileName +" -> "+ checkedName);
		} else { //更名失败，则采取变相的更名方法
			try{
				FileUtils.copyFile(file, target); // 将旧文件拷贝给新文件
				System.out.println(fileName +" -> "+ checkedName);
				//清空旧文件
				emptyFileContent(file);
			} catch (IOException e) {
				System.out.println("Failed to rename ["+fileName+"] to ["+checkedName+"].");
			}
		}*/
		
		boolean renameTo = checkingFile.renameTo(file);
		
		if(renameTo){System.out.println("重命名成功");}else{System.out.println("重命名失败");}
		
		long endTimeMillis = System.currentTimeMillis();
		System.out.println("ERROR_THRESHOLD:"+ERROR_THRESHOLD);
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
		// 错误编码
		String checkOutFlag = "";

		ChildThreadChecker[] childThreadChecker = null;

		try {

			String fileName = checkingFile.getName().substring(0, checkingFile.getName().lastIndexOf("."));
			// 分割符
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();
			// 获取上传时间
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(lastModified);
			String uploadtime = DateFormat.getTimeInstance().format(calendar.getTime());

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
			String encoding = fileCharsetDetector.guessFileEncoding(checkingFile, 2);

			System.out.println("encoding:" + encoding);
			checkOutFlag = BaseUtil.isLegalEncoding(filePojo, encoding);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
				//return;

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

				startRowNumber = 3 + i * blockSize;
				System.out.println("startRowNumber:" + startRowNumber);
				if (i == (core - 1)) {
					blockSize = row_count - startRowNumber + 1;
				}
				ChildThreadChecker checker = new ChildThreadChecker(this, i, startRowNumber, blockSize, start, end);
				childThreadChecker[i] = checker;
				threadPoolExecutor.submit(checker);
			}

			start.countDown();
			System.out.println("所有校验线程开始校验");
			end.await();
			System.out.println("所有校验线程完成校验");
			threadPoolExecutor.shutdown();

		} catch (Exception e) {
			logger.error(Level.ERROR, e);
			this.statusError = true;
		} finally {
			// 合并日志文件
			tmpLogFileToTargetLogFile(childThreadChecker, errorMsg);
		}

	}

	// 合并日志文件
	private void tmpLogFileToTargetLogFile(ChildThreadChecker[] childThreadChecker, String errorMsg) {

		try {
			if (errorCount > 0) {
				BufferedOutputStream outputStream = null;
				File errorFile = new File(ChainFileChecker.ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.error");
				if(errorFile.exists()){
					errorFile.delete();
					errorFile.createNewFile();
				}
				if (!statusError) {
					outputStream = new BufferedOutputStream(new FileOutputStream(errorFile));
					// 写入首行
					outputStream.write(String.valueOf(errorCount).getBytes());
					outputStream.write(13);
					outputStream.write((ERROR_COLUMNS_TITLE + filePojo.getColumnsTitle()).getBytes());
					outputStream.write(13);
					outputStream.write(errorMsg.getBytes());
					//outputStream.write(13);
				}

				// 遍历所有子线程创建的临时文件，按顺序把下载内容写入目标文件中
				for (int i = 0; i < core; i++) {
					if (statusError) {
						for (int k = 0; k < core; k++) {
							childThreadChecker[k].tmpLogFile.delete();
						}
						logger.log(Level.INFO, fileName + "----校验异常。");
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
					if (childThreadChecker[i].status == ChildThreadChecker.STATUS_HAS_FINISHED) {
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
		public static final int STATUS_HAS_EXCEPTION = 2;
		private ChainFileChecker task;
		private File tmpLogFile;
		private int id;
		private Integer startRowNumber;
		private Integer blockSize;
		private CountDownLatch start;
		private CountDownLatch end;

		private FileWriter writer;
		// 线程状态码
		private int status = ChildThreadChecker.STATUS_HASNOT_FINISHED;

		public ChildThreadChecker() {
			super();
		}

		public ChildThreadChecker(ChainFileChecker chainFileChecker, int id, Integer startRowNumber, Integer blockSize,
				CountDownLatch start, CountDownLatch end) {
			super();
			this.task = chainFileChecker;
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
				e.printStackTrace();
				this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
				this.task.statusError = false;
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
			RandomAccessFile randomAccessFile = null;

			try {
				start.await();

				Integer column_Count = filePojo.getColumnsTitle().split("\\|#\\|").length;
				String columnsTitleSplit = filePojo.getColumnsTitleSplit();

				long readChars = BaseUtil.getFileAppointLinePointer(checkingFile.getAbsolutePath(), startRowNumber);

				randomAccessFile = new RandomAccessFile(checkingFile, "rw");
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
					status = ChildThread.STATUS_HAS_ERROR;
				}
				end.countDown();
			} catch (Exception e1) {
				e1.printStackTrace();
				this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
				this.task.statusError = false;
			} finally {
				if (writer != null) {
					try {
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
						this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
						this.task.statusError = false;
					}

				}
				
				if( randomAccessFile !=null){
					try {
						randomAccessFile.close();
					} catch (IOException e) {
						e.printStackTrace();
						this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
						this.task.statusError = false;
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

	// 错误计数器
	public synchronized int errorCounter() {

		return ChainFileChecker.this.errorCount++;
	}
	
	/** 清空文件内容 */
	public static void emptyFileContent(File file) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(new byte[0]);
		} catch (Exception e) {
			System.out.println("Can't not empty " + file.getName());
		} finally {
			IOUtils.closeQuietly(out);
		}
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
	
	public static void setERROR_THRESHOLD(String eRROR_THRESHOLD) {
		ERROR_THRESHOLD = eRROR_THRESHOLD;
	}

	public static String getSRC_FILE_PATH() {
		return SRC_FILE_PATH;
	}

	public static String getERROR_LOG_PATH() {
		return ERROR_LOG_PATH;
	}

	public static String getERROR_COLUMNS_TITLE() {
		return ERROR_COLUMNS_TITLE;
	}

	public static Map<String, String> getERROR_CODE_MAP() {
		return ERROR_CODE_MAP;
	}

	public static String getERROR_THRESHOLD() {
		return ERROR_THRESHOLD;
	}
	
	

}