/*   1:    */package com.asiainfo.checkdatafiles.util;
/*   2:    */
/*   3:    */import java.io.BufferedInputStream;
/*   4:    */import java.io.FileInputStream;
/*   5:    */import java.io.FileNotFoundException;
/*   6:    */import java.io.FileReader;
/*   7:    */import java.io.IOException;
/*   8:    */import java.io.InputStream;
/*   9:    */import java.io.LineNumberReader;
/*  10:    */import java.io.PrintStream;
/*  11:    */import java.util.Calendar;
/*  12:    */import java.util.HashMap;
/*  13:    */import java.util.Map;
/*  14:    */import java.util.regex.Matcher;
/*  15:    */import java.util.regex.Pattern;
/*  16:    */
/*  24:    */public class BaseUtil
/*  25:    */{
/*  26:    */  public static boolean isLegalFileName(String fileName)
/*  27:    */  {
/*  28: 28 */    return true;
/*  29:    */  }
/*  30:    */  
/*  31:    */  public static boolean isOverFieldLength(String fieldData, String parameter)
/*  32:    */  {
/*  33:    */    try {
/*  34: 34 */      int fieldLength = Integer.parseInt(parameter);
/*  35: 35 */      if (fieldData.length() > fieldLength) {
/*  36: 36 */        return false;
/*  37:    */      }
/*  38: 38 */      return true;
/*  39:    */    }
/*  40:    */    catch (NumberFormatException e) {
/*  41: 41 */      e.printStackTrace(); }
/*  42: 42 */    return false;
/*  43:    */  }
/*  44:    */  
/*  46:    */  public static boolean checkEmail(String email)
/*  47:    */  {
/*  48: 48 */    boolean flag = false;
/*  49:    */    try {
/*  50: 50 */      String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
/*  51: 51 */      Pattern regex = Pattern.compile(check);
/*  52: 52 */      Matcher matcher = regex.matcher(email);
/*  53: 53 */      flag = matcher.matches();
/*  54:    */    } catch (Exception e) {
/*  55: 55 */      flag = false;
/*  56:    */    }
/*  57: 57 */    return flag;
/*  58:    */  }
/*  59:    */  
/*  60:    */  public static String readAppointedLineNumber(LineNumberReader reader, int selectLineNumber)
/*  61:    */  {
/*  62:    */    try {
/*  63: 63 */      String line = reader.readLine();
/*  64: 64 */      while (line != null) {}
/*  65:    */      
/*  66: 66 */      reader.setLineNumber(selectLineNumber);
/*  67: 67 */      return reader.readLine();
/*  68:    */    }
/*  69:    */    catch (FileNotFoundException e)
/*  70:    */    {
/*  71: 71 */      e.printStackTrace();
/*  72:    */    }
/*  73:    */    catch (IOException e) {
/*  74: 74 */      e.printStackTrace();
/*  75:    */    }
/*  76:    */    
/*  77: 77 */    return null;
/*  78:    */  }
/*  79:    */  
/*  80:    */  public static int totalLines(String filename) throws IOException
/*  81:    */  {
/*  82: 82 */    InputStream is = new BufferedInputStream(new FileInputStream(filename));
/*  83: 83 */    byte[] c = new byte[1024];
/*  84: 84 */    int count = 1;
/*  85: 85 */    int readChars = 0;
/*  86: 86 */    int i; for (; (readChars = is.read(c)) != -1; 
/*  87: 87 */        i < readChars) { i = 0;continue;
/*  88:    */      
/*  89: 89 */      if (c[i] == 10) {
/*  90: 90 */        count++;
/*  91:    */      }
/*  92: 87 */      i++;
/*  93:    */    }
/*  94:    */    
/*  99: 94 */    is.close();
/* 100: 95 */    return count;
/* 101:    */  }
/* 102:    */  
/* 103:    */  public static Map<String, Object> readFile(String filename)
/* 104:    */    throws IOException
/* 105:    */  {
/* 106:101 */    Map<String, Object> result = new HashMap();
/* 107:    */    
/* 109:104 */    FileReader in = new FileReader(filename);
/* 110:105 */    String encoding = in.getEncoding();
/* 111:    */    
/* 112:107 */    in.close();
/* 113:    */    
/* 114:109 */    InputStream is = new BufferedInputStream(new FileInputStream(filename));
/* 115:110 */    StringBuffer stringbuffer = new StringBuffer();
/* 116:    */    
/* 117:    */    int i;
/* 118:113 */    while ((i = is.read()) != -1) {
/* 119:    */      int i;
/* 120:115 */      stringbuffer.append((char)i);
/* 121:    */    }
/* 122:117 */    String str_in = stringbuffer.toString();
/* 123:    */    
/* 124:119 */    is.close();
/* 125:    */    
/* 126:121 */    String[] rows = str_in.split("\\\n|\\\r\\\n");
/* 127:    */    
/* 129:124 */    Integer m = Integer.valueOf(rows.length);
/* 130:    */    
/* 131:126 */    Integer n = Integer.valueOf(rows[1].split("\\|#\\|").length);
/* 132:    */    
/* 133:128 */    String[][] data = new String[m.intValue()][n.intValue()];
/* 134:    */    
/* 135:130 */    data[0][0] = rows[0];
/* 136:131 */    data[1][0] = rows[1];
/* 137:    */    
/* 139:134 */    String[] column = new String[0];
/* 140:135 */    for (int x = 2; x < m.intValue(); x++) {
/* 141:136 */      String string = rows[x];
/* 142:137 */      column = string.split("\\|");
/* 143:138 */      for (int y = 0; y < n.intValue(); y++) {
/* 144:139 */        data[x][y] = column[y];
/* 145:    */      }
/* 146:    */    }
/* 147:    */    
/* 148:143 */    System.out.println(m);
/* 149:144 */    System.out.println(n);
/* 150:    */    
/* 151:146 */    result.put("Encoding", encoding);
/* 152:147 */    result.put("ROW_COUNT", m);
/* 153:148 */    result.put("COLUMN_COUNT", n);
/* 154:149 */    result.put("DATA", data);
/* 155:    */    
/* 156:151 */    return result;
/* 157:    */  }
/* 158:    */  
/* 159:    */  public static boolean isMobile(String str)
/* 160:    */  {
/* 161:156 */    Pattern p = null;
/* 162:157 */    Matcher m = null;
/* 163:158 */    boolean b = false;
/* 164:159 */    p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$");
/* 165:160 */    m = p.matcher(str);
/* 166:161 */    b = m.matches();
/* 167:162 */    return b;
/* 168:    */  }
/* 169:    */  
/* 170:    */  public static boolean isPhone(String str)
/* 171:    */  {
/* 172:167 */    Pattern p1 = null;Pattern p2 = null;
/* 173:168 */    Matcher m = null;
/* 174:169 */    boolean b = false;
/* 175:170 */    p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");
/* 176:171 */    p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");
/* 177:172 */    if (str.length() > 9) {
/* 178:173 */      m = p1.matcher(str);
/* 179:174 */      b = m.matches();
/* 180:    */    } else {
/* 181:176 */      m = p2.matcher(str);
/* 182:177 */      b = m.matches();
/* 183:    */    }
/* 184:179 */    return b;
/* 185:    */  }
/* 186:    */  
/* 187:    */  public static boolean valiDateTimeWithLongFormat(String timeStr)
/* 188:    */  {
/* 189:184 */    String format = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
/* 190:    */    
/* 191:186 */    Pattern pattern = Pattern.compile(format);
/* 192:187 */    Matcher matcher = pattern.matcher(timeStr);
/* 193:188 */    if (matcher.matches()) {
/* 194:189 */      pattern = Pattern.compile("(\\d{4})-(\\d+)-(\\d+).*");
/* 195:190 */      matcher = pattern.matcher(timeStr);
/* 196:191 */      if (matcher.matches()) {
/* 197:192 */        int y = Integer.valueOf(matcher.group(1)).intValue();
/* 198:193 */        int m = Integer.valueOf(matcher.group(2)).intValue();
/* 199:194 */        int d = Integer.valueOf(matcher.group(3)).intValue();
/* 200:195 */        if (d > 28) {
/* 201:196 */          Calendar c = Calendar.getInstance();
/* 202:197 */          c.set(y, m - 1, 1);
/* 203:198 */          int lastDay = c.getActualMaximum(5);
/* 204:199 */          return lastDay >= d;
/* 205:    */        }
/* 206:    */      }
/* 207:202 */      return true;
/* 208:    */    }
/* 209:204 */    return false;
/* 210:    */  }
/* 211:    */  
/* 213:    */  public static boolean isNumber(String number)
/* 214:    */  {
/* 215:210 */    String regex = "^(-?[1-9]\\d*\\.?\\d*)|(-?0\\.\\d*[1-9])|(-?[0])|(-?[0]\\.\\d*)$";
/* 216:211 */    if (!number.matches(regex)) {
/* 217:212 */      return false;
/* 218:    */    }
/* 219:214 */    return true;
/* 220:    */  }
/* 221:    */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.util.BaseUtil
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */