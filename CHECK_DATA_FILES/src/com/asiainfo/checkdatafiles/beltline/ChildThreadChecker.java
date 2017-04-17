package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.test.NextMultiThreadDownLoad.ChildThread;
import com.asiainfo.checkdatafiles.util.BaseUtil;

public class ChildThreadChecker extends Thread {
	// У��״̬��
	public static final int STATUS_HASNOT_FINISHED = 0;
	public static final int STATUS_HAS_FINISHED = 1;
	public static final int STATUS_HTTPSTATUS_ERROR = 2;
	private File checkingFile;
	private Map<String, String> ERROR_CODE_MAP;
	private FilePojo filePojo;
	private FieldPojo[] fields;
	private String fileName;
	private File logFile;
	private String[] fieldsArray;
	private String fieldValue;
	private Integer startRowNumber;
	private Integer blockSize;
	private CountDownLatch start;
	private CountDownLatch end;

	// �߳�״̬��
	private int status = ChildThread.STATUS_HASNO_ERROR;
	private int errorCount = 0;

	public ChildThreadChecker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChildThreadChecker(File checkingFile, Map<String, String> ERROR_CODE_MAP, FilePojo filePojo,
			FieldPojo[] fields, File logFile, String fileName, Integer startRowNumber, Integer blockSize,
			CountDownLatch start, CountDownLatch end) {
		super();
		this.ERROR_CODE_MAP = ERROR_CODE_MAP;
		this.filePojo = filePojo;
		this.fields = fields;
		this.fileName = fileName;
		this.logFile = logFile;
		this.startRowNumber = startRowNumber;
		this.blockSize = blockSize;
		this.start = start;
		this.end = end;
		this.checkingFile = checkingFile;
	}

	public synchronized int errorCounter() {
		return errorCount++;
	}

	@Override
	public void run() {
		FileOutputStream writer = null;
		// ������Ϣ
		String errorMsg = "";
		// �������
		String checkOutFlag = "";
		String rowValue;
		FieldPojo fieldPojo;

		try {
			start.await();

			Integer column_Count = filePojo.getColumnsTitle().split("\\|#\\|").length;
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();

			writer = new FileOutputStream(logFile);

			long readChars = BaseUtil.getFileAppointLinePointer(checkingFile.getAbsolutePath(), startRowNumber);
			System.out.println("readChars" + readChars);

			@SuppressWarnings("resource")
			RandomAccessFile randomAccessFile = new RandomAccessFile(checkingFile, "rw");
			randomAccessFile.seek(readChars);

			// ���ݼ�У��
			for (int i = 0; i < blockSize; i++) {
				rowValue = randomAccessFile.readLine();
				fieldsArray = rowValue.split("\\|#\\|");
				if (column_Count == fieldsArray.length) {
					for (int j = 0; j < column_Count; j++) {
						fieldPojo = fields[j];
						fieldValue = fieldsArray[j];
						// �ǿ�У��
						checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
									fieldValue);
							errorCounter();
							continue;
						}
						// �ֶγ���У��
						checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
									fieldValue);
							errorCounter();
							continue;
						}

						// ���ָ�ʽУ��
						if ("Number".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isNumber(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// ���ڸ�ʽУ��
						if ("Date".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// �����ʽУ��
						if ("Email".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isEmail(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// �ֻ������ʽУ��
						if ("Telephone".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isTelephoneNumber(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

					}

				} else if (1 == fieldsArray.length) {
					// ����У��
					checkOutFlag = "CHK010";
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
								"");
						errorCounter();
					}
					continue;
				} else {
					// ���ݼ��ֶ�������ƥ��
					checkOutFlag = "CHK007";
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
								"");
						errorCounter();
					}
					continue;
				}
				if (errorCount > 10000) {
					break;
				}
			}

			// ��־���
			System.out.println(this.getName() + "�Ĵ�����Ϣ��СΪ��" + errorMsg.length());
			System.out.println(this.getName() + "�Ĵ�����Ϣ����Ϊ��" + errorCount);
			writer.write(errorMsg.getBytes());
			writer.flush();

			if (errorCount > 0) {
				status = ChildThread.STATUS_HASNO_ERROR;
			}
			end.countDown();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public void writeLogMsg(FileOutputStream writer, String checkOutFlag, String fileName, String columnsTitleSplit,
			String errorMsg, int errorRowNumber, String fieldValue) {

		if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
			errorMsg += (fileName + columnsTitleSplit + errorRowNumber + columnsTitleSplit + checkOutFlag
					+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue + "\n");
		}

		// System.out.println(this.getName()+":"+errorMsg);

		// ���������Ϣ�ۼ���1024*1024����д��log��־
		if (errorMsg.length() > 1024 * 1024) {
			try {
				writer.write(errorMsg.getBytes());
				errorMsg = "";
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
