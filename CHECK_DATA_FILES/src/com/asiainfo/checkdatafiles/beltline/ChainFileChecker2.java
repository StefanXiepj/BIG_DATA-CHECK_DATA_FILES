package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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

public class ChainFileChecker2 {

	private Logger logger = Logger.getLogger(ChainFileChecker2.class);

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
		ChainFileChecker2.filePojoMap = filePojoMap;
	}

	public static void set_instance(ChainFileChecker2 _instance) {
		ChainFileChecker2._instance = _instance;
	}

	private static ChainFileChecker2 _instance;

	public static ChainFileChecker2 getInstance() {
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

			_instance = JSON.parseObject(initConfig, ChainFileChecker2.class);

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

		// ��ȡУ��·���������ļ���

		File files = new File(SRC_FILE_PATH);
		File[] listFiles = files.listFiles();
		long startTimeMillis;
		long endTimeMillis;
		boolean checkedResult;
		// ����ļ�У��
		for (int i = 0; i < listFiles.length; i++) {
			startTimeMillis = System.currentTimeMillis();
			File file = listFiles[i];

			// ���˵������ϴ��Լ�����У����ļ�
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

			// ��ȡ�ӿ�����
			String fileName = file.getName();
			String interfaceName = fileName.substring(20, 28);

			// ���ݽӿ����ƻ�ýӿڳ���ģ��
			FilePojo filePojo = filePojoMap.get(interfaceName);

			if (filePojo == null) {
				continue;
			}

			// �޸��ļ���Ϊ��checking,��ʼУ��
			File checkingFile = new File(file.getAbsolutePath() + ".checking");
			file.renameTo(checkingFile);

			// ���������ļ�
			String errorPath = ERROR_LOG_PATH + fileName + ".error";
			File errorFile = new File(errorPath);
			if (errorFile.exists()) {
				errorFile.delete();
				errorFile.createNewFile();
			}

			// ��ȡ�ļ�У��ȼ�����У��
			String checkLevel = filePojo.getCheckLevel();

			if ("NAME_ENCODING_COUNT_FIELD".equals(checkLevel)) {
				FileInputStream fi = null;
				FileChannel channel = fi.getChannel();
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

			// У����ϣ������ļ�״̬
			checkingFile.renameTo(new File(file.getAbsolutePath() + ".checked"));
			endTimeMillis = System.currentTimeMillis();
			System.out.println(fileName + " У������ʱ��Ϊ��" + (endTimeMillis - startTimeMillis));
		}
	}


}