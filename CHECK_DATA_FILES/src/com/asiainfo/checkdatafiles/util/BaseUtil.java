package com.asiainfo.checkdatafiles.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;

/**
 * Class Name: BaseUtil.java Description:
 * 
 * @author Stefan_xiepj DateTime 2017��4��14�� ����10:02:06
 * @company asiainfo
 * @email xiepj@asiainfo.com.cn
 * @version 1.0
 */
public class BaseUtil {
	
	
	Map<String,Writer> logMp = new ConcurrentHashMap<String, Writer>();
	public Map<String, Writer> getLogMp() {
		return logMp;
	}

	public void setLogMp(Map<String, Writer> logMp) {
		this.logMp = logMp;
	}

	public void writeLogFile(){
		
	}

	// У���ļ���
	public static String isLegalFileName(FilePojo filePojo, String fileName) {
		try {

			Integer retryFlag = Integer.parseInt(filePojo.getRetryFlag());
			Integer maxRetryCnt = Integer.parseInt(filePojo.getRetryCnt());
			Integer retryCnt = Integer.parseInt(fileName.substring(retryFlag - 1, retryFlag));

			if (retryCnt > maxRetryCnt) {
				return "CHK009";
			}

			return null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return "CHK009";
		}
	}

	// У���ļ��ӳ��ϴ�
	public static String isUploadTooLate(FilePojo filePojo, String uploadTime) {
		String appointTime = filePojo.getAppointTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			if (appointTime != null
					&& sdf.parse(uploadTime).getTime() > sdf.parse(filePojo.getAppointTime()).getTime()) {
				return "CHK008";
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	// У�����
	public static String isLegalEncoding(FilePojo filePojo, String encoding) {
		// �����ַ�ȱʡ����Ϊ GBK����δָ��У�����ʱ������GBK����У��
		String charset = "GBK";
		// ��ȡ�û�ָ������
		if (filePojo.getEncoding() != null) {
			charset = filePojo.getEncoding();
		}

		if ("GBK".equals(filePojo.getEncoding())) {
			if (charset.equals(encoding) || "GB2312".equals(encoding) || "GB18030".equals(encoding)) {
				return null;
			} else {
				return "CHK001";
			}
		} else if (filePojo.getEncoding().equals(encoding)) {
			return null;
		} else {
			return "CHK001";
		}

	}

	// У���ļ�������
	public static String isLegalHederLine(FilePojo filePojo, String headerLine) {

		if (filePojo.getColumnsTitle() != null && !(filePojo.getColumnsTitle().equals(headerLine))) {
			return "CHK003";
		}
		return null;

	}

	// У���¼����
	public static String isRowsEqual(Integer topRowValue, Integer rowsCnt) {

		if (!(topRowValue == (rowsCnt - 2))) {
			return "CHK002";
		}
		return null;

	}

	// У��ǿ�
	public static String isNull(FieldPojo fieldPojo, String fieldValue) {
		Integer isMust = Integer.parseInt(fieldPojo.getIsMust());
		if (isMust == 1 && ("".equals(fieldValue))) {
			return "CHK013";
		}
		return null;

	}

	// У���ֶγ���
	public static String isOverFieldLength(String fieldData, String parameter) {
		Integer legalLength = Integer.parseInt(parameter.substring(1));
		if (parameter.contains("V")) {
			if (fieldData.length() > legalLength) {
				return "CHK005";
			}
		}

		if (parameter.contains("F")) {
			if (fieldData.length() != legalLength) {
				return "CHK005";
			}
		}
		return null;

	}

	// У������
	public static String isEmail(String email) {

		String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(email);

		if (matcher.matches()) {
			return null;
		}
		return "CHK011";

	}

	// У���ֻ�����
	public static String isTelephoneNumber(String telephoneNumber) {
		String check = "^[1][3,4,5,8][0-9]{9}$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(telephoneNumber);

		if (matcher.matches()) {
			return null;
		}
		return "CHK012";
	}

	// У�����ڸ�ʽ
	public static String isDateTimeWithLongFormat(String timeStr) {
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
					if (lastDay < d) {
						return "CHK006";
					}
				}
				return null;
			}
			return "CHK006";
		}
		return "CHK006";
	}

	// ���ָ�ʽУ��
	public static String isNumber(String number) {

		String check = "^(-?[1-9]\\d*\\.?\\d*)|(-?0\\.\\d*[1-9])|(-?[0])|(-?[0]\\.\\d*)$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(number);
		if (matcher.matches()) {
			return null;
		}
		return "CHK004";
	}

	// ��ȡָ����
	public static String readAppointedLineNumber(LineNumberReader reader, int selectLineNumber) {
		
		reader.setLineNumber(selectLineNumber);
		try {
			return reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// ��ȡ������
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
	// ��ȡ��ָ���е��ַ���
	public static long getFileAppointLinePointer(String filename,int lineNumber) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		byte[] c = new byte[1024];
		int count = 1;
		int readChars = 0;
		long charsCount = 0L;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				charsCount++;
				if (c[i] == '\n')
					++count;
				if(count == lineNumber){
					return charsCount;
				}
			}
		}
		
		is.close();
		return count;
	}

	// ���ֽڶ�ȡ
	public static Map<String, Object> readFile(String filename) throws IOException {

		Map<String, Object> result = new HashMap<String, Object>();

		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		StringBuffer stringbuffer = new StringBuffer();

		int i;
		while ((i = is.read()) != -1) {
			stringbuffer.append((char) i);
		}
		String str_in = stringbuffer.toString();
		// �ر���
		is.close();
		// �ֽ���
		String[] rows = str_in.split("\\\n|\\\r\\\n");

		// ������
		Integer m = rows.length;
		// ����
		Integer n = rows[1].split("\\|#\\|").length;
		// ���ݼ�
		String[][] data = new String[m][n];
		// ���� & �ֶ���
		data[0][0] = rows[0];
		data[1][0] = rows[1];

		// ������
		String[] column = new String[] {};
		for (int x = 2; x < m; x++) {
			String row = rows[x];
			Integer isNullRow = 0;
			if (row.length() > 0) {
				isNullRow = row.length() - row.replaceAll("\\|#\\|", "\\|#").length() + 1;
				column = row.split("\\|#\\|");
			}

			if (isNullRow == n) {
				for (int y = 0; y < n; y++) {
					data[x][y] = column[y];
				}
			} else if (isNullRow < n && isNullRow > 0) {
				for (int y = 0; y < isNullRow; y++) {
					data[x][y] = column[y];
				}
				for (int y = isNullRow; y < n; y++) {
					data[x][y] = "|#|";
				}
			} else if (isNullRow > n) {
				for (int y = 0; y < n - 1; y++) {
					data[x][y] = column[y];
				}
				data[x][n - 1] = "|#|";
			} else {
				for (int y = 0; y < n; y++) {
					data[x][y] = "|#|";
				}
			}
		}

		result.put("ROW_COUNT", m);
		result.put("COLUMN_COUNT", n);
		result.put("DATA", data);

		return result;
	}

	public static int subStrCnt(String superString, String subString) {
		int count = 0;
		int index = superString.indexOf(superString);
		while (index != -1) {
			count++;
			index = superString.indexOf(subString, index + subString.length());
		}
		return count;
	}

	String encoding;
	boolean found;

	/**
	 * ����һ���ļ�(File)���󣬼���ļ�����
	 * 
	 * @param file
	 *            File����ʵ��
	 * @return �ļ����룬���ޣ��򷵻�null
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String guessFileEncoding(File file) throws FileNotFoundException, IOException {
		return guessFileEncoding(file, new nsDetector());
	}

	/**
	 * <pre>
	 * ��ȡ�ļ��ı���
	 * &#64;param file
	 *            File����ʵ��
	 * &#64;param languageHint
	 *            ������ʾ������� @see #nsPSMDetector ,ȡֵ���£�
	 *             1 : Japanese
	 *             2 : Chinese
	 *             3 : Simplified Chinese
	 *             4 : Traditional Chinese
	 *             5 : Korean
	 *             6 : Dont know(default)
	 * </pre>
	 * 
	 * @return �ļ����룬eg��UTF-8,GBK,GB2312��ʽ(��ȷ����ʱ�򣬷��ؿ��ܵ��ַ���������)�����ޣ��򷵻�null
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String guessFileEncoding(File file, int languageHint) throws FileNotFoundException, IOException {
		return guessFileEncoding(file, new nsDetector(languageHint));
	}

	/**
	 * ��ȡ�ļ��ı���
	 * 
	 * @param file
	 * @param det
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private String guessFileEncoding(File file, nsDetector det) throws FileNotFoundException, IOException {
		// Set an observer...
		// The Notify() will be called when a matching charset is found.
		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				encoding = charset;
				found = true;
			}
		});

		BufferedInputStream imp = new BufferedInputStream(new FileInputStream(file));
		byte[] buf = new byte[1024];
		int len;
		boolean done = false;
		boolean isAscii = false;

		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			// Check if the stream is only ascii.
			isAscii = det.isAscii(buf, len);
			if (isAscii) {
				break;
			}
			// DoIt if non-ascii and not done yet.
			done = det.DoIt(buf, len, false);
			if (done) {
				break;
			}
		}

		imp.close();
		det.DataEnd();

		if (isAscii) {
			encoding = "ASCII";
			found = true;
		}

		if (!found) {
			String[] prob = det.getProbableCharsets();
			// ���ｫ���ܵ��ַ��������������
			for (int i = 0; i < prob.length; i++) {
				if (i == 0) {
					encoding = prob[i];
				} else {
					encoding += "," + prob[i];
				}
			}

			if (prob.length > 0) {
				// ��û�з��������,Ҳ����ֻȡ��һ�����ܵı���,���ﷵ�ص���һ�����ܵ�����
				return encoding;
			} else {
				return null;
			}
		}
		return encoding;
	}

}
