/*   1:    */package com.asiainfo.checkdatafiles.resources;
/*   2:    */
/*   3:    */import java.io.BufferedInputStream;
/*   4:    */import java.io.BufferedOutputStream;
/*   5:    */import java.io.FileInputStream;
/*   6:    */import java.io.FileNotFoundException;
/*   7:    */import java.io.FileOutputStream;
/*   8:    */import java.io.FileReader;
/*   9:    */import java.io.IOException;
/*  10:    */import java.io.InputStream;
/*  11:    */import java.io.LineNumberReader;
/*  12:    */import java.io.PrintStream;
/*  13:    */import java.net.HttpURLConnection;
/*  14:    */import java.net.URL;
/*  15:    */import java.net.URLConnection;
/*  16:    */import java.util.UUID;
/*  17:    */import java.util.concurrent.CountDownLatch;
/*  18:    */import java.util.concurrent.ExecutorService;
/*  19:    */import java.util.concurrent.Executors;
/*  20:    */import java.util.concurrent.TimeUnit;
/*  21:    */import java.util.logging.Level;
/*  22:    */import java.util.logging.Logger;
/*  23:    */
/*  24:    */public class MultiThreadDownLoad
/*  25:    */{
/*  26: 26 */  private String charset = "utf-8";
/*  27:    */  private int threadNum;
/*  28: 28 */  private String fileName = null;
/*  29: 29 */  private URL url = null;
/*  30: 30 */  private long threadLength = 0L;
/*  31: 31 */  private int threadLines = 0;
/*  32:    */  private ChildThread[] childThreads;
/*  33: 33 */  private String fileDir = null;
/*  34: 34 */  private boolean statusError = false;
/*  35: 35 */  private int sleepSeconds = 5000;
/*  36:    */  
/*  37: 37 */  Logger logger = Logger.getLogger(getClass().getName());
/*  38:    */  
/*  39:    */  public void download(String urlStr, String charset, String fileDir, int threadNum) {
/*  40: 40 */    this.charset = charset;
/*  41: 41 */    this.fileDir = fileDir;
/*  42: 42 */    this.threadNum = threadNum;
/*  43: 43 */    long contentLength = 0L;
/*  44: 44 */    CountDownLatch latch = new CountDownLatch(threadNum);
/*  45: 45 */    this.childThreads = new ChildThread[threadNum];
/*  46: 46 */    long[] startPos = new long[threadNum];
/*  47: 47 */    long endPos = 0L;
/*  48: 48 */    this.logger.log(Level.INFO, "charset = ----" + charset);
/*  49: 49 */    this.logger.log(Level.INFO, "fileDir = ----" + fileDir);
/*  50: 50 */    this.logger.log(Level.INFO, "threadNum = ----" + threadNum);
/*  51:    */    try
/*  52:    */    {
/*  53: 53 */      this.fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.lastIndexOf("?") > 0 ? urlStr.lastIndexOf("?") : urlStr.length());
/*  54: 54 */      if ("".equalsIgnoreCase(this.fileName)) {
/*  55: 55 */        this.fileName = UUID.randomUUID().toString();
/*  56:    */      }
/*  57:    */      
/*  58: 58 */      this.logger.log(Level.INFO, "fileName = ----" + this.fileName);
/*  59:    */      
/*  61: 61 */      this.url = new URL(urlStr);
/*  62: 62 */      URLConnection con = this.url.openConnection();
/*  63: 63 */      setHeader(con);
/*  64:    */      
/*  65: 65 */      contentLength = con.getContentLength();
/*  66:    */      
/*  67: 67 */      this.threadLength = (contentLength / threadNum);
/*  68:    */      
/*  69: 69 */      this.logger.log(Level.INFO, "threadLength = ----" + this.threadLength);
/*  70:    */      
/*  72: 72 */      setThreadBreakpoint(fileDir, this.fileName, contentLength, startPos);
/*  73: 73 */      this.logger.log(Level.INFO, "startPos = ----" + startPos);
/*  74:    */      
/*  75: 75 */      ExecutorService exec = Executors.newCachedThreadPool();
/*  76: 76 */      for (int i = 0; i < threadNum; i++)
/*  77:    */      {
/*  78: 78 */        startPos[i] += this.threadLength * i;
/*  79:    */        
/*  82: 82 */        if (i == threadNum - 1) {
/*  83: 83 */          endPos = contentLength;
/*  84:    */        } else {
/*  85: 85 */          endPos = this.threadLength * (i + 1) - 1L;
/*  86:    */        }
/*  87:    */        
/*  88: 88 */        ChildThread thread = new ChildThread(this, latch, i, startPos[i], endPos);
/*  89: 89 */        this.childThreads[i] = thread;
/*  90: 90 */        exec.execute(thread);
/*  91:    */      }
/*  92:    */      
/*  96: 96 */      latch.await();
/*  97: 97 */      exec.shutdown();
/*  98:    */      
/* 100:100 */      tempFileToTargetFile(this.childThreads);
/* 101:    */    }
/* 102:    */    catch (Exception e) {
/* 103:103 */      e.printStackTrace();
/* 104:    */    }
/* 105:    */  }
/* 106:    */  
/* 111:    */  private void setThreadBreakpoint(String fileDir, String fileName, long contentLength, long[] startPos)
/* 112:    */  {
/* 113:113 */    java.io.File file = new java.io.File(fileDir + fileName);
/* 114:114 */    long localFileSize = file.length();
/* 115:    */    
/* 116:116 */    if (file.exists())
/* 117:    */    {
/* 118:118 */      if (localFileSize == contentLength)
/* 119:    */      {
/* 120:120 */        this.logger.log(Level.INFO, "file " + fileName + " has exists,exe will exit!!");
/* 121:121 */        return;
/* 122:    */      }
/* 123:123 */      this.logger.log(Level.INFO, "Error in downloaded file, Now download continue ... ");
/* 124:    */      
/* 126:126 */      java.io.File tempFileDir = new java.io.File(fileDir);
/* 127:127 */      java.io.File[] files = tempFileDir.listFiles();
/* 128:128 */      for (int k = 0; k < files.length; k++) {
/* 129:129 */        String tempFileName = files[k].getName();
/* 130:    */        
/* 131:131 */        if ((tempFileName != null) && (files[k].length() > 0L) && 
/* 132:132 */          (tempFileName.startsWith(fileName + "_"))) {
/* 133:133 */          int fileLongNum = Integer.parseInt(tempFileName
/* 134:134 */            .substring(tempFileName.lastIndexOf("_") + 1, 
/* 135:135 */            tempFileName.lastIndexOf("_") + 2));
/* 136:    */          
/* 137:137 */          startPos[fileLongNum] = files[k].length();
/* 138:    */        }
/* 139:    */      }
/* 140:    */    }
/* 141:    */    else
/* 142:    */    {
/* 143:    */      try {
/* 144:144 */        file.createNewFile();
/* 145:    */      } catch (IOException e) {
/* 146:146 */        e.printStackTrace();
/* 147:    */      }
/* 148:    */    }
/* 149:    */  }
/* 150:    */  
/* 151:    */  private void tempFileToTargetFile(ChildThread[] childThreads) {
/* 152:    */    try {
/* 153:153 */      BufferedOutputStream outputStream = new BufferedOutputStream(
/* 154:154 */        new FileOutputStream(this.fileDir + this.fileName));
/* 155:    */      
/* 157:157 */      for (int i = 0; i < this.threadNum; i++) {
/* 158:158 */        if (this.statusError) {
/* 159:159 */          for (int k = 0; k < this.threadNum; k++) {
/* 160:160 */            if (childThreads[k].tempFile.length() == 0L)
/* 161:161 */              childThreads[k].tempFile.delete();
/* 162:    */          }
/* 163:163 */          this.logger.log(Level.INFO, "本次下载任务不成功，请重新设置线程数。");
/* 164:164 */          break;
/* 165:    */        }
/* 166:    */        
/* 167:167 */        BufferedInputStream inputStream = new BufferedInputStream(
/* 168:168 */          new FileInputStream(childThreads[i].tempFile));
/* 169:169 */        this.logger.log(Level.INFO, "Now is file " + childThreads[i].id);
/* 170:170 */        int len = 0;
/* 171:171 */        long count = 0L;
/* 172:172 */        byte[] b = new byte[1024];
/* 173:173 */        while ((len = inputStream.read(b)) != -1) {
/* 174:174 */          count += len;
/* 175:175 */          outputStream.write(b, 0, len);
/* 176:176 */          if (count % 4096L == 0L) {
/* 177:177 */            outputStream.flush();
/* 178:    */          }
/* 179:    */        }
/* 180:    */        
/* 183:183 */        inputStream.close();
/* 184:    */        
/* 185:185 */        if (childThreads[i].status == 1) {
/* 186:186 */          childThreads[i].tempFile.delete();
/* 187:    */        }
/* 188:    */      }
/* 189:    */      
/* 190:190 */      outputStream.flush();
/* 191:191 */      outputStream.close();
/* 192:    */    } catch (FileNotFoundException e) {
/* 193:193 */      e.printStackTrace();
/* 194:    */    } catch (IOException e) {
/* 195:195 */      e.printStackTrace();
/* 196:    */    }
/* 197:    */  }
/* 198:    */  
/* 201:    */  private void setHeader(URLConnection con)
/* 202:    */  {
/* 203:203 */    con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
/* 204:204 */    con.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
/* 205:205 */    con.setRequestProperty("Accept-Encoding", "aa");
/* 206:206 */    con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
/* 207:207 */    con.setRequestProperty("Keep-Alive", "300");
/* 208:208 */    con.setRequestProperty("Connection", "keep-alive");
/* 209:209 */    con.setRequestProperty("If-Modified-Since", "Fri, 02 Jan 2009 17:00:05 GMT");
/* 210:210 */    con.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
/* 211:211 */    con.setRequestProperty("Cache-Control", "max-age=0");
/* 212:212 */    con.setRequestProperty("Referer", "http://www.dianping.com");
/* 213:    */  }
/* 214:    */  
/* 215:    */  public class ChildThread
/* 216:    */    extends Thread
/* 217:    */  {
/* 218:    */    public static final int STATUS_HASNOT_FINISHED = 0;
/* 219:    */    public static final int STATUS_HAS_FINISHED = 1;
/* 220:    */    public static final int STATUS_HTTPSTATUS_ERROR = 2;
/* 221:    */    private MultiThreadDownLoad task;
/* 222:    */    private int id;
/* 223:    */    private long startPosition;
/* 224:    */    private long endPosition;
/* 225:    */    private final CountDownLatch latch;
/* 226:226 */    private java.io.File tempFile = null;
/* 227:    */    
/* 228:228 */    private int status = 0;
/* 229:    */    
/* 230:    */    public ChildThread(MultiThreadDownLoad multiThreadDownLoad, CountDownLatch latch, int id, long startPos, long endPos)
/* 231:    */    {
/* 232:232 */      this.task = multiThreadDownLoad;
/* 233:233 */      this.id = id;
/* 234:234 */      this.startPosition = startPos;
/* 235:235 */      this.endPosition = endPos;
/* 236:236 */      this.latch = latch;
/* 237:    */      try
/* 238:    */      {
/* 239:239 */        this.tempFile = new java.io.File(this.task.fileDir + this.task.fileName + "_" + id);
/* 240:240 */        if (!this.tempFile.exists()) {
/* 241:241 */          this.tempFile.createNewFile();
/* 242:    */        }
/* 243:    */      } catch (IOException e) {
/* 244:244 */        e.printStackTrace();
/* 245:    */      }
/* 246:    */    }
/* 247:    */    
/* 248:    */    public void run()
/* 249:    */    {
/* 250:250 */      MultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " run ...");
/* 251:251 */      HttpURLConnection con = null;
/* 252:252 */      InputStream inputStream = null;
/* 253:253 */      BufferedOutputStream outputStream = null;
/* 254:254 */      long count = 0L;
/* 255:255 */      long threadDownloadLength = this.endPosition - this.startPosition;
/* 256:    */      try
/* 257:    */      {
/* 258:258 */        outputStream = new BufferedOutputStream(new FileOutputStream(this.tempFile.getPath(), true));
/* 259:    */      } catch (FileNotFoundException e2) {
/* 260:260 */        e2.printStackTrace();
/* 261:    */      }
/* 262:    */      
/* 263:263 */      for (int k = 0; k < 10; k++) {
/* 264:264 */        if (k > 0) {
/* 265:265 */          MultiThreadDownLoad.this.logger.log(Level.INFO, "Now thread " + this.id + "is reconnect, start position is " + this.startPosition);
/* 266:    */        }
/* 267:    */        try {
/* 268:268 */          con = (HttpURLConnection)this.task.url.openConnection();
/* 269:269 */          MultiThreadDownLoad.this.setHeader(con);
/* 270:270 */          con.setAllowUserInteraction(true);
/* 271:    */          
/* 272:272 */          con.setConnectTimeout(10000);
/* 273:    */          
/* 274:274 */          con.setReadTimeout(10000);
/* 275:    */          
/* 276:276 */          if (this.startPosition < this.endPosition)
/* 277:    */          {
/* 278:278 */            con.setRequestProperty("Range", "bytes=" + this.startPosition + "-" + 
/* 279:279 */              this.endPosition);
/* 280:280 */            MultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " startPosition is " + this.startPosition);
/* 281:281 */            MultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " endPosition is " + this.endPosition);
/* 282:    */            
/* 285:285 */            if ((con.getResponseCode() != 200) && 
/* 286:286 */              (con.getResponseCode() != 206)) {
/* 287:287 */              MultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + ": code = " + 
/* 288:288 */                con.getResponseCode() + ", status = " + 
/* 289:289 */                con.getResponseMessage());
/* 290:290 */              this.status = 2;
/* 291:291 */              this.task.statusError = true;
/* 292:292 */              outputStream.close();
/* 293:293 */              con.disconnect();
/* 294:294 */              MultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " finished.");
/* 295:295 */              this.latch.countDown();
/* 296:296 */              break;
/* 297:    */            }
/* 298:    */            
/* 299:299 */            inputStream = con.getInputStream();
/* 300:    */            
/* 301:301 */            int len = 0;
/* 302:302 */            byte[] b = new byte[1024];
/* 303:303 */            while ((len = inputStream.read(b)) != -1) {
/* 304:304 */              outputStream.write(b, 0, len);
/* 305:305 */              count += len;
/* 306:306 */              this.startPosition += len;
/* 307:    */              
/* 308:308 */              if (count % 4096L == 0L) {
/* 309:309 */                outputStream.flush();
/* 310:    */              }
/* 311:    */            }
/* 312:    */            
/* 313:313 */            System.out.println("count is " + count);
/* 314:314 */            if (count >= threadDownloadLength) {
/* 315:315 */              this.status = 1;
/* 316:    */            }
/* 317:317 */            outputStream.flush();
/* 318:318 */            outputStream.close();
/* 319:319 */            inputStream.close();
/* 320:320 */            con.disconnect();
/* 321:    */          } else {
/* 322:322 */            this.status = 1;
/* 323:    */          }
/* 324:    */          
/* 325:325 */          System.out.println("Thread " + this.id + " finished.");
/* 326:326 */          this.latch.countDown();
/* 327:    */        }
/* 328:    */        catch (IOException e) {
/* 329:    */          try {
/* 330:330 */            outputStream.flush();
/* 331:331 */            TimeUnit.SECONDS.sleep(MultiThreadDownLoad.this.sleepSeconds);
/* 332:    */          } catch (InterruptedException e1) {
/* 333:333 */            e1.printStackTrace();
/* 334:    */          } catch (IOException e2) {
/* 335:335 */            e2.printStackTrace();
/* 336:    */          }
/* 337:    */        }
/* 338:    */      }
/* 339:    */    }
/* 340:    */  }
/* 341:    */  
/* 342:    */  static int getTotalLines(java.io.File file)
/* 343:    */    throws IOException
/* 344:    */  {
/* 345:345 */    FileReader in = new FileReader(file);
/* 346:346 */    LineNumberReader reader = new LineNumberReader(in);
/* 347:347 */    String s = reader.readLine();
/* 348:348 */    int lines = 0;
/* 349:349 */    while (s != null) {
/* 350:350 */      lines++;
/* 351:351 */      s = reader.readLine();
/* 352:    */    }
/* 353:353 */    reader.close();
/* 354:354 */    in.close();
/* 355:355 */    return lines;
/* 356:    */  }
/* 357:    */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.resources.MultiThreadDownLoad
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */