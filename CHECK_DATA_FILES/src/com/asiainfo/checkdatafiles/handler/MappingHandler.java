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
	
	// У�����嵥
	Map<String, BaseHandler> handlerMap = new LinkedHashMap<String, BaseHandler>();

	static {
		Properties prop = new Properties();
		// Map<String, String> parameters = new HashMap<String, String>();
		try {
			// ��ȡ�����ļ�a.properties
			InputStream in = new BufferedInputStream(
					new FileInputStream("D:\\workspaces\\CHECK_DATA_FILES\\conf\\field_mapping.properties"));
			// ���������б�
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
	
	//�ʼ�Ա����
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

	//�ϰ汾
	public BaseHandler createValidateQueue(String[] fields) {

		// ����������
		String functionName = null;
		String funParameter = null;
		String key = null;
		String value = null;
		String[] splitValues = null;
		BaseHandler handler = null;

		try {
			// ��ô�����
			for (int i = 0; i < fields.length; i++) {
				// ���ֶ�ӳ���ļ���ȡ��
				key = fields[i];
				value = fieldMapping.get(key);

				// �޷�ƥ��ʱ��ѡ��default����
				if (value == null) {
					value = fieldMapping.get("DEFAULT");
				}

				{
					splitValues = value.split(",");
					functionName = splitValues[0];
					funParameter = splitValues[1];
				}
				// ������������
				handler = (BaseHandler) Class.forName(functionName).newInstance();
				handler.setParameter(funParameter);
				handler.setHanderName(key);
				handlerMap.put(fields[i], handler);
			}

			// ����������
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
