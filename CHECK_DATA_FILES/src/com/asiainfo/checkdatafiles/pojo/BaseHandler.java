package com.asiainfo.checkdatafiles.pojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.flame.check.FileCharsetDetector;

import com.asiainfo.checkdatafiles.beltline.ChainFileChecker2;
import com.asiainfo.checkdatafiles.util.BaseUtil;

public class BaseHandler {

	private Logger logger = Logger.getLogger(BaseHandler.class);
	private Map<String, String> ERROR_CODE_MAP;
	private String ERROR_COLUMNS_TITLE;
	// 错误信息
	String errorMsg = "";
	// 错误数量
	int errorCount = 0;
	// 错误编码
	String checkOutFlag = "";
	FilePojo filePojo;
	File checkingFile;
	File errorFile;
	String fileName = "";
	String columnsTitleSplit = filePojo.getColumnsTitleSplit();
	
	private boolean checker(FilePojo filePojo, File checkingFile, File errorFile) {
		

		try {
			String fileName = checkingFile.getName().substring(0, checkingFile.getName().lastIndexOf("."));
			// 分割符
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();
			// 获取上传时间
			long lastModified = checkingFile.lastModified();
			Calendar cd = Calendar.getInstance();
			cd.setTimeInMillis(lastModified);
			String uploadtime = DateFormat.getTimeInstance().format(cd.getTime());

			// 校验文件名
			checkOutFlag = BaseUtil.isLegalFileName(filePojo, fileName);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
			}

			// 校验文件延迟上传
			checkOutFlag = BaseUtil.isUploadTooLate(filePojo, uploadtime);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
			}

			// 编码校验
			FileCharsetDetector fileCharsetDetector = new FileCharsetDetector();
			String encoding = fileCharsetDetector.guessFileEncoding(checkingFile);
			System.out.println("encoding:" + encoding);
			checkOutFlag = BaseUtil.isLegalEncoding(filePojo, encoding);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;

				return false;
			}

			// 流校验
			
			FileInputStream in = new FileInputStream(checkingFile);
			FileChannel channel = in.getChannel();
			long fileSize = channel.size();
			channel.close();
			in.close();
			if(fileSize <= 8*1024*1024*200){
				useRamdomAccessMemoryToCheck(checkingFile);
			}

			

			
			FieldPojo[] fields = filePojo.getFields();
			
			if (errorCount != 0) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			logger.error(Level.ERROR, e);
			return false;
		} finally {
			// 输出log
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
				errorFile.delete();
			}

		}

	}

	// 内存式校验，用于200M以下文件校验
	public void useRamdomAccessMemoryToCheck(FilePojo filePojo, File checkingFile, File errorFile) {

		// 读取文件流
		Map<String, Object> readFile = BaseUtil.readFile(checkingFile.getAbsolutePath());

		// 总行数
		Integer row_count = (Integer) readFile.get("ROW_COUNT");
		// 总列数
		Integer column_Count = (Integer) readFile.get("COLUMN_COUNT");
		// 总数据集
		String[][] data = (String[][]) readFile.get("DATA");
		// 首行值
		Integer topRowValue = Integer.parseInt(data[0][0]);
		// 第二行值
		String secondRowValue = data[1][0];

		// 文件记录行数校验
		checkOutFlag = BaseUtil.isRowsEqual(topRowValue, row_count);
		if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
			errorMsg += (fileName + columnsTitleSplit + "1" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
					+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
			errorCount++;
		}

		// 文件标题行校验
		checkOutFlag = BaseUtil.isLegalHederLine(filePojo, secondRowValue);
		if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
			errorMsg += (fileName + columnsTitleSplit + "2" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
					+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
			errorCount++;
		}
		
		for (int i = 2; i < row_count; i++) {
		for (int j = 0; j < column_Count; j++) {
			FieldPojo fieldPojo = fields[j];
			String fieldValue = data[i][j];

			// 空行校验
			if (j == 0 && "|#|".equals(fieldValue)) {
				checkOutFlag = "CHK010";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
					errorCount++;
				}
				break;
			}

			// 数据集字段数量不匹配
			if (j != 0 && "|#|".equals(fieldValue)) {
				checkOutFlag = "CHK007";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
					errorCount++;
				}
				continue;
			}

			// 非空校验
			checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
						+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
				continue;
			}
			// 字段长度校验
			checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
						+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue + "\n");
				errorCount++;
				continue;
			}

			// 数字格式校验
			if ("Number".equals(fieldPojo.getType())) {
				checkOutFlag = BaseUtil.isNumber(fieldValue);
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue
							+ "\n");
					errorCount++;
				}
				continue;
			}

			// 日期格式校验
			if ("Date".equals(fieldPojo.getType())) {
				checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue
							+ "\n");
					errorCount++;
				}
				continue;
			}

			// 邮箱格式校验
			if ("Email".equals(fieldPojo.getType())) {
				checkOutFlag = BaseUtil.isEmail(fieldValue);
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue
							+ "\n");
					errorCount++;
				}
				continue;
			}

			// 手机号码格式校验
			if ("Telephone".equals(fieldPojo.getType())) {
				checkOutFlag = BaseUtil.isTelephoneNumber(fieldValue);
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue
							+ "\n");
					errorCount++;
				}
				continue;
			}

		}

		if (errorCount > 10000) {
			return false;
		}

	}

	}

	// IO流式校验，用于大于200M文件校验
	public void useIoToCheck(File checkingFile) {

	}
}
