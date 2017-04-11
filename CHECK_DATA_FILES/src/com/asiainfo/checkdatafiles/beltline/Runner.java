package com.asiainfo.checkdatafiles.beltline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.asiainfo.checkdatafiles.handler.BaseHandler;
import com.asiainfo.checkdatafiles.handler.CheckNumberHandler;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Runner {

	//private Logger logger = Logger.getLogger(CheckNumberHandler.class);
	private static List<String> INTERFACE_LIST;
	private static String SRC_FILE_URL;
	private static String ERROR_LOG_URL;
	private static String ERROR_COLUMNS_TITLE;
	private static Map<String,String> ERROR_CODE_MAP;
	private static Map<String,FilePojo> filePojoMap = new HashMap<String, FilePojo>();

	
	
	
	public void setINTERFACE_LIST(List<String> iNTERFACE_LIST) {
		INTERFACE_LIST = iNTERFACE_LIST;
	}


	public void setSRC_FILE_URL(String sRC_FILE_URL) {
		SRC_FILE_URL = sRC_FILE_URL;
	}


	public void setERROR_LOG_URL(String eRROR_LOG_URL) {
		ERROR_LOG_URL = eRROR_LOG_URL;
	}


	public void setERROR_COLUMNS_TITLE(String eRROR_COLUMNS_TITLE) {
		ERROR_COLUMNS_TITLE = eRROR_COLUMNS_TITLE;
	}


	public void setERROR_CODE_MAP(Map<String, String> eRROR_CODE_MAP) {
		ERROR_CODE_MAP = eRROR_CODE_MAP;
	}


	public static void setFilePojoMap(Map<String, FilePojo> filePojoMap) {
		Runner.filePojoMap = filePojoMap;
	}


	public static void set_instance(Runner _instance) {
		Runner._instance = _instance;
	}


	private static Runner _instance;
	
	public static Runner getInstance(){
		return _instance;
	}
	
	static{
		FileInputStream configIn = null;
		try {
			configIn = new FileInputStream("conf\\__init__.json");
			byte[] buf = new byte[1024];
			String initConfig = "";
			int length = 0;
			while ((length = configIn.read(buf)) != -1) {
				initConfig += new String(buf, 0, length);

			}
			
			_instance = JSON.parseObject(initConfig, Runner.class);
			
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

	
	public void execute(){
	
		File files = new File(this.SRC_FILE_URL);
		File[] listFiles = files.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			String fileName = file.getName();
			String interfaceName = fileName.substring(20, 28);
			System.out.println("interfaceName:"+interfaceName);
			
			FilePojo filePojo = filePojoMap.get(interfaceName);
			
		}
	}


}