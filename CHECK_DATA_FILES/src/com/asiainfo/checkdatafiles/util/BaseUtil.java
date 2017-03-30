package com.asiainfo.checkdatafiles.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUtil {

	// 校验文件名
	public static boolean isLegalFileName(String fileName) {

		// fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1,
		// urlStr.lastIndexOf("?")>0 ? urlStr.lastIndexOf("?") :
		// urlStr.length());
		return true;
	}
	
	//校验字段长度
	public static boolean isOverFieldLength(String fieldData,String parameter){
		try {
			int fieldLength = Integer.parseInt(parameter);
			if(fieldData.length() > fieldLength){
				return false;
			}else{
			return true;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	// 校验邮箱
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	// 获取指定行
	public static String readAppointedLineNumber(LineNumberReader reader, int selectLineNumber) {
		try {
			String line = reader.readLine();
			while (line != null) {
			}
			reader.setLineNumber(selectLineNumber);
			String selectLine = reader.readLine();
			return selectLine;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	// 获取总行数
	public static int totalLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		byte[] c = new byte[1024];
		int count = 1;
		int readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {

				if (c[i] == '\n')
					++count;
			}
		}

		is.close();
		return count;
	}

	// 测试按字节流读取效率
	public static Map<String, Object> readFile(String filename) throws IOException {

		Map<String, Object> result = new HashMap<String, Object>();

		// 读取编码
		FileReader in = new FileReader(filename);
		String encoding = in.getEncoding();

		in.close();

		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		StringBuffer stringbuffer = new StringBuffer();

		int i;
		while ((i = is.read()) != -1) {

			stringbuffer.append((char) i);
		}
		String str_in = stringbuffer.toString();
		// 关闭流
		is.close();
		// 分解行
		String[] rows = str_in.split("\\\n|\\\r\\\n");
		
		// 总行数
		Integer m = rows.length;
		// 列数
		Integer n = rows[1].split("\\|#\\|").length;
		// 数据集
		String[][] data = new String[m][n];
		// 首行 & 字段行
		data[0][0] = rows[0];
		data[1][0] = rows[1];

		// 数据行
		String[] column = new String[] {};
		for (int x = 2; x < m; x++) {
			String string = rows[x];
			column = string.split("\\|");
			for (int y = 0; y < n; y++) {
				data[x][y] = column[y];
			}
		}

		System.out.println(m);
		System.out.println(n);

		result.put("Encoding", encoding);
		result.put("ROW_COUNT", m);
		result.put("COLUMN_COUNT", n);
		result.put("DATA", data);

		return result;
	}

	// 校验手机号码
	public static boolean isMobile(String str) {
		Pattern p = null;
		Matcher m = null;
		boolean b = false;
		p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
		m = p.matcher(str);
		b = m.matches();
		return b;
	}

	// 校验电话号码
	public static boolean isPhone(String str) {
		Pattern p1 = null, p2 = null;
		Matcher m = null;
		boolean b = false;
		p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$"); // 验证带区号的
		p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$"); // 验证没有区号的
		if (str.length() > 9) {
			m = p1.matcher(str);
			b = m.matches();
		} else {
			m = p2.matcher(str);
			b = m.matches();
		}
		return b;
	}

	// 校验日期格式
	public static boolean valiDateTimeWithLongFormat(String timeStr) {
		String format = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
				+ "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
		Pattern pattern = Pattern.compile(format);
		Matcher matcher = pattern.matcher(timeStr);
		if (matcher.matches()) {
			pattern = Pattern.compile("(\\d{4})-(\\d+)-(\\d+).*");
			matcher = pattern.matcher(timeStr);
			if (matcher.matches()) {
				int y = Integer.valueOf(matcher.group(1));
				int m = Integer.valueOf(matcher.group(2));
				int d = Integer.valueOf(matcher.group(3));
				if (d > 28) {
					Calendar c = Calendar.getInstance();
					c.set(y, m - 1, 1);
					int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
					return (lastDay >= d);
				}
			}
			return true;
		}
		return false;
	}

	// 数字格式校验
	public static boolean isNumber(String number) {

		String regex = "^(-?[1-9]\\d*\\.?\\d*)|(-?0\\.\\d*[1-9])|(-?[0])|(-?[0]\\.\\d*)$";
		if (!number.matches(regex)) {
			return false;
		}
		return true;
	}

}
