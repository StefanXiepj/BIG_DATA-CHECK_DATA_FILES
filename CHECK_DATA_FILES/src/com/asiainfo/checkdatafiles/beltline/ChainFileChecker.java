package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.flame.check.FileCharsetDetector;
import java.nio.channels.FileChannel;
import com.alibaba.fastjson.JSON;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.google.gson.JsonSyntaxException;

public class ChainFileChecker {

	private Logger logger = Logger.getLogger(ChainFileChecker.class);

	private static String SRC_FILE_PATH;
	private static String ERROR_LOG_PATH;
	private static String ERROR_COLUMNS_TITLE;
	private static Map<String, String> ERROR_CODE_MAP;
	private static Map<String, FilePojo> filePojoMap = new HashMap<String, FilePojo>();

	public void setINTERFACE_LIST(List<String> iNTERFACE_LIST) {
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

	public static void setFilePojoMap(Map<String, FilePojo> filePojoMap) {
		ChainFileChecker.filePojoMap = filePojoMap;
	}

	public static void set_instance(ChainFileChecker _instance) {
		ChainFileChecker._instance = _instance;
	}

	private static ChainFileChecker _instance;

	public static ChainFileChecker getInstance() {
		return _instance;
	}

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

			_instance = JSON.parseObject(initConfig, ChainFileChecker.class);

			List<FilePojo> filePojoList = FilePojo.getInstance();
			for (FilePojo filePojo : filePojoList) {
				filePojoMap.put(filePojo.getInterfaceName(), filePojo);
			}

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(configIn);
		}
	}

	public void execute() throws Exception {

		// 获取校验路径下所有文件集

		File files = new File(SRC_FILE_PATH);
		File[] listFiles = files.listFiles();
		long startTimeMillis;
		long endTimeMillis;
		boolean checkedResult;
		// 逐个文件校验
		for (int i = 0; i < listFiles.length; i++) {
			startTimeMillis = System.currentTimeMillis();
			File file = listFiles[i];

			// 过滤掉正在上传以及正在校验的文件
			if (!file.exists() || !file.isFile()) {
				logger.info(file.getName() + " doesn't exist or is not a file");
				return;
			}
			if (file.getName().endsWith(".ing")) {
				continue;
			}
			if (file.getName().endsWith(".checking")) {
				continue;
			}
			if (file.getName().endsWith(".checked")) {
				continue;
			}

			// 获取接口名称
			String fileName = file.getName();
			String interfaceName = fileName.substring(20, 28);

			// 根据接口名称获得接口抽象模板
			FilePojo filePojo = filePojoMap.get(interfaceName);

			if (filePojo == null) {
				continue;
			}

			// 修改文件名为。checking,开始校验
			File checkingFile = new File(file.getAbsolutePath() + ".checking");
			file.renameTo(checkingFile);

			// 创建反馈文件
			String errorPath = ERROR_LOG_PATH + fileName + ".error";
			File errorFile = new File(errorPath);
			if (errorFile.exists()) {
				errorFile.delete();
				errorFile.createNewFile();
			}

			// 获取文件校验等级，并校验
			String checkLevel = filePojo.getCheckLevel();

			if ("NAME_ENCODING_COUNT_FIELD".equals(checkLevel)) {
				checkedResult = checker(filePojo, checkingFile, errorFile);
				if (checkedResult) {
					logger.info(fileName + " is a Legal File!!!");
				} else {
					logger.info(fileName + " is a Illegal File!!!");
				}
			}
			if ("ENCODING_COUNT_FIELD".equals(checkLevel)) {

			}
			if ("COUNT_FIELD".equals(checkLevel)) {
			}
			if ("FIELD".equals(checkLevel)) {
			}

			// 校验完毕，更改文件状态
			checkingFile.renameTo(new File(file.getAbsolutePath() + ".checked"));
			endTimeMillis = System.currentTimeMillis();
			System.out.println(fileName + " 校验所用时长为：" + (endTimeMillis - startTimeMillis));
		}
	}

	private boolean checker(FilePojo filePojo, File checkingFile, File errorFile) {
		// 错误信息
		String errorMsg = "";
		// 错误数量
		int errorCount = 0;
		// 错误编码
		String checkOutFlag = "";

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

			Reader in = new FileReader(checkingFile);
			LineNumberReader reader = new LineNumberReader(in);

			// 总行数
			Integer row_count = BaseUtil.totalLines(checkingFile.getAbsolutePath());
			// 总列数
			Integer column_Count = filePojo.getColumnsTitle().split(filePojo.getColumnsTitleSplit()).length;
			// 总数据集
			// String[][] data = (String[][]) readFile.get("DATA");
			// 首行值
			Integer topRowValue = Integer.parseInt(BaseUtil.readAppointedLineNumber(reader, 1));
			// 第二行值
			String secondRowValue = BaseUtil.readAppointedLineNumber(reader, 2);

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

			FieldPojo[] fields = filePojo.getFields();
			FieldPojo fieldPojo;
			String rowValue = null;
			String[] fieldsArray;
			String fieldValue;
			
			ThreadGroup threadGroup = System.getSecurityManager().getThreadGroup();
			
			
			// 数据集校验
			for (int i = 2; i < row_count; i++) {
				rowValue = BaseUtil.readAppointedLineNumber(reader, i);
				fieldsArray = rowValue.split("\\|#\\|");
				if (column_Count.equals(fieldsArray.length)) {
					for (int j = 0; j < column_Count; j++) {
						fieldPojo = fields[j];
						fieldValue = fieldsArray[j];
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
									+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit
									+ fieldValue + "\n");
							errorCount++;
							continue;
						}

						// 数字格式校验
						if ("Number".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isNumber(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
										+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit
										+ fieldValue + "\n");
								errorCount++;
							}
							continue;
						}

						// 日期格式校验
						if ("Date".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isDateTimeWithLongFormat(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
										+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit
										+ fieldValue + "\n");
								errorCount++;
							}
							continue;
						}

						// 邮箱格式校验
						if ("Email".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isEmail(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
										+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit
										+ fieldValue + "\n");
								errorCount++;
							}
							continue;
						}

						// 手机号码格式校验
						if ("Telephone".equals(fieldPojo.getType())) {
							checkOutFlag = BaseUtil.isTelephoneNumber(fieldValue);
							if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
								errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
										+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit
										+ fieldValue + "\n");
								errorCount++;
							}
							continue;
						}

					}

					/*if (errorCount > 10000) {
						return false;
					}*/
				} else if (1 == fieldsArray.length) {
					// 空行校验
					checkOutFlag = "CHK010";
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
								+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
						errorCount++;
					}
					continue;
				} else {
					// 数据集字段数量不匹配
					checkOutFlag = "CHK007";
					if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
						errorMsg += (fileName + columnsTitleSplit + (i + 1) + columnsTitleSplit + checkOutFlag
								+ columnsTitleSplit + ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
						errorCount++;
					}
					continue;
				}
				
			}
			
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
	
	

}