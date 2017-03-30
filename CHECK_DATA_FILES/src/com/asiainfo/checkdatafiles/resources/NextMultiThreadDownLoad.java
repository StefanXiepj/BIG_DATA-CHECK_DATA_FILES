package com.asiainfo.checkdatafiles.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NextMultiThreadDownLoad {
	
	private int threadNum;
	private String fileName = null;
	private URL url = null;
	private int threadLines = 0;
	private ChildThread[] childThreads;
	private String fileDir = null;
	private boolean statusError = false;
	private int sleepSeconds = 5000;
	private long threadLength;
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public void download(String urlStr, String charset,String fileDir,int threadNum) {
        this.fileDir = fileDir;
        this.threadNum = threadNum;
        long contentLength = 0; 
        CountDownLatch latch = new CountDownLatch(threadNum);
        childThreads = new ChildThread[threadNum];
        long[] startPos = new long[threadNum];  
        long endPos = 0;  
        logger.log(Level.INFO, "charset = ----"+charset);
        logger.log(Level.INFO, "fileDir = ----"+fileDir);
        logger.log(Level.INFO, "threadNum = ----"+threadNum);
        try {  
            // ��url�л�����ص��ļ���ʽ������  
            this.fileName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.lastIndexOf("?")>0 ? urlStr.lastIndexOf("?") : urlStr.length());  
            if("".equalsIgnoreCase(this.fileName)){  
                this.fileName = UUID.randomUUID().toString();  
            }
            
            logger.log(Level.INFO, "fileName = ----"+fileName);
            File file = new File(fileName);
            //�����������
            this.url = new URL(urlStr);
            URLConnection con = url.openConnection();  
            setHeader(con);
            // �õ�content�ĳ���  
            contentLength = con.getContentLength();  
            // ��context��ΪthreadNum�εĻ���ÿ�εĳ��ȡ�  
            this.threadLength = contentLength / threadNum;
            
            logger.log(Level.INFO, "threadLength = ----"+threadLength);
              
            // ��һ�������������ص���ʱ�ļ������öϵ㣬������µ�������������Ŀ���ļ�.
            setThreadBreakpoint(fileDir, fileName, contentLength, startPos);  
            logger.log(Level.INFO, "startPos = ----"+startPos);
            //�ڶ������ֶ���߳������ļ�  
            ExecutorService exec = Executors.newCachedThreadPool();  
            for (int i = 0; i < threadNum; i++) { 
                // �������߳��������������ݣ�ÿ�����ݵ���ʼλ��Ϊ(threadLength * i + �����س���) 
                startPos[i] += threadLength * i;
  
                /*�������̵߳���ֹλ�ã������һ���̼߳�Ϊ(threadLength * (i + 1) - 1) 
                 * ���һ���̵߳���ֹλ�ü�Ϊ�������ݵĳ���*/  
                if (i == threadNum - 1) {  
                    endPos = contentLength;  
                } else {  
                    endPos = threadLength * (i + 1) - 1;  
                }
                // �������̣߳���ִ�С�  
                ChildThread thread = new ChildThread(this, latch, i, startPos[i], endPos);  
                childThreads[i] = thread;
                exec.execute(thread); 
                
            }
            
            
            // �ȴ�CountdownLatch�ź�Ϊ0����ʾ�������̶߳�������  
            latch.await();  
            exec.shutdown();  

            // ���������ѷֶ�������������ʱ�ļ��е�����д��Ŀ���ļ��С� 
            tempFileToTargetFile(childThreads);  
            
        }catch(Exception e){
        	e.printStackTrace();
        }
  
            
    }
        

      //��һ�������������ص���ʱ�ļ������öϵ㣬������µ�������������Ŀ���ļ���  
    private void setThreadBreakpoint(String fileDir, String fileName,  
            long contentLength, long[] startPos) {  
        File file = new File(fileDir + fileName);
        long localFileSize = file.length();  
      
        if (file.exists()) {  
            // ���ص�Ŀ���ļ��Ѵ��ڣ��ж�Ŀ���ļ��Ƿ����� ������������ж����ء��������������ʱ�ļ����ϵ����� 
            if (localFileSize == contentLength) { 
            	//����ļ����������������ж�����
            	logger.log(Level.INFO, "file " + fileName + " has exists,exe will exit!!");
            	return ;
            }else{
                logger.log(Level.INFO, "Error in downloaded file, Now download continue ... ");
      
                // ����Ŀ���ļ���������ʱ�ļ������öϵ��λ�ã���ÿ����ʱ�ļ��ĳ���  
                File tempFileDir = new File(fileDir);  
                File[] files = tempFileDir.listFiles();
                for (int k = 0; k < files.length; k++) {  
                    String tempFileName = files[k].getName();  
                    // ��ʱ�ļ���������ʽΪ��Ŀ���ļ���+"_"+���  
                    if (tempFileName != null && files[k].length() > 0  
                            && tempFileName.startsWith(fileName + "_")) {  
                        int fileLongNum = Integer.parseInt(tempFileName  
                                .substring(tempFileName.lastIndexOf("_") + 1,  
                                        tempFileName.lastIndexOf("_") + 2));  
                        // Ϊÿ���߳����������ص�λ��  
                        startPos[fileLongNum] = files[k].length();
                    }  
                }  
            }  
        } else {  
            // ������ص�Ŀ���ļ������ڣ��򴴽����ļ�  
            try {  
                file.createNewFile();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }
    
    private void tempFileToTargetFile(ChildThread[] childThreads) {  
        try {  
            BufferedOutputStream outputStream = new BufferedOutputStream(  
                    new FileOutputStream(fileDir + fileName)); 
      
            // �����������̴߳�������ʱ�ļ�����˳�����������д��Ŀ���ļ���  
            for (int i = 0; i < threadNum; i++) { 
                if (statusError) {
                    for (int k = 0; k < threadNum; k++) {  
                        if (childThreads[k].tempFile.length() == 0)  
                            childThreads[k].tempFile.delete();  
                    }
                    logger.log(Level.INFO, "�����������񲻳ɹ��������������߳�����");
                    break;  
                }  
      
                BufferedInputStream inputStream = new BufferedInputStream(  
                        new FileInputStream(childThreads[i].tempFile));  
                logger.log(Level.INFO, "Now is file " + childThreads[i].id);
                int len = 0;  
                long count = 0;  
                byte[] b = new byte[1024];  
                while ((len = inputStream.read(b)) != -1) {
                    count += len;  
                    outputStream.write(b, 0, len);  
                    if ((count % 4096) == 0) {  
                        outputStream.flush();  
                    }  
      
                    // b = new byte[1024];  
                }  
      
                inputStream.close();  
                // ɾ����ʱ�ļ�  
                if (childThreads[i].status == ChildThread.STATUS_HAS_FINISHED) {  
                    childThreads[i].tempFile.delete();  
                }  
            }  
      
            outputStream.flush();  
            outputStream.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
                
    
    
    //����ͷαװ
    private void setHeader(URLConnection con) {  
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");  
        con.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");  
        con.setRequestProperty("Accept-Encoding", "aa");  
        con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");  
        con.setRequestProperty("Keep-Alive", "300");  
        con.setRequestProperty("Connection", "keep-alive");  
        con.setRequestProperty("If-Modified-Since", "Fri, 02 Jan 2009 17:00:05 GMT");  
        con.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");  
        con.setRequestProperty("Cache-Control", "max-age=0");  
        con.setRequestProperty("Referer", "http://www.dianping.com");  
    }
    
    
    public class ChildThread extends Thread {  
        public static final int STATUS_HASNOT_FINISHED = 0;  
        public static final int STATUS_HAS_FINISHED = 1;  
        public static final int STATUS_HTTPSTATUS_ERROR = 2;  
        private NextMultiThreadDownLoad task;  
        private int id;  
        private long startPosition;  
        private long endPosition;  
        private final CountDownLatch latch;  
        // private RandomAccessFile tempFile = null;  
        private File tempFile = null;  
        //�߳�״̬��  
        private int status = ChildThread.STATUS_HASNOT_FINISHED;  
      
        public ChildThread(NextMultiThreadDownLoad task, CountDownLatch latch, int id, long startPos, long endPos) {  
            super();  
            this.task = task;  
            this.id = id;  
            this.startPosition = startPos;  
            this.endPosition = endPos;  
            this.latch = latch;  
      
            try {  
                tempFile = new File(this.task.fileDir + this.task.fileName + "_" + id);  
                if(!tempFile.exists()){  
                    tempFile.createNewFile();  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
      
        }  
      
        public void run() {  
            logger.log(Level.INFO, "Thread " + id + " run ...");
            HttpURLConnection con = null;  
            InputStream inputStream = null;  
            BufferedOutputStream outputStream = null;  
            long count = 0;   
            long threadDownloadLength = endPosition - startPosition;  
      
            try {  
                outputStream = new BufferedOutputStream(new FileOutputStream(tempFile.getPath(), true));  
            } catch (FileNotFoundException e2) {  
                e2.printStackTrace();  
            }  
              
            for(int k = 0; k < 10; k++){  
                if(k > 0)
                	logger.log(Level.INFO, "Now thread " + id + "is reconnect, start position is " + startPosition);
                try {  
                    //��URLConnection  
                    con = (HttpURLConnection) task.url.openConnection();  
                    setHeader(con);  
                    con.setAllowUserInteraction(true);  
                    //�������ӳ�ʱʱ��Ϊ10000ms  
                    con.setConnectTimeout(10000);  
                    //���ö�ȡ���ݳ�ʱʱ��Ϊ10000ms  
                    con.setReadTimeout(10000);  
                      
                    if(startPosition < endPosition){  
                        //�����������ݵ���ֹ����  
                        con.setRequestProperty("Range", "bytes=" + startPosition + "-"  
                                + endPosition);
                    	logger.log(Level.INFO, "Thread " + id + " startPosition is " + startPosition);
                    	logger.log(Level.INFO, "Thread " + id + " endPosition is " + endPosition);
      
                        //�ж�http status�Ƿ�ΪHTTP/1.1 206 Partial Content����200 OK  
                        //���������������״̬����status��ΪSTATUS_HTTPSTATUS_ERROR  
                        if (con.getResponseCode() != HttpURLConnection.HTTP_OK  
                                && con.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                        	logger.log(Level.INFO, "Thread " + id + ": code = "  
                                    + con.getResponseCode() + ", status = "  
                                    + con.getResponseMessage());
                            status = ChildThread.STATUS_HTTPSTATUS_ERROR;  
                            this.task.statusError = true;  
                            outputStream.close();  
                            con.disconnect();  
                            logger.log(Level.INFO, "Thread " + id + " finished.");
                            latch.countDown();  
                            break;  
                        }  
      
                        inputStream = con.getInputStream();  
      
                        int len = 0;  
                        byte[] b = new byte[1024];  
                        while ((len = inputStream.read(b)) != -1) {  
                            outputStream.write(b, 0, len);  
                            count += len;   
                            startPosition += len;                             
                            //ÿ����4096��byte��һ���ڴ�ҳ������������flushһ��  
                            if(count % 4096 == 0){  
                               outputStream.flush();  
                            }  
                        }  
      
                        System.out.println("count is " + count);   
                        if (count >= threadDownloadLength) {  
                            status = ChildThread.STATUS_HAS_FINISHED;  
                        }  
                        outputStream.flush();  
                        outputStream.close();  
                        inputStream.close();  
                        con.disconnect();  
                    } else {  
                        status = ChildThread.STATUS_HAS_FINISHED;  
                    }  
      
                    System.out.println("Thread " + id + " finished.");  
                    latch.countDown();  
                    break;  
                } catch (IOException e) {  
                    try {  
                       outputStream.flush();
                       TimeUnit.SECONDS.sleep(sleepSeconds);  
                    } catch (InterruptedException e1) {  
                        e1.printStackTrace();  
                    } catch (IOException e2) {  
                        e2.printStackTrace();  
                    }  
                    continue;  
                }                 
            }  
        }  
    } 
    
 // �ļ����ݵ��������� 
    static int getTotalLines(File file) throws IOException {
    	InputStream is = new BufferedInputStream(new FileInputStream(file.getName()));
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n')
                    ++count;
            }
        }
        is.close();
        return count;
    }

}