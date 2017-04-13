package com.asiainfo.checkdatafiles.pojo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class FilePojo {

	// 接口名称
	private String interfaceName;
	// 校验级别：定义 NAME_ENCODING_COUNT_FIELD 为文件名规则_编码_条数_字段 全校验
	// 文件名规则:DEFAULT 不校验文件名；其余对应相应文件名规则
	private String checkLevel;
	// 约定上传时间
	private String appointTime;
	/*
	 * //约定上传格式 private String fileType;
	 */
	// 接口编码
	private String encoding;
	// 重传标志
	private String retryFlag;
	// 重传次数
	private String retryCnt;
	// 标题行
	private String columnsTitle;
	// 标题行分隔符
	private String columnsTitleSplit;
	// 字段定义
	private FieldPojo[] fields;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getCheckLevel() {
		return checkLevel;
	}

	public void setCheckLevel(String checkLevel) {
		this.checkLevel = checkLevel;
	}

	public String getAppointTime() {
		return appointTime;
	}

	public void setAppointTime(String appointTime) {
		this.appointTime = appointTime;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getRetryFlag() {
		return retryFlag;
	}

	public void setRetryFlag(String retryFlag) {
		this.retryFlag = retryFlag;
	}

	public String getRetryCnt() {
		return retryCnt;
	}

	public void setRetryCnt(String retryCnt) {
		this.retryCnt = retryCnt;
	}

	public String getColumnsTitle() {
		return columnsTitle;
	}

	public void setColumnsTitle(String columnsTitle) {
		this.columnsTitle = columnsTitle;
	}

	public String getColumnsTitleSplit() {
		return columnsTitleSplit;
	}

	public void setColumnsTitleSplit(String columnsTitleSplit) {
		this.columnsTitleSplit = columnsTitleSplit;
	}

	public FieldPojo[] getFields() {
		return fields;
	}

	public void setFields(FieldPojo[] fields) {
		this.fields = fields;
	}

	private static List<FilePojo> _instance;

	public static List<FilePojo> getInstance() {
		return _instance;
	}

	static {
		FileInputStream configIn = null;
		try {
			configIn = new FileInputStream("conf\\src_file_config.json");
			byte[] buf = new byte[1024];
			String strConfig = "";
			int length = 0;
			while ((length = configIn.read(buf)) != -1) {
				strConfig += new String(buf, 0, length);

			}

			_instance = JSONArray.parseArray(strConfig, FilePojo.class);
			

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(configIn);
		}
	}
}
