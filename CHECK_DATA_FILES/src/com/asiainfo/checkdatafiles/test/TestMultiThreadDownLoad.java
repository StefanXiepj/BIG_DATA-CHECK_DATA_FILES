package com.asiainfo.checkdatafiles.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.asiainfo.checkdatafiles.beltline.ChainFileChecker;
import com.asiainfo.checkdatafiles.pojo.FieldPojo;
import com.asiainfo.checkdatafiles.pojo.FilePojo;
import com.asiainfo.checkdatafiles.util.BaseUtil;
import com.asiainfo.checkdatafiles.util.LineNumberConfigReader;

public class TestMultiThreadDownLoad {
	
	@Ignore	
	@Test
	public void testSplit() throws Exception{
		File checkingFile = new File("D:\\download\\file\\20000000012008330004BUS10151201305301000.txt.checking");
/*		Reader in = new FileReader(checkingFile);
		LineNumberConfigReader reader = new LineNumberConfigReader(in );
		reader.setLineNumber(11);
		String readLine = reader.readLine();
		//readLine = reader.readLine();
		int lineNumber = reader.getLineNumber();
		System.out.println("lineNumber"+lineNumber);
		System.out.println("readLine:");
		System.out.println(readLine);
			*/	
		long readChars = BaseUtil.getFileAppointLinePointer(checkingFile.getAbsolutePath(), 10);
		System.out.println("readChars"+readChars);
		
		RandomAccessFile randomAccessFile = new RandomAccessFile(checkingFile, "rw");
		randomAccessFile.seek(readChars);
		
		String readLine10 = randomAccessFile.readLine();
		String readLine11 = randomAccessFile.readLine();
		String readLine12 = randomAccessFile.readLine();
		String string10 = new String(readLine10.getBytes("8859_1"),"gbk");
		String string11 = new String(readLine11.getBytes("8859_1"),"gbk");
		String string12 = new String(readLine12.getBytes("8859_1"),"gbk");
		System.out.println(string10);
		System.out.println(string11);
		System.out.println(string12);

		
		//String readAppointedLineNumber = BaseUtil.readAppointedLineNumber(reader , 5);
		//System.out.println(readAppointedLineNumber);
	}
	@Ignore
	@Test
	public void testExecute() throws Exception{
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
		/*File file = new File("D:\\download\\file\\20000000012008330004BUS10151201305301001.txt");
		File renameFile = new File("D:\\download\\file\\20000000012008330004BUS10151201305301001.txt.checked");
		
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
		}*/
		long currentTimeMillis = System.currentTimeMillis();
		String fileName = "D:\\download\\file\\20000000012008330004BUS10151201305301001.txt.checked";
		int totalLines = 0;
		try {
			totalLines = BaseUtil.totalLines(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		long endTimeMillis = System.currentTimeMillis();
		System.out.println(" 总行数 " + totalLines);
		System.out.println("总时间：" + (endTimeMillis-currentTimeMillis));
		System.out.println("------------------------------------------");
		
		
		long endTimeMillis1 = System.currentTimeMillis();
		Reader in = new FileReader(fileName);
		@SuppressWarnings("resource")
		LineNumberReader reader = new LineNumberReader(in);
		String s = reader.readLine();    
        int lines = 0;    
        while (s != null) {    
            lines++;    
            s = reader.readLine();    
        }
        
		System.out.println(" 总行数 " + lines);
		long endTimeMillis2 = System.currentTimeMillis();
		System.out.println("总时间：" + (endTimeMillis2-endTimeMillis1));
		
		in.close();
		reader.close();
		//BaseUtil.readAppointedLineNumber(reader, 8413340);
		
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
