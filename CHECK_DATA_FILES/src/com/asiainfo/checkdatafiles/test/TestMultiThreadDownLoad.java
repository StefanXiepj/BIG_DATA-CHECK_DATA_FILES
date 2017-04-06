package com.asiainfo.checkdatafiles.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

import org.junit.Ignore;
import org.junit.Test;

import com.asiainfo.checkdatafiles.beltline.ExecuteMain;
import com.asiainfo.checkdatafiles.handler.BaseHandler;
import com.asiainfo.checkdatafiles.handler.MappingHandler;
import com.asiainfo.checkdatafiles.handler.MappingHandler;
import com.asiainfo.checkdatafiles.util.BaseUtil;

public class TestMultiThreadDownLoad {
	
	/*public String filePath;
	public int kernelCount;
	public int threadNum;

	@Ignore
	@Before
	public void setUp() throws Exception {
		this.filePath = "http://127.0.0.1:8080/Junit4Demo/file/20170322_20170323.txt";
		this.kernelCount = Runtime.getRuntime().availableProcessors();
		this.threadNum = kernelCount + 1;
	}

	@Ignore
	@Test
	public void testMultiThreadDownLoad() {
		MultiThreadDownLoad download = new MultiThreadDownLoad();
		//((Object) download).downLoad();
	}
	@Ignore
	@Test
	public void testNextMultiThreadDownLoad() {
		String url = "http://127.0.0.1:8080/Junit4Demo/file/20170322_20170323.txt";
		StartDownload download = new StartDownload();
		download.download(url, null, null);
	}*/
	//@Ignore
	@Test
	public void testExecute() {
		long start = System.currentTimeMillis();
		//String url = "http://127.0.0.1:8080/Junit4Demo/file/123.txt";
		String[] urls = new String[]{"D:\\download\\20170322_20170323.txt"};
		//String[] urls = new String[]{"D:\\download\\123.txt"};
		//String[] urls = new String[]{"D:\\download\\123.txt"};
		//ExecuteMain exe = new ExecuteMain();
		ExecuteMain exe = new ExecuteMain();
		exe.execute(urls);
		System.out.println(System.currentTimeMillis()-start);

	}
	@Ignore
	@Test
	public void testClassForName() throws Exception {
		
		
		//System.out.println(Runtime.getRuntime().availableProcessors());
		//String url = "http://127.0.0.1:8080/Junit4Demo/file/123.txt";
		/*String[] url = new String[]{"1","2","3","4","5"};
		Stack<String> stack = new Stack<String>();
		for(int i=0;i<url.length;i++){
			stack.push(url[i]);
		}
		stack.copyInto(url);
		for (int i = 0; i < url.length; i++) {
			String pop = stack.pop();
			System.out.println("pop:----"+pop);
		}*/
		
		//String mobile = "189001256411";
		
		//System.out.println(MobileUtil.isMobile(mobile));
		
		//String number = "46003";
		//System.out.println(BaseUtil.isNumber(number));
		/*long start = System.currentTimeMillis();
		String urls = "D:\\download\\123.txt";
		try {
			BaseUtil.readFile(urls);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//long time =System.currentTimeMillis()-start; 
			System.out.println(System.currentTimeMillis()-start);

		}*/
		
		/*HashMap<String, BaseHandler2> handlerFactory = MappingHandler2.handlerFactory();
		
		System.out.println(handlerFactory.get("IsStringHandler2"));
		
		//String test = "com.asiainfo.checkdatafiles.handler.IsStringHandler2";
		
		//System.out.println(test.substring(test.lastIndexOf(".")+1));
		
		try {
			Class.forName("com.asiainfo.checkdatafiles.handler."+"IsStringHandler").newInstance();
			//Class.forName(test.substring(test.lastIndexOf(".")+1)).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();                 20170322_20170323
		}*/
		
		InputStream is = new BufferedInputStream(new FileInputStream("D:\\download\\20170322_20170323.txt"));
		StringBuffer stringbuffer = new StringBuffer();

		int i;
		while ((i = is.read()) != -1) {

			stringbuffer.append((char) i);
		}
		String str_in = stringbuffer.toString();
		// 关闭流
		is.close();
		//String[] split = str_in.split("^[\n|\r\n]");
		//String[] split = str_in.split("\\\r\\\n");
		//正确
		//String[] split = str_in.split("\\\n|\\\r\\\n");
		//错误
		//String[] split = str_in.split("\\\r\\\n");
		String[] split = str_in.split("\\\n");
		System.out.println(split.length);
		for (int j = 0; j < 10; j++) {
			if(split.length>10){
				System.out.println(split[j]);
			}else{
				System.out.println(split[0]);
			}
			
		}
		
		
	}

}
