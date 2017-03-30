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
				logger.error("ERROR:��" + lineNumber + "��   " + request + " :�������֣���");
			}
			if (request.length() > Integer.parseInt(parameter)) {
				logger.error("ERROR:��" + lineNumber + "��   " + request +  " :�ֶγ��ȳ�����ʵ���ֶγ���  "+ parameter +" ������");
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
