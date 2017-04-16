package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;

import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.util.BaseUtil;

public class MultiThreadChecker extends Thread {

	private Map<String, String> ERROR_CODE_MAP;
	LineNumberReader reader;
	private FilePojo filePojo;
	private FieldPojo[] fields;
	private FieldPojo fieldPojo;
	private String fileName;
	private File logFile;
	private String rowValue;
	private String[] fieldsArray;
	private String fieldValue;
	private Integer startRowNumber;
	private Integer rowsCount;

	public MultiThreadChecker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MultiThreadChecker(Map<String, String> ERROR_CODE_MAP,LineNumberReader reader, FilePojo filePojo, FieldPojo[] fields, FieldPojo fieldPojo,
			File logFile, String rowValue, String[] fieldsArray, String fieldValue) {
		super();
		this.ERROR_CODE_MAP = ERROR_CODE_MAP;
		this.reader = reader;
		this.filePojo = filePojo;
		this.fields = fields;
		this.fieldPojo = fieldPojo;
		this.fileName = fileName;
		this.logFile = logFile;
		this.rowValue = rowValue;
		this.fieldsArray = fieldsArray;
		this.fieldValue = fieldValue;
	}

	@Override
	public void run() {
		// 错误信息
		String errorMsg = "";

		// 错误编码
		String checkOutFlag = "";
		
		Integer column_Count = filePojo.getColumnsTitle().split(filePojo.getColumnsTitleSplit()).length;
		String columnsTitleSplit = filePojo.getColumnsTitleSplit();
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// 数据集校验
		for (int i = startRowNumber; i <= rowsCount; i++) {
			rowValue = BaseUtil.readAppointedLineNumber(reader, i);
			fieldsArray = rowValue.split("\\|#\\|");
			if (column_Count.equals(fieldsArray.length)) {
				for (int j = 0; j < column_Count; j++) {
					fieldPojo = fields[j];
					fieldValue = fieldsArray[j];
					// 非空校验
					checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						continue;
					}
					// 字段长度校验
					checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						continue;
					}

					// 数字格式校验
					if ("Number".equals(fieldPojo.getType())) {
						checkOutFlag = BaseUtil.isNumber(fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						}
						continue;
					}

					// 日期格式校验
					if ("Date".equals(fieldPojo.getType())) {
						checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						}
						continue;
					}

					// 邮箱格式校验
					if ("Email".equals(fieldPojo.getType())) {
						checkOutFlag = BaseUtil.isEmail(fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						}
						continue;
					}

					// 手机号码格式校验
					if ("Telephone".equals(fieldPojo.getType())) {
						checkOutFlag = BaseUtil.isTelephoneNumber(fieldValue);
						if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
							writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

						}
						continue;
					}

				}

				/*
				 * if (errorCount > 10000) { return false; }
				 */
			} else if (1 == fieldsArray.length) {
				// 空行校验
				checkOutFlag = "CHK010";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

				}
				continue;
			} else {
				// 数据集字段数量不匹配
				checkOutFlag = "CHK007";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					writeLogMsg(writer, checkOutFlag, fileName, columnsTitleSplit, errorMsg);

				}
				continue;
			}

		}
	}
	
	public void writeLogMsg(FileOutputStream writer, String checkOutFlag, String fileName, String columnsTitleSplit,String errorMsg){
		
		if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
			errorMsg = (fileName + columnsTitleSplit + "1" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
					+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
		}
		
		if(errorMsg != null || !"".equals(errorMsg)){
			try {
				writer.write(errorMsg.getBytes());
				errorMsg = "";
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
