package com.asiainfo.checkdatafiles.handler;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckDateHandler extends BaseHandler {
	

	
	public String handerRequest(Object data) {
		String date = (String)data;
		return BaseUtil.isDateTimeWithLongFormat(date);
	}

}