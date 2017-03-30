package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

import com.asiainfo.checkdatafiles.util.BaseUtil;

public class CheckNumberHandler extends BaseHandler {

	Logger logger = Logger.getLogger(CheckNumberHandler.class);

	@Override
	public void handerRequest(String request, Integer lineNumber) {
		try {

			String parameter = this.getParameter();

			if (!BaseUtil.isNumber(request)) {
				logger.error("ERROR:第" + lineNumber + "行   " + request + " :不是数字！！");
			}
			if (request.length() > Integer.parseInt(parameter)) {
				logger.error("ERROR:第" + lineNumber + "行   " + request +  " :字段长度超过了实际字段长度  "+ parameter +" 的限制");
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
