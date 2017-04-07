package com.asiainfo.checkdatafiles.pojo;

public class ServiceTypePojo {
	
	//接口类型
	private String serviceType;
	//校验级别：定义 NAME_ENCODING_COUNT_FIELD 为文件名规则_编码_条数_字段 全校验
	//文件名规则:DEFAULT 不校验文件名；其余对应相应文件名规则
	private String checkLevel;
	//字段
	private String[] fields;

}
