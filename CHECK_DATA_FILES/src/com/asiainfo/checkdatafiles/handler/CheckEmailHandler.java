package com.asiainfo.checkdatafiles.handler;

import com.asiainfo.checkdatafiles.util.BaseUtil;


public class CheckEmailHandler extends BaseHandler {
	
	@Override
	public boolean handerRequest(Object request) {
			String email = (String)request;
			return BaseUtil.isEmail(email);

	}
}
