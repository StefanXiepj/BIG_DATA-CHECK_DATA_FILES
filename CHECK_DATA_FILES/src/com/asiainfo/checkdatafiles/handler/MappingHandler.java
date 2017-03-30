package com.asiainfo.checkdatafiles.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;

public class MappingHandler {

	Logger logger = Logger.getLogger(CheckNumberHandler.class);

	static HashMap<String, String> fieldMapping = new LinkedHashMap<String, String>() ;
	static HashMap<String, BaseHandler> handlerMapping = new LinkedHashMap<String, BaseHandler>() ;
	
	// 校验者清单
	Map<String, BaseHandler> handlerMap = new LinkedHashMap<String, BaseHandler>();

	static {
		Properties prop = new Properties();
		// Map<String, String> parameters = new HashMap<String, String>();
		try {
			// 读取属性文件a.properties
			InputStream in = new BufferedInputStream(
					new FileInputStream("D:\\workspaces\\CHECK_DATA_FILES\\conf\\field_mapping.properties"));
			// 加载属性列表
			prop.load(in);
			Iterator<String> it = prop.stringPropertyNames().iterator();
			HashMap<String, String> properties = new HashMap<String, String>();

			while (it.hasNext()) {
				String key = it.next();
				properties.put(key, prop.getProperty(key));
			}
			fieldMapping = properties;
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//质检员工厂
	public static HashMap<String, BaseHandler> handlerFactory(){
		try {
			String handlerNames = fieldMapping.get("HANDLER2");
			String[] handlerList = handlerNames.split(",");
			for (int i = 0; i < handlerList.length; i++) {
				BaseHandler hander = (BaseHandler) Class.forName("com.asiainfo.checkdatafiles.handler."+handlerList[i]).newInstance();
				handlerMapping.put(handlerList[i], hander);
			}
			
			return handlerMapping;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	//老版本
	public BaseHandler createValidateQueue(String[] fields) {

		// 解析数据行
		String functionName = null;
		String funParameter = null;
		String key = null;
		String value = null;
		String[] splitValues = null;
		BaseHandler handler = null;

		try {
			// 获得处理方法
			for (int i = 0; i < fields.length; i++) {
				// 从字段映射文件中取得
				key = fields[i];
				value = fieldMapping.get(key);

				// 无法匹配时，选用default配置
				if (value == null) {
					value = fieldMapping.get("DEFAULT");
				}

				{
					splitValues = value.split(",");
					functionName = splitValues[0];
					funParameter = splitValues[1];
				}
				// 构建方法队列
				handler = (BaseHandler) Class.forName(functionName).newInstance();
				handler.setParameter(funParameter);
				handler.setHanderName(key);
				handlerMap.put(fields[i], handler);
			}

			// 创建处理链
			for (int i = 1; i < fields.length; i++) {
				handlerMap.get(fields[i - 1]).setSuccessor(handlerMap.get(fields[i]));
			}

			return handlerMap.get(fields[0]);

		} catch (InstantiationException e) {
			e.printStackTrace();
			logger.error("InstantiationException");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			logger.error("IllegalAccessException");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error("ClassNotFoundException");
		}
		return null;
	}

}
