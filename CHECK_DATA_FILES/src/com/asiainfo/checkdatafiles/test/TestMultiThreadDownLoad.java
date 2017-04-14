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

import org.apache.commons.io.FilenameUtils;
import org.flame.check.FileCharsetDetector;

public class TestMultiThreadDownLoad {
	
	@Ignore	
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
		
		
		/*String fileName = "A0000059A8A362|#|18004788309|#|460036940781255|#|ÄÚÃÉ¹Å|#|°ÍÑåÄ×¶û|#|»ªÎª|#|HW-KIW CL00|#|KIW-CL00C92B437|#|2017-03-22 00:00:08";
		int subStrCnt = BaseUtil.subStrCnt(fileName, "\\|#\\|");
		System.out.println("subStrCnt:"+subStrCnt);*/
		File file = new File("D:\\download\\file\\20000000012008330004BUS10151201305301001.txt");
		File renameFile = new File("D:\\download\\file\\20000000012008330004BUS10151201305301001.txt.checking");
		
		if(renameFile.exists()){
			renameFile.delete();
		}
		boolean renameTo = file.renameTo(renameFile);
		System.out.println(renameTo);
		if(renameTo){
			try {
				FileCharsetDetector fileCharsetDetector = new FileCharsetDetector();
				String guessFileEncoding = fileCharsetDetector.guessFileEncoding(renameFile);
				System.out.println(guessFileEncoding);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		
		
	}

	@Test
	public void testExecute1() throws Exception{
		ChainFileChecker instance = ChainFileChecker.getInstance();
		instance.execute();
		
	}
	@Ignore	
	@Test
	public void testExecute2() throws Exception{
		List<FilePojo> listFilePojo = FilePojo.getInstance();
		
		FieldPojo[] fields = listFilePojo.get(0).getFields();
	}
	
}
