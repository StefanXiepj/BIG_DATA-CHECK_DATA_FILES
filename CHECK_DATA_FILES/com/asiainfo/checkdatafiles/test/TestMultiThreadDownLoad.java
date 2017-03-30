/*   1:    */package com.asiainfo.checkdatafiles.test;
/*   2:    */
/*   3:    */import com.asiainfo.checkdatafiles.handler.ExecuteMain;
/*   4:    */import java.io.BufferedInputStream;
/*   5:    */import java.io.FileInputStream;
/*   6:    */import java.io.InputStream;
/*   7:    */import java.io.PrintStream;
/*   8:    */import org.junit.Ignore;
/*   9:    */import org.junit.Test;
/*  10:    */
/*  47:    */public class TestMultiThreadDownLoad
/*  48:    */{
/*  49:    */  @Test
/*  50:    */  public void testExecute()
/*  51:    */  {
/*  52: 52 */    long start = System.currentTimeMillis();
/*  53:    */    
/*  54: 54 */    String[] urls = { "D:\\download\\20170322_20170323.txt" };
/*  55:    */    
/*  58: 58 */    ExecuteMain exe = new ExecuteMain();
/*  59: 59 */    exe.execute(urls);
/*  60: 60 */    System.out.println(System.currentTimeMillis() - start);
/*  61:    */  }
/*  62:    */  
/* 117:    */  @Ignore
/* 118:    */  @Test
/* 119:    */  public void testClassForName()
/* 120:    */    throws Exception
/* 121:    */  {
/* 122:122 */    InputStream is = new BufferedInputStream(new FileInputStream("D:\\download\\20170322_20170323.txt"));
/* 123:123 */    StringBuffer stringbuffer = new StringBuffer();
/* 124:    */    
/* 125:    */    int i;
/* 126:126 */    while ((i = is.read()) != -1) {
/* 127:    */      int i;
/* 128:128 */      stringbuffer.append((char)i);
/* 129:    */    }
/* 130:130 */    String str_in = stringbuffer.toString();
/* 131:    */    
/* 132:132 */    is.close();
/* 133:    */    
/* 139:139 */    String[] split = str_in.split("\\\n");
/* 140:140 */    System.out.println(split.length);
/* 141:141 */    for (int j = 0; j < 10; j++) {
/* 142:142 */      if (split.length > 10) {
/* 143:143 */        System.out.println(split[j]);
/* 144:    */      } else {
/* 145:145 */        System.out.println(split[0]);
/* 146:    */      }
/* 147:    */    }
/* 148:    */  }
/* 149:    */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.test.TestMultiThreadDownLoad
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */