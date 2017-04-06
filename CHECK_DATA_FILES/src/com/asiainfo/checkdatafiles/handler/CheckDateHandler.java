package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckDateHandler extends BaseHandler {
	
	private static Logger logger = Logger.getLogger(CheckDateHandler.class);  

	@Override
	public void handerRequest(Object request) {
		try {
			
			String parameter = this.getParameter();
			String[] data = (String[]) request;
			
			if(!BaseUtil.valiDateTimeWithLongFormat(data[0])){
				logger.error("ERROR:��"+lineNumber+"��   "+request + " :���ڸ�ʽ���󣡣�");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}