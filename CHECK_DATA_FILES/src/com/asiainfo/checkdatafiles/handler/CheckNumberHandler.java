package com.asiainfo.checkdatafiles.handler;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckNumberHandler extends BaseHandler {

	@Override
	public boolean handerRequest(Object request) {
		String number = (String)request;
		return BaseUtil.isNumber(number);
	}
}
