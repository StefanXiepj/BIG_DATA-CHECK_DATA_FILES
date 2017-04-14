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
	// ������Ϣ
	String errorMsg = "";
	// ��������
	int errorCount = 0;
	// �������
	String checkOutFlag = "";
	FilePojo filePojo;
	File checkingFile;
	File errorFile;
	String fileName = "";
	String columnsTitleSplit = filePojo.getColumnsTitleSplit();
	
	private boolean checker(FilePojo filePojo, File checkingFile, File errorFile) {
		

		try {
			String fileName = checkingFile.getName().substring(0, checkingFile.getName().lastIndexOf("."));
			// �ָ��
			String columnsTitleSplit = filePojo.getColumnsTitleSplit();
			// ��ȡ�ϴ�ʱ��
			long lastModified = checkingFile.lastModified();
			Calendar cd = Calendar.getInstance();
			cd.setTimeInMillis(lastModified);
			String uploadtime = DateFormat.getTimeInstance().format(cd.getTime());

			// У���ļ���
			checkOutFlag = BaseUtil.isLegalFileName(filePojo, fileName);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
			}

			// У���ļ��ӳ��ϴ�
			checkOutFlag = BaseUtil.isUploadTooLate(filePojo, uploadtime);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "0" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
			}

			// ����У��
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

			// ��У��
			
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
			// ���log
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

	// �ڴ�ʽУ�飬����200M�����ļ�У��
	public void useRamdomAccessMemoryToCheck(FilePojo filePojo, File checkingFile, File errorFile) {

		// ��ȡ�ļ���
		Map<String, Object> readFile = BaseUtil.readFile(checkingFile.getAbsolutePath());

		// ������
		Integer row_count = (Integer) readFile.get("ROW_COUNT");
		// ������
		Integer column_Count = (Integer) readFile.get("COLUMN_COUNT");
		// �����ݼ�
		String[][] data = (String[][]) readFile.get("DATA");
		// ����ֵ
		Integer topRowValue = Integer.parseInt(data[0][0]);
		// �ڶ���ֵ
		String secondRowValue = data[1][0];

		// �ļ���¼����У��
		checkOutFlag = BaseUtil.isRowsEqual(topRowValue, row_count);
		if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
			errorMsg += (fileName + columnsTitleSplit + "1" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
					+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
			errorCount++;
		}

		// �ļ�������У��
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

			// ����У��
			if (j == 0 && "|#|".equals(fieldValue)) {
				checkOutFlag = "CHK010";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
					errorCount++;
				}
				break;
			}

			// ���ݼ��ֶ�������ƥ��
			if (j != 0 && "|#|".equals(fieldValue)) {
				checkOutFlag = "CHK007";
				if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
					errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
							+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
					errorCount++;
				}
				continue;
			}

			// �ǿ�У��
			checkOutFlag = BaseUtil.isNull(fieldPojo, fieldValue);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
						+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
				continue;
			}
			// �ֶγ���У��
			checkOutFlag = BaseUtil.isOverFieldLength(fieldValue, fieldPojo.getLength());
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
						+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + fieldValue + "\n");
				errorCount++;
				continue;
			}

			// ���ָ�ʽУ��
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

			// ���ڸ�ʽУ��
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

			// �����ʽУ��
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

			// �ֻ������ʽУ��
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

	// IO��ʽУ�飬���ڴ���200M�ļ�У��
	public void useIoToCheck(File checkingFile) {

	}
}
