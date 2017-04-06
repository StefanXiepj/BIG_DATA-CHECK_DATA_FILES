package com.asiainfo.checkdatafiles.handler;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckMobileHandler extends BaseHandler {

	@Override
	public boolean handerRequest(Object request) {
		
		String parameter = this.getParameter();
		String telephone = (String)request;
		
		if("MOBILE".equals(parameter)){
			return BaseUtil.isMobile(telephone);
		} else if ("TELEPHONE".equals(parameter)){
			return BaseUtil.isPhone(telephone);
		}else{
			return false;
		}
	}
}
