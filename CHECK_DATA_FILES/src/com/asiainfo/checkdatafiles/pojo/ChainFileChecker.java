package com.asiainfo.checkdatafiles.pojo;

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
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.asiainfo.checkdatafiles.util.FileCharsetDetector;
import com.asiainfo.checkdatafiles.util.LineNumberConfigReader;
import com.google.gson.JsonSyntaxException;

/**
 * Class Name: ChainFileChecker.java Description:
 * 
 * @author Stefan_xiepj DateTime 2017��4��17�� ����4:49:41
 * @company asiainfo
 * @email xiepj@asiainfo.com.cn
 * @version 1.0
 */
public class ChainFileChecker {

	private  volatile static ChainFileChecker instance = null;

	private Integer THREAD_NUM;
	private String SRC_FILE_PATH;
	private String ERROR_LOG_PATH;
	private String ERROR_COLUMNS_TITLE;
	private Map<String, String> ERROR_CODE_MAP;
	private static Map<String, FilePojo> FILE_POJO_MAP;
	private Integer ERROR_THRESHOLD;

	public void setTHREAD_NUM(Integer tHREAD_NUM) {
		THREAD_NUM = tHREAD_NUM;
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

	public void setERROR_THRESHOLD(Integer eRROR_THRESHOLD) {
		ERROR_THRESHOLD = eRROR_THRESHOLD;
	}

	// �޲ι��췽��
	public ChainFileChecker() {
	}

	// �βϹ��췽��
	public ChainFileChecker(String initPath) {
		FileInputStream configIn = null;
		try {
			configIn = new FileInputStream(initPath);
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

	// ˫������ģʽ
	public static  ChainFileChecker getInstance() {
		if (instance == null) {
			synchronized (ChainFileChecker.class) {
				if (instance == null) {
					instance = new ChainFileChecker();
				}
			}
		}
		return instance;
	}

	// ��У���ļ���
	String fileName;
	// �ļ��ӿ�
	private FilePojo filePojo;
	// ��ǰУ���ļ�
	//private File checkingFile;
	// �ֶ�ʵ��
	private FieldPojo[] fieldPojos;
	// ���������
	private int errorCount;
	// �ļ�����ϴ�ʱ��
	private long lastModified;
	// ��ǰϵͳ�ں���
	private int core;
	private boolean statusError;

	private Logger logger = Logger.getLogger(ChainFileChecker.class);

	// ��ʼ����̬�����
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

	// ִ��У��
	public void executeCheck(File file) throws Exception {

		long startTimeMillis = System.currentTimeMillis();

		//��ʼ������
		this.fileName = null;
		this.filePojo = null;
		this.fieldPojos = null;
		this.errorCount = 0;
		this.lastModified = 0L;
		this.core = 0;
		this.statusError = false;
		
		File checkingFile = null;
		// ��һ�������˵������ϴ���У���Լ�����У����ļ�
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
		
		this.fileName = file.getName();
		
		// �ڶ������ļ�У��״̬�޸�
		this.lastModified = file.lastModified();
		checkingFile = new File(file.getAbsolutePath() + ".checking");
		if(checkingFile.exists()){
			checkingFile.delete();
			checkingFile.createNewFile();
		}
		
		file.renameTo(checkingFile);

		// ����������ȡ�ӿڳ���ģ��ӳ����,���ݽӿ����ƻ�ýӿڳ���ģ��
		this.filePojo = fileToFilePojoMapping(file);

		if (filePojo == null) {
			return;
		}

		this.fieldPojos = filePojo.getFields();

		// ���Ĳ��� ��ȡ�ļ�У��ȼ�����У��
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
			// ������
		}
		if ("COUNT_FIELD".equals(checkLevel)) {
			// ������
		}
		if ("FIELD".equals(checkLevel)) {
			// ������
		}

		// ���岽��У����ϣ������ļ�״̬
		//String checkedName = SRC_FILE_PATH + fileName + ".checked";

		while(true){
			boolean renameTo = checkingFile.renameTo(new File(checkingFile.getAbsolutePath().replaceAll("checking", "checked")));
			
			if (renameTo) {
				System.out.println("�������ɹ�");
				break;
			} else {
				System.out.println("������ʧ��,�ٴγ���");
				Thread.sleep(3000);
			}
			
		}


		long endTimeMillis = System.currentTimeMillis();
		System.out.println(fileName + " У������ʱ��Ϊ��" + (endTimeMillis - startTimeMillis));
	}

	// ��ȡ�ļ��ӿ�����
	private FilePojo fileToFilePojoMapping(File file) {
		String fileName = file.getName();
		String interfaceName = fileName.substring(20, 28);

		return FILE_POJO_MAP.get(interfaceName);
	}

	// У��
	private void checker(FilePojo filePojo, File checkingFile) {
		// ������Ϣ
		String errorMsg = "";
		// �������
		String checkOutFlag = "";

		ChildThreadChecker[] childThreadChecker = null;

		try {

			String fileName = checkingFile.getName().substring(0, checkingFile.getName().lastIndexOf("."));
			// �ָ��
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();
			// ��ȡ�ϴ�ʱ��
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(lastModified);
			String uploadtime = DateFormat.getTimeInstance().format(calendar.getTime());

			// У���ļ���
			checkOutFlag = BaseUtil.isLegalFileName(filePojo, fileName);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// У���ļ��ӳ��ϴ�
			checkOutFlag = BaseUtil.isUploadTooLate(filePojo, uploadtime);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// ����У��
			FileCharsetDetector fileCharsetDetector = new FileCharsetDetector();
			String encoding = fileCharsetDetector.guessFileEncoding(checkingFile, 2);

			System.out.println("encoding:" + encoding);
			checkOutFlag = BaseUtil.isLegalEncoding(filePojo, encoding);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
				// return;

			}

			Reader in = new FileReader(checkingFile);
			LineNumberConfigReader reader = new LineNumberConfigReader(in);

			// ������
			Integer row_count = BaseUtil.totalLines(checkingFile.getAbsolutePath());
			System.out.println("row_count:"+row_count);
			// ����ֵ
			String readAppointedLineNumber = BaseUtil.readAppointedLineNumber(reader, 0);
			Integer topRowValue = Integer.parseInt(readAppointedLineNumber);
			// �ڶ���ֵ
			String secondRowValue = BaseUtil.readAppointedLineNumber(reader, 1);
			
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
			
			// �ļ���¼����У��
			checkOutFlag = BaseUtil.isRowsEqual(topRowValue, row_count);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "1" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// �ļ�������У��
			checkOutFlag = BaseUtil.isLegalHederLine(filePojo, secondRowValue);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "2" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCounter();
			}

			// ���ݼ�У��
			Integer startRowNumber;
			core = Runtime.getRuntime().availableProcessors();
			if(row_count < 10000){
				THREAD_NUM = 1;
			}else if(0 == THREAD_NUM || "".equals(THREAD_NUM) || THREAD_NUM == null){
				THREAD_NUM = core;
			}
			
			CountDownLatch start = new CountDownLatch(1);
			CountDownLatch end = new CountDownLatch(THREAD_NUM);
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM + 1, 10, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(10));

			System.out.println("THREAD_NUM:" + THREAD_NUM);
			int blockSize = row_count / THREAD_NUM;
			childThreadChecker = new ChildThreadChecker[THREAD_NUM];

			for (int i = 0; i < THREAD_NUM; i++) {
				System.out.println("�߳�" + i + "׼������");

				startRowNumber = 3 + i * blockSize;
				System.out.println("startRowNumber:" + startRowNumber);
				if (i == (THREAD_NUM - 1)) {
					blockSize = row_count - startRowNumber + 1;
				}
				ChildThreadChecker checker = new ChildThreadChecker(this,checkingFile, i, startRowNumber, blockSize, start, end);
				childThreadChecker[i] = checker;
				threadPoolExecutor.submit(checker);
			}

			start.countDown();
			System.out.println("����У���߳̿�ʼУ��");
			end.await();
			System.out.println("����У���߳����У��");
			threadPoolExecutor.shutdown();

		} catch (Exception e) {
			logger.error(Level.ERROR, e);
			this.statusError = true;
		} finally {
			// �ϲ���־�ļ�
			tmpLogFileToTargetLogFile(childThreadChecker, errorMsg);
		}

	}

	// �ϲ���־�ļ�
	private void tmpLogFileToTargetLogFile(ChildThreadChecker[] childThreadChecker, String errorMsg) {

		try {
			if (errorCount > 0) {
				BufferedOutputStream outputStream = null;
				File errorFile = new File(ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.error");
				if (errorFile.exists()) {
					errorFile.delete();
					errorFile.createNewFile();
				}
				if (!statusError) {
					outputStream = new BufferedOutputStream(new FileOutputStream(errorFile));
					// д������
					outputStream.write(String.valueOf(errorCount).getBytes());
					outputStream.write(13);
					outputStream.write((ERROR_COLUMNS_TITLE + filePojo.getColumnsTitle()).getBytes());
					outputStream.write(13);
					outputStream.write(errorMsg.getBytes());
					// outputStream.write(13);
				}

				// �����������̴߳�������ʱ�ļ�����˳�����������д��Ŀ���ļ���
				for (int i = 0; i < THREAD_NUM; i++) {
					if (statusError) {
						for (int k = 0; k < THREAD_NUM; k++) {
							childThreadChecker[k].tmpLogFile.delete();
						}
						logger.log(Level.INFO, fileName + "----У���쳣��");
						break;
					}

					BufferedInputStream inputStream = new BufferedInputStream(
							new FileInputStream(childThreadChecker[i].tmpLogFile));
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
					// ɾ����ʱ�ļ�
					if (childThreadChecker[i].status == ChildThreadChecker.STATUS_HAS_FINISHED) {
						childThreadChecker[i].tmpLogFile.delete();
					}
				}

				outputStream.flush();
				IOUtils.closeQuietly(outputStream);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class ChildThreadChecker extends Thread {
		// У��״̬��
		public static final int STATUS_HASNOT_FINISHED = 0;
		public static final int STATUS_HAS_FINISHED = 1;
		public static final int STATUS_HAS_EXCEPTION = 2;
		private ChainFileChecker task;
		private File checkingFile;
		private int id;
		private Integer startRowNumber;
		private Integer blockSize;
		private CountDownLatch start;
		private CountDownLatch end;

		private File tmpLogFile;
		private FileWriter writer;
		// �߳�״̬��
		private int status = ChildThreadChecker.STATUS_HASNOT_FINISHED;

		public ChildThreadChecker() {
			super();
		}

		public ChildThreadChecker(ChainFileChecker chainFileChecker,File checkingFile, int id, Integer startRowNumber, Integer blockSize,
				CountDownLatch start, CountDownLatch end) {
			super();
			this.task = chainFileChecker;
			this.checkingFile = checkingFile;
			this.id = id;
			this.startRowNumber = startRowNumber;
			this.blockSize = blockSize;
			this.start = start;
			this.end = end;

			try {
				tmpLogFile = new File(ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.log" + id);
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
			// ������Ϣ
			String errorMsg = "";
			// �������
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
				// ���ݼ�У��
				for (int i = 0; i < blockSize; i++) {
					rowValue = randomAccessFile.readLine();
					fieldsArray = rowValue.split("\\|#\\|");
					if (column_Count == fieldsArray.length) {
						for (int j = 0; j < column_Count; j++) {
							fieldPojo = fieldPojos[j];
							fieldValue = fieldsArray[j];
							// �ǿ�У��
							checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
										+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
										+ columnsTitleSplit + fieldValue + "\n");
								errorCounter();
								continue;
							}
							// �ֶγ���У��
							checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
										+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
										+ columnsTitleSplit + fieldValue + "\n");
								errorCounter();
								continue;
							}

							// ���ָ�ʽУ��
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

							// ���ڸ�ʽУ��
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

							// �����ʽУ��
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

							// �ֻ������ʽУ��
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
						// ����У��
						checkOutFlag = "CHK010";
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							errorMsg += (fileName + columnsTitleSplit + (i + startRowNumber) + columnsTitleSplit
									+ checkOutFlag + columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag)
									+ columnsTitleSplit + "\n");
							errorCounter();
						}
						continue;
					} else {
						// ���ݼ��ֶ�������ƥ��
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
						// ��д��־�����errorMsg
						writeLogMsg(writer, errorMsg);
						errorMsg = "";
					}

					if (errorCount > ERROR_THRESHOLD) {
						break;
					}

				}

				// ��־���
				writeLogMsg(writer, errorMsg);
				
				if (errorCount > 0) {
					status = ChildThreadChecker.STATUS_HAS_FINISHED;
				}
				end.countDown();
			} catch (Exception e1) {
				e1.printStackTrace();
				this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
				this.task.statusError = false;
			} finally {
				try {
					writer.flush();
					IOUtils.closeQuietly(writer);
					IOUtils.closeQuietly(randomAccessFile);
					System.out.println("writer �ѹر�");
				} catch (IOException e) {
					e.printStackTrace();
					this.status = ChildThreadChecker.STATUS_HAS_EXCEPTION;
					this.task.statusError = false;
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

	// ���������
	public synchronized int errorCounter() {
		return ChainFileChecker.this.errorCount++;
	}

	@Override
	public String toString() {
		return "ChainFileChecker [SRC_FILE_PATH=" + SRC_FILE_PATH + ", ERROR_LOG_PATH=" + ERROR_LOG_PATH
				+ ", ERROR_COLUMNS_TITLE=" + ERROR_COLUMNS_TITLE + ", ERROR_CODE_MAP=" + ERROR_CODE_MAP
				+ ", ERROR_THRESHOLD=" + ERROR_THRESHOLD + "]";
	}

}