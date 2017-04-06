package com.asiainfo.checkdatafiles.handler;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckDateHandler extends BaseHandler {
	

	@Override
	public boolean handerRequest(Object data) {
		String date = (String)data;
		return BaseUtil.isDateTimeWithLongFormat(date);
	}

}