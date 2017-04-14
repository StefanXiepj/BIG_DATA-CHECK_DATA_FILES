package com.asiainfo.checkdatafiles.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.asiainfo.checkdatafiles.beltline.ChainFileChecker;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.util.BaseUtil;

public class TestMultiThreadDownLoad {
	

	@Test
	public void testExecute(){
	/*	File files = new File("D:\\download\\file\\");
		File[] listFiles = files.listFiles();
		for (File file : listFiles) {
			String path = file.getAbsolutePath();
			System.out.println(path);
			
			String name = file.getName();
			
			System.out.println(name);
		}*/
		
		
		/*String fileName = "A0000059A8A362|#|18004788309|#|460036940781255|#|内蒙古|#|巴彦淖尔|#|华为|#|HW-KIW CL00|#|KIW-CL00C92B437|#|2017-03-22 00:00:08";
		int subStrCnt = BaseUtil.subStrCnt(fileName, "\\|#\\|");
		System.out.println("subStrCnt:"+subStrCnt);*/
		BaseUtil util = new BaseUtil();
		File file = new File("D:/download/file/20000000012008330004BUS10151201305301006.txt.checking");
		try {
			String guessFileEncoding = util.guessFileEncoding(file, 2);
			String guessFileEncoding2 = util.guessFileEncoding(file);
			//System.out.println(guessFileEncoding);
			System.out.println(guessFileEncoding2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		
	}
	@Ignore	
	@Test
	public void testExecute1() throws Exception{
		long startTimeMillis = System.currentTimeMillis();
		ChainFileChecker instance = ChainFileChecker.getInstance();
		instance.execute();
		long currentTimeMillis = System.currentTimeMillis();
		
		System.out.println("运行时长："+(currentTimeMillis - startTimeMillis));
	}
	@Ignore	
	@Test
	public void testExecute2() throws Exception{
		List<FilePojo> listFilePojo = FilePojo.getInstance();
		
		FieldPojo[] fields = listFilePojo.get(0).getFields();
	}
	
}
