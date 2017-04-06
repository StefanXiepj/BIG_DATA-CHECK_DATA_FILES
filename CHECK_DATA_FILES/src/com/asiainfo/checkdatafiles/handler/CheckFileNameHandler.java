package com.asiainfo.checkdatafiles.handler;

import org.apache.log4j.Logger;

public class CheckFileNameHandler extends FileNamingRule{

	Logger logger = Logger.getLogger(CheckFileNameHandler.class);

	@Override
	public void checkFileName(String fileName) {
		//（1）	长度为40位，且文件名称不能重复
		if(fileName.length() != 40){
			logger.error(fileName+":文件名长度不是40位！！");
		}
		//（2）	前10位是发起方系统编码，填写天翼终端经营分析编码2000000001
		
	}

}
