package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckMobileHandler extends BaseHandler {
	
	Logger logger = Logger.getLogger(CheckMobileHandler.class);

	@Override
	public void handerRequest(String request,Integer lineNumber) {
		try {

			String parameter = this.getParameter();
			
			if("MOBILE".equals(parameter)){
				if(!BaseUtil.isMobile(request)){
					logger.error("ERROR:��"+lineNumber+"��   "+request + " :�ֻ������ʽ���󣡣�");
				}
			} else if ("TELEPHONE".equals(parameter)){
				if(!BaseUtil.isPhone(request)){
					logger.error("ERROR:��"+lineNumber+"��   "+request + " :�绰�����ʽ���󣡣�");
				}
			}else{
				
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
