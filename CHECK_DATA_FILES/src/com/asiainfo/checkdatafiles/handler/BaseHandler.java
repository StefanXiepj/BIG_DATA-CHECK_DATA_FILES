package com.asiainfo.checkdatafiles.handler;

import java.util.logging.Logger;

/**
 *  Class Name: BaseHandler.java
 *  Description: 
 *  @author Stefan_xiepj  DateTime 2017��3��28�� ����4:08:48 
 *  @company asiainfo 
 *  @email xiepj@asiainfo.com.cn  
 *  @version 1.0
 */
public abstract class BaseHandler{
	
	//�ʼ�Ա����
	
	private String handerName;
	private String parameter;
	private BaseHandler successor;
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public String getHanderName() {
		return handerName;
	}

	public void setHanderName(String handerName) {
		this.handerName = handerName;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public BaseHandler getSuccessor() {
		return successor;
	}

	public void setSuccessor(BaseHandler successor) {
		this.successor = successor;
	}
	
	//��������
	
	
	/**
	 *  Description:
	 *  @author Stefan_xiepj  DateTime 2017��3��28�� ����4:06:43
	 *  @param request
	 *  @param lineNumber
	 */
	public abstract void handerRequest(Object request);
}
