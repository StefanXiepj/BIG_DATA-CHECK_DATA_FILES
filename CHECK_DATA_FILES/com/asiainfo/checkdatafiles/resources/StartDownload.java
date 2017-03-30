/*  1:   */package com.asiainfo.checkdatafiles.resources;
/*  2:   */
/*  3:   */import java.io.BufferedInputStream;
/*  4:   */import java.io.FileInputStream;
/*  5:   */import java.io.InputStream;
/*  6:   */import java.io.PrintStream;
/*  7:   */import java.util.HashMap;
/*  8:   */import java.util.Set;
/*  9:   */
/* 10:   */public class StartDownload
/* 11:   */{
/* 12:   */  private static java.util.Map<String, String> initParameters;
/* 13:13 */  private String charset = "utf-8";
/* 14:14 */  private int threadNum = 1;
/* 15:15 */  private String fileDir = "D:/download/test/";
/* 16:   */  
/* 17:   */  static {
/* 18:18 */    java.util.Properties prop = new java.util.Properties();
/* 19:   */    
/* 20:   */    try
/* 21:   */    {
/* 22:22 */      InputStream in = new BufferedInputStream(new FileInputStream("conf/init_download.properties"));
/* 23:23 */      prop.load(in);
/* 24:24 */      java.util.Iterator<String> it = prop.stringPropertyNames().iterator();
/* 25:25 */      HashMap<String, String> properties = new HashMap();
/* 26:26 */      while (it.hasNext()) {
/* 27:27 */        String key = (String)it.next();
/* 28:28 */        properties.put(key, prop.getProperty(key));
/* 29:   */      }
/* 30:30 */      initParameters = properties;
/* 31:31 */      System.out.println(((String)initParameters.get("isMultiThreadDownload")).equals("YES"));
/* 32:   */      
/* 33:33 */      in.close();
/* 34:   */    }
/* 35:   */    catch (Exception e)
/* 36:   */    {
/* 37:37 */      System.out.println(e);
/* 38:   */    }
/* 39:   */  }
/* 40:   */  
/* 41:   */  public void download(String url, String encoding, String definedFileDir)
/* 42:   */  {
/* 43:43 */    if (((String)initParameters.get("isMultiThreadDownload")).equals("YES")) {
/* 44:44 */      this.threadNum = (Runtime.getRuntime().availableProcessors() + 1);
/* 45:   */    }
/* 46:46 */    if (((String)initParameters.get("isMultiThreadDownload")).equals("default")) {
/* 47:47 */      this.threadNum = 5;
/* 48:   */    }
/* 49:49 */    if (initParameters.get("fileDir") != null) {
/* 50:50 */      this.fileDir = ((String)initParameters.get("fileDir"));
/* 51:   */    }
/* 52:52 */    if (definedFileDir != null) {
/* 53:53 */      this.fileDir = definedFileDir;
/* 54:   */    }
/* 55:55 */    if (encoding != null) {
/* 56:56 */      this.charset = encoding;
/* 57:   */    }
/* 58:58 */    NextMultiThreadDownLoad download = new NextMultiThreadDownLoad();
/* 59:59 */    download.download(url, this.charset, this.fileDir, this.threadNum);
/* 60:   */  }
/* 61:   */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.resources.StartDownload
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */