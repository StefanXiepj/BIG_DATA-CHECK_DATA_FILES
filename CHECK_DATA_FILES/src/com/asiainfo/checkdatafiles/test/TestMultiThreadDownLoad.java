package com.asiainfo.checkdatafiles.test;

import java.io.File;

import org.junit.Test;

import com.asiainfo.checkdatafiles.beltline.ChainFileChecker;

public class TestMultiThreadDownLoad {
	
	@Test
	public void testExecute(){
		File files = new File("D:\\download\\file\\");
		File[] listFiles = files.listFiles();
		for (File file : listFiles) {
			String path = file.getAbsolutePath();
			System.out.println(path);
			
			String name = file.getName();
			
			System.out.println(name);
		}
	}
	
}
