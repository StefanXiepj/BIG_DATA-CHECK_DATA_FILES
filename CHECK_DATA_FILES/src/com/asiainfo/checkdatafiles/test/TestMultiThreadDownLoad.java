package com.asiainfo.checkdatafiles.test;

import org.junit.Test;

import com.asiainfo.checkdatafiles.beltline.Runner;

public class TestMultiThreadDownLoad {
	
	@Test
	public void testExecute(){
		Runner runner = Runner.getInstance();
		runner.execute();
	}
	
}
