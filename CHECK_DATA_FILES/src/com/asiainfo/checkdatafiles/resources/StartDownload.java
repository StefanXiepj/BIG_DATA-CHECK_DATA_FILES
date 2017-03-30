package com.asiainfo.checkdatafiles.resources;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class StartDownload {
	private static  Map<String,String> initParameters;
	private  String charset = "utf-8";
	private  int threadNum = 1;
	private  String fileDir = "D:/download/test/";
	
	static{
		Properties prop = new Properties();
		//Map<String, String> parameters = new HashMap<String, String>();
		try{
	     //读取属性文件a.properties
	     InputStream in = new BufferedInputStream (new FileInputStream("conf/init_download.properties"));
	     prop.load(in);     ///加载属性列表
	     Iterator<String> it=prop.stringPropertyNames().iterator();
	     HashMap<String, String> properties = new HashMap<String, String>();
	     while(it.hasNext()){
	         String key=it.next();
	         properties.put(key, prop.getProperty(key));
	     }
	     initParameters = properties;
         System.out.println(initParameters.get("isMultiThreadDownload").equals("YES"));

	     in.close();
	     
	     	     
	     }catch(Exception e){
		     System.out.println(e);
		 }
	}
	
	public void download(String url,String encoding,String definedFileDir){
		
		if(initParameters.get("isMultiThreadDownload").equals("YES")){
			threadNum = Runtime.getRuntime().availableProcessors()+1;
		}
		if(initParameters.get("isMultiThreadDownload").equals("default")){
			threadNum = 5;
		}
		if(initParameters.get("fileDir") != null){
			fileDir = initParameters.get("fileDir");
		}
		if(definedFileDir != null){
			fileDir = definedFileDir;
		}
		if(encoding!=null){
			charset = encoding;
		}
		NextMultiThreadDownLoad download = new NextMultiThreadDownLoad();
		download.download(url, charset,fileDir,threadNum);
	
     }

}
