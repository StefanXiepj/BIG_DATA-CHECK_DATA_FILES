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
					logger.error("ERROR:第"+lineNumber+"行   "+request + " :手机号码格式错误！！");
				}
			} else if ("TELEPHONE".equals(parameter)){
				if(!BaseUtil.isPhone(request)){
					logger.error("ERROR:第"+lineNumber+"行   "+request + " :电话号码格式错误！！");
				}
			}else{
				
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
