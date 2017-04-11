package com.asiainfo.checkdatafiles.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class TermlSaleEmailData {
	
	public static void main(String[] args) throws ParseException {
		DateFormat dateInstance = DateFormat.getTimeInstance();
		String format = dateInstance.format(new Date());
		System.out.println(format);
	}

}
