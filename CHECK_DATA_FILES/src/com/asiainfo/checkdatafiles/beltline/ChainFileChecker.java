package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.asiainfo.checkdatafiles.util.LineNumberConfigReader;
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
			String errorPath = ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.error";
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

		File[] errorFiles = null;

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

			System.out.println(checkingFile.getAbsolutePath());
			Reader in = new FileReader(checkingFile);
			LineNumberConfigReader reader = new LineNumberConfigReader(in);

			// 总行数
			Integer row_count = BaseUtil.totalLines(checkingFile.getAbsolutePath());
			// 总数据集
			// String[][] data = (String[][]) readFile.get("DATA");
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
				errorCount++;
			}

			// 文件标题行校验
			checkOutFlag = BaseUtil.isLegalHederLine(filePojo, secondRowValue);
			if (ERROR_CODE_MAP.get(checkOutFlag) != null) {
				errorMsg += (fileName + columnsTitleSplit + "2" + columnsTitleSplit + checkOutFlag + columnsTitleSplit
						+ ERROR_CODE_MAP.get(checkOutFlag) + columnsTitleSplit + "\n");
				errorCount++;
			}

			// 数据集校验

			FieldPojo[] fields = filePojo.getFields();
			Integer startRowNumber;
			int core = Runtime.getRuntime().availableProcessors();
			CountDownLatch start = new CountDownLatch(1);
			CountDownLatch end = new CountDownLatch(core);
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(core, core + 1, 10, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(10));

			// 小于十万条数据，单线程处理
			if (row_count <= 10000) {
				core = 1;
			}
			System.out.println("core:"+ core);
			int blockSize = row_count / core;
			errorFiles = new File[core + 1];
			System.out.println("errorFiles.length:"+errorFiles.length);
			errorFiles[0] = errorFile;
			for (int i = 0; i < core; i++) {
				System.out.println("线程" + i + "准备就绪");
				errorFiles[i + 1] = new File(ERROR_LOG_PATH + fileName.substring(0, 37) + "999.txt.error" + i);
				if (errorFiles[i + 1].exists()) {
					errorFiles[i + 1].delete();
					errorFiles[i + 1].createNewFile();
				}
				
				startRowNumber = 2 + i * blockSize;
				System.out.println("startRowNumber:" + startRowNumber);
				if (i == (core - 1)) {
					blockSize = row_count - startRowNumber + 1;
				}
				threadPoolExecutor.submit(new ChildThreadChecker(checkingFile, ERROR_CODE_MAP, filePojo, fields,
						errorFiles[i], fileName, startRowNumber, blockSize, start, end));
			}

			start.countDown();
			System.out.println("所有校验线程开始校验");
			end.await();
			System.out.println("所有校验线程完成校验");
			threadPoolExecutor.shutdown();

			if (errorCount != 0) {
				return false;
			} else {
				return true;
			}

		} catch (Exception e) {
			logger.error(Level.ERROR, e);
			return false;
		} finally {
			// 合并日志文件
			for (int i = 0; i < errorFiles.length; i++) {
				
			}
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