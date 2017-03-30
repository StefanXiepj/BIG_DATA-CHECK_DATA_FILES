/*   1:    */package com.asiainfo.checkdatafiles.resources;
/*   2:    */
/*   3:    */import java.io.BufferedInputStream;
/*   4:    */import java.io.BufferedOutputStream;
/*   5:    */import java.io.File;
/*   6:    */import java.io.FileInputStream;
/*   7:    */import java.io.FileNotFoundException;
/*   8:    */import java.io.FileOutputStream;
/*   9:    */import java.io.IOException;
/*  10:    */import java.io.InputStream;
/*  11:    */import java.io.PrintStream;
/*  12:    */import java.net.HttpURLConnection;
/*  13:    */import java.net.URL;
/*  14:    */import java.net.URLConnection;
/*  15:    */import java.util.UUID;
/*  16:    */import java.util.concurrent.CountDownLatch;
/*  17:    */import java.util.concurrent.ExecutorService;
/*  18:    */import java.util.concurrent.Executors;
/*  19:    */import java.util.concurrent.TimeUnit;
/*  20:    */import java.util.logging.Level;
/*  21:    */import java.util.logging.Logger;
/*  22:    */
/*  24:    */public class NextMultiThreadDownLoad
/*  25:    */{
/*  26:    */  private int threadNum;
/*  27: 27 */  private String fileName = null;
/*  28: 28 */  private URL url = null;
/*  29: 29 */  private int threadLines = 0;
/*  30:    */  private ChildThread[] childThreads;
/*  31: 31 */  private String fileDir = null;
/*  32: 32 */  private boolean statusError = false;
/*  33: 33 */  private int sleepSeconds = 5000;
/*  34:    */  
/*  35:    */  private long threadLength;
/*  36: 36 */  Logger logger = Logger.getLogger(getClass().getName());
/*  37:    */  
/*  38:    */  public void download(String urlStr, String charset, String fileDir, int threadNum) {
/*  39: 39 */    this.fileDir = fileDir;
/*  40: 40 */    this.threadNum = threadNum;
/*  41: 41 */    long contentLength = 0L;
/*  42: 42 */    CountDownLatch latch = new CountDownLatch(threadNum);
/*  43: 43 */    this.childThreads = new ChildThread[threadNum];
/*  44: 44 */    long[] startPos = new long[threadNum];
/*  45: 45 */    long endPos = 0L;
/*  46: 46 */    this.logger.log(Level.INFO, "charset = ----" + charset);
/*  47: 47 */    this.logger.log(Level.INFO, "fileDir = ----" + fileDir);
/*  48: 48 */    this.logger.log(Level.INFO, "threadNum = ----" + threadNum);
/*  49:    */    try
/*  50:    */    {
/*  51: 51 */      this.fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.lastIndexOf("?") > 0 ? urlStr.lastIndexOf("?") : urlStr.length());
/*  52: 52 */      if ("".equalsIgnoreCase(this.fileName)) {
/*  53: 53 */        this.fileName = UUID.randomUUID().toString();
/*  54:    */      }
/*  55:    */      
/*  56: 56 */      this.logger.log(Level.INFO, "fileName = ----" + this.fileName);
/*  57: 57 */      File file = new File(this.fileName);
/*  58:    */      
/*  59: 59 */      this.url = new URL(urlStr);
/*  60: 60 */      URLConnection con = this.url.openConnection();
/*  61: 61 */      setHeader(con);
/*  62:    */      
/*  63: 63 */      contentLength = con.getContentLength();
/*  64:    */      
/*  65: 65 */      this.threadLength = (contentLength / threadNum);
/*  66:    */      
/*  67: 67 */      this.logger.log(Level.INFO, "threadLength = ----" + this.threadLength);
/*  68:    */      
/*  70: 70 */      setThreadBreakpoint(fileDir, this.fileName, contentLength, startPos);
/*  71: 71 */      this.logger.log(Level.INFO, "startPos = ----" + startPos);
/*  72:    */      
/*  73: 73 */      ExecutorService exec = Executors.newCachedThreadPool();
/*  74: 74 */      for (int i = 0; i < threadNum; i++)
/*  75:    */      {
/*  76: 76 */        startPos[i] += this.threadLength * i;
/*  77:    */        
/*  80: 80 */        if (i == threadNum - 1) {
/*  81: 81 */          endPos = contentLength;
/*  82:    */        } else {
/*  83: 83 */          endPos = this.threadLength * (i + 1) - 1L;
/*  84:    */        }
/*  85:    */        
/*  86: 86 */        ChildThread thread = new ChildThread(this, latch, i, startPos[i], endPos);
/*  87: 87 */        this.childThreads[i] = thread;
/*  88: 88 */        exec.execute(thread);
/*  89:    */      }
/*  90:    */      
/*  94: 94 */      latch.await();
/*  95: 95 */      exec.shutdown();
/*  96:    */      
/*  98: 98 */      tempFileToTargetFile(this.childThreads);
/*  99:    */    }
/* 100:    */    catch (Exception e) {
/* 101:101 */      e.printStackTrace();
/* 102:    */    }
/* 103:    */  }
/* 104:    */  
/* 109:    */  private void setThreadBreakpoint(String fileDir, String fileName, long contentLength, long[] startPos)
/* 110:    */  {
/* 111:111 */    File file = new File(fileDir + fileName);
/* 112:112 */    long localFileSize = file.length();
/* 113:    */    
/* 114:114 */    if (file.exists())
/* 115:    */    {
/* 116:116 */      if (localFileSize == contentLength)
/* 117:    */      {
/* 118:118 */        this.logger.log(Level.INFO, "file " + fileName + " has exists,exe will exit!!");
/* 119:119 */        return;
/* 120:    */      }
/* 121:121 */      this.logger.log(Level.INFO, "Error in downloaded file, Now download continue ... ");
/* 122:    */      
/* 124:124 */      File tempFileDir = new File(fileDir);
/* 125:125 */      File[] files = tempFileDir.listFiles();
/* 126:126 */      for (int k = 0; k < files.length; k++) {
/* 127:127 */        String tempFileName = files[k].getName();
/* 128:    */        
/* 129:129 */        if ((tempFileName != null) && (files[k].length() > 0L) && 
/* 130:130 */          (tempFileName.startsWith(fileName + "_"))) {
/* 131:131 */          int fileLongNum = Integer.parseInt(tempFileName
/* 132:132 */            .substring(tempFileName.lastIndexOf("_") + 1, 
/* 133:133 */            tempFileName.lastIndexOf("_") + 2));
/* 134:    */          
/* 135:135 */          startPos[fileLongNum] = files[k].length();
/* 136:    */        }
/* 137:    */      }
/* 138:    */    }
/* 139:    */    else
/* 140:    */    {
/* 141:    */      try {
/* 142:142 */        file.createNewFile();
/* 143:    */      } catch (IOException e) {
/* 144:144 */        e.printStackTrace();
/* 145:    */      }
/* 146:    */    }
/* 147:    */  }
/* 148:    */  
/* 149:    */  private void tempFileToTargetFile(ChildThread[] childThreads) {
/* 150:    */    try {
/* 151:151 */      BufferedOutputStream outputStream = new BufferedOutputStream(
/* 152:152 */        new FileOutputStream(this.fileDir + this.fileName));
/* 153:    */      
/* 155:155 */      for (int i = 0; i < this.threadNum; i++) {
/* 156:156 */        if (this.statusError) {
/* 157:157 */          for (int k = 0; k < this.threadNum; k++) {
/* 158:158 */            if (childThreads[k].tempFile.length() == 0L)
/* 159:159 */              childThreads[k].tempFile.delete();
/* 160:    */          }
/* 161:161 */          this.logger.log(Level.INFO, "本次下载任务不成功，请重新设置线程数。");
/* 162:162 */          break;
/* 163:    */        }
/* 164:    */        
/* 165:165 */        BufferedInputStream inputStream = new BufferedInputStream(
/* 166:166 */          new FileInputStream(childThreads[i].tempFile));
/* 167:167 */        this.logger.log(Level.INFO, "Now is file " + childThreads[i].id);
/* 168:168 */        int len = 0;
/* 169:169 */        long count = 0L;
/* 170:170 */        byte[] b = new byte[1024];
/* 171:171 */        while ((len = inputStream.read(b)) != -1) {
/* 172:172 */          count += len;
/* 173:173 */          outputStream.write(b, 0, len);
/* 174:174 */          if (count % 4096L == 0L) {
/* 175:175 */            outputStream.flush();
/* 176:    */          }
/* 177:    */        }
/* 178:    */        
/* 181:181 */        inputStream.close();
/* 182:    */        
/* 183:183 */        if (childThreads[i].status == 1) {
/* 184:184 */          childThreads[i].tempFile.delete();
/* 185:    */        }
/* 186:    */      }
/* 187:    */      
/* 188:188 */      outputStream.flush();
/* 189:189 */      outputStream.close();
/* 190:    */    } catch (FileNotFoundException e) {
/* 191:191 */      e.printStackTrace();
/* 192:    */    } catch (IOException e) {
/* 193:193 */      e.printStackTrace();
/* 194:    */    }
/* 195:    */  }
/* 196:    */  
/* 199:    */  private void setHeader(URLConnection con)
/* 200:    */  {
/* 201:201 */    con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
/* 202:202 */    con.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
/* 203:203 */    con.setRequestProperty("Accept-Encoding", "aa");
/* 204:204 */    con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
/* 205:205 */    con.setRequestProperty("Keep-Alive", "300");
/* 206:206 */    con.setRequestProperty("Connection", "keep-alive");
/* 207:207 */    con.setRequestProperty("If-Modified-Since", "Fri, 02 Jan 2009 17:00:05 GMT");
/* 208:208 */    con.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
/* 209:209 */    con.setRequestProperty("Cache-Control", "max-age=0");
/* 210:210 */    con.setRequestProperty("Referer", "http://www.dianping.com");
/* 211:    */  }
/* 212:    */  
/* 213:    */  public class ChildThread
/* 214:    */    extends Thread
/* 215:    */  {
/* 216:    */    public static final int STATUS_HASNOT_FINISHED = 0;
/* 217:    */    public static final int STATUS_HAS_FINISHED = 1;
/* 218:    */    public static final int STATUS_HTTPSTATUS_ERROR = 2;
/* 219:    */    private NextMultiThreadDownLoad task;
/* 220:    */    private int id;
/* 221:    */    private long startPosition;
/* 222:    */    private long endPosition;
/* 223:    */    private final CountDownLatch latch;
/* 224:224 */    private File tempFile = null;
/* 225:    */    
/* 226:226 */    private int status = 0;
/* 227:    */    
/* 228:    */    public ChildThread(NextMultiThreadDownLoad task, CountDownLatch latch, int id, long startPos, long endPos)
/* 229:    */    {
/* 230:230 */      this.task = task;
/* 231:231 */      this.id = id;
/* 232:232 */      this.startPosition = startPos;
/* 233:233 */      this.endPosition = endPos;
/* 234:234 */      this.latch = latch;
/* 235:    */      try
/* 236:    */      {
/* 237:237 */        this.tempFile = new File(this.task.fileDir + this.task.fileName + "_" + id);
/* 238:238 */        if (!this.tempFile.exists()) {
/* 239:239 */          this.tempFile.createNewFile();
/* 240:    */        }
/* 241:    */      } catch (IOException e) {
/* 242:242 */        e.printStackTrace();
/* 243:    */      }
/* 244:    */    }
/* 245:    */    
/* 246:    */    public void run()
/* 247:    */    {
/* 248:248 */      NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " run ...");
/* 249:249 */      HttpURLConnection con = null;
/* 250:250 */      InputStream inputStream = null;
/* 251:251 */      BufferedOutputStream outputStream = null;
/* 252:252 */      long count = 0L;
/* 253:253 */      long threadDownloadLength = this.endPosition - this.startPosition;
/* 254:    */      try
/* 255:    */      {
/* 256:256 */        outputStream = new BufferedOutputStream(new FileOutputStream(this.tempFile.getPath(), true));
/* 257:    */      } catch (FileNotFoundException e2) {
/* 258:258 */        e2.printStackTrace();
/* 259:    */      }
/* 260:    */      
/* 261:261 */      for (int k = 0; k < 10; k++) {
/* 262:262 */        if (k > 0) {
/* 263:263 */          NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Now thread " + this.id + "is reconnect, start position is " + this.startPosition);
/* 264:    */        }
/* 265:    */        try {
/* 266:266 */          con = (HttpURLConnection)this.task.url.openConnection();
/* 267:267 */          NextMultiThreadDownLoad.this.setHeader(con);
/* 268:268 */          con.setAllowUserInteraction(true);
/* 269:    */          
/* 270:270 */          con.setConnectTimeout(10000);
/* 271:    */          
/* 272:272 */          con.setReadTimeout(10000);
/* 273:    */          
/* 274:274 */          if (this.startPosition < this.endPosition)
/* 275:    */          {
/* 276:276 */            con.setRequestProperty("Range", "bytes=" + this.startPosition + "-" + 
/* 277:277 */              this.endPosition);
/* 278:278 */            NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " startPosition is " + this.startPosition);
/* 279:279 */            NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " endPosition is " + this.endPosition);
/* 280:    */            
/* 283:283 */            if ((con.getResponseCode() != 200) && 
/* 284:284 */              (con.getResponseCode() != 206)) {
/* 285:285 */              NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + ": code = " + 
/* 286:286 */                con.getResponseCode() + ", status = " + 
/* 287:287 */                con.getResponseMessage());
/* 288:288 */              this.status = 2;
/* 289:289 */              this.task.statusError = true;
/* 290:290 */              outputStream.close();
/* 291:291 */              con.disconnect();
/* 292:292 */              NextMultiThreadDownLoad.this.logger.log(Level.INFO, "Thread " + this.id + " finished.");
/* 293:293 */              this.latch.countDown();
/* 294:294 */              break;
/* 295:    */            }
/* 296:    */            
/* 297:297 */            inputStream = con.getInputStream();
/* 298:    */            
/* 299:299 */            int len = 0;
/* 300:300 */            byte[] b = new byte[1024];
/* 301:301 */            while ((len = inputStream.read(b)) != -1) {
/* 302:302 */              outputStream.write(b, 0, len);
/* 303:303 */              count += len;
/* 304:304 */              this.startPosition += len;
/* 305:    */              
/* 306:306 */              if (count % 4096L == 0L) {
/* 307:307 */                outputStream.flush();
/* 308:    */              }
/* 309:    */            }
/* 310:    */            
/* 311:311 */            System.out.println("count is " + count);
/* 312:312 */            if (count >= threadDownloadLength) {
/* 313:313 */              this.status = 1;
/* 314:    */            }
/* 315:315 */            outputStream.flush();
/* 316:316 */            outputStream.close();
/* 317:317 */            inputStream.close();
/* 318:318 */            con.disconnect();
/* 319:    */          } else {
/* 320:320 */            this.status = 1;
/* 321:    */          }
/* 322:    */          
/* 323:323 */          System.out.println("Thread " + this.id + " finished.");
/* 324:324 */          this.latch.countDown();
/* 325:    */        }
/* 326:    */        catch (IOException e) {
/* 327:    */          try {
/* 328:328 */            outputStream.flush();
/* 329:329 */            TimeUnit.SECONDS.sleep(NextMultiThreadDownLoad.this.sleepSeconds);
/* 330:    */          } catch (InterruptedException e1) {
/* 331:331 */            e1.printStackTrace();
/* 332:    */          } catch (IOException e2) {
/* 333:333 */            e2.printStackTrace();
/* 334:    */          }
/* 335:    */        }
/* 336:    */      }
/* 337:    */    }
/* 338:    */  }
/* 339:    */  
/* 340:    */  static int getTotalLines(File file)
/* 341:    */    throws IOException
/* 342:    */  {
/* 343:343 */    InputStream is = new BufferedInputStream(new FileInputStream(file.getName()));
/* 344:344 */    byte[] c = new byte[1024];
/* 345:345 */    int count = 0;
/* 346:346 */    int readChars = 0;
/* 347:347 */    int i; for (; (readChars = is.read(c)) != -1; 
/* 348:348 */        i < readChars) { i = 0;continue;
/* 349:349 */      if (c[i] == 10) {
/* 350:350 */        count++;
/* 351:    */      }
/* 352:348 */      i++;
/* 353:    */    }
/* 354:    */    
/* 357:353 */    is.close();
/* 358:354 */    return count;
/* 359:    */  }
/* 360:    */}


/* Location:           D:\git\CHECK_DATA_FILES\bin\
 * Qualified Name:     com.asiainfo.checkdatafiles.resources.NextMultiThreadDownLoad
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */