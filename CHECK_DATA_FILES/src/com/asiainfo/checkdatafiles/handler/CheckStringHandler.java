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
				logger.error("ERROR:��" + lineNumber + "��   " + data + " :�ֶγ��ȳ�����ʵ���ֶγ���  " + parameter + " ������");
			}

			/*if (!stack.isEmpty()) {
				// ����ת��
				this.setSuccessor(successor);
				this.successor.handerRequest(stack,successor, lineNumber);
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
