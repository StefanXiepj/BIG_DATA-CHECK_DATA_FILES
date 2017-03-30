package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

public class CheckStringHandler extends BaseHandler {

	private static Logger logger = Logger.getLogger(CheckStringHandler.class);

	public CheckStringHandler() {
		super();
	}


	@Override
	public void handerRequest(String data, Integer lineNumber) {

		try {

			String parameter = this.getParameter();
			if (data != null && data.length() > Integer.parseInt(getParameter())) {
				logger.error("ERROR:第" + lineNumber + "行   " + data + " :字段长度超过了实际字段长度  " + parameter + " 的限制");
			}

			/*if (!stack.isEmpty()) {
				// 请求转发
				this.setSuccessor(successor);
				this.successor.handerRequest(stack,successor, lineNumber);
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
