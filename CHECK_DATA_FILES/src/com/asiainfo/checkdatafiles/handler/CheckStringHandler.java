package com.asiainfo.checkdatafiles.handler;

public class CheckStringHandler extends BaseHandler {

	@Override
	public boolean handerRequest(Object request) {
		String parameter = this.getParameter();
		String data = (String)request;

		if (data != null && parameter != null && data.length() > Integer.parseInt(this.getParameter())) {
			return false;
		}else{
			return true;
		}
	}
}
