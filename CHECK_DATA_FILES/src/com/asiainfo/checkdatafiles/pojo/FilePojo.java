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

	// �ӿ�����
	private String interfaceName;
	// У�鼶�𣺶��� NAME_ENCODING_COUNT_FIELD Ϊ�ļ�������_����_����_�ֶ� ȫУ��
	// �ļ�������:DEFAULT ��У���ļ����������Ӧ��Ӧ�ļ�������
	private String checkLevel;
	// Լ���ϴ�ʱ��
	private String appointTime;
	/*
	 * //Լ���ϴ���ʽ private String fileType;
	 */
	// �ӿڱ���
	private String encoding;
	// �ش���־
	private String retryFlag;
	// �ش�����
	private String retryCnt;
	// ������
	private String columnsTitle;
	// �����зָ���
	private String columnsTitleSplit;
	// �ֶζ���
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
