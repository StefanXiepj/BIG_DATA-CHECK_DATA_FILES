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
	// 校验状态码
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

	// 线程状态码
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
		// 错误信息
		String errorMsg = "";
		// 错误编码
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

			// 数据集校验
			for (int i = 0; i < blockSize; i++) {
				rowValue = randomAccessFile.readLine();
				fieldsArray = rowValue.split("\\|#\\|");
				if (column_Count == fieldsArray.length) {
					for (int j = 0; j < column_Count; j++) {
						fieldPojo = fields[j];
						fieldValue = fieldsArray[j];
						// 非空校验
						checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
									fieldValue);
							errorCounter();
							continue;
						}
						// 字段长度校验
						checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
									fieldValue);
							errorCounter();
							continue;
						}

						// 数字格式校验
						if ("Number".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isNumber(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// 日期格式校验
						if ("Date".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// 邮箱格式校验
						if ("Email".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isEmail(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg,
										i + startRowNumber, fieldValue);
								errorCounter();
							}
							continue;
						}

						// 手机号码格式校验
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
					// 空行校验
					checkOutFlag = "CHK010";
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg, i + startRowNumber,
								"");
						errorCounter();
					}
					continue;
				} else {
					// 数据集字段数量不匹配
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

			// 日志输出
			System.out.println(this.getName() + "的错误信息大小为：" + errorMsg.length());
			System.out.println(this.getName() + "的错误信息数量为：" + errorCount);
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

		// 如果错误信息累计至1024*1024，则写入log日志
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
