package com.asiainfo.checkdatafiles.handler;

import java.util.logging.Logger;

/**
 *  Class Name: BaseHandler.java
 *  Description: 
 *  @author Stefan_xiepj  DateTime 2017年3月28日 下午4:08:48 
 *  @company asiainfo 
 *  @email xiepj@asiainfo.com.cn  
 *  @version 1.0
 */
public abstract class BaseHandler{
	
	//质检员名称
	
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
	
	//请求处理方法
	
	
	/**
	 *  Description:
	 *  @author Stefan_xiepj  DateTime 2017年3月28日 下午4:06:43
	 *  @param request
	 *  @param lineNumber
	 */
	public abstract void handerRequest(Object request);
}
