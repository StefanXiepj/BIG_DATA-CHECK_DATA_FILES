package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

public class CheckFileNameHandler extends BaseHandler{

	Logger logger = Logger.getLogger(CheckFileNameHandler.class);
	
	@Override
	public void handerRequest(Object request) {
		
		//��1��	����Ϊ40λ�����ļ����Ʋ����ظ�
		if(request.length() != 40){
			logger.error(request+":�ļ������Ȳ���40λ����");
		}
		//��2��	ǰ10λ�Ƿ���ϵͳ���룬��д�����ն˾�Ӫ��������2000000001
		
	}

}
