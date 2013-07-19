//Link:http://blog.csdn.net/wangweijun125455/article/details/6070277
package com.nekocraft.launcher;
/*
  * java 多线程下载
  */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
public class MulThreadDownloaderService {
 /*
  * urlStr 资源地址
  */
    public int current;

    public MulThreadDownloaderService() {
        this.current = 20;
    }
 public void mulThreadDownloader(String urlStr,File file) throws Exception{
  URL url = new URL(urlStr);
  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
  conn.setRequestMethod("GET");
  conn.setRequestProperty("Accept-Encoding", "identity"); 
  
  // 由链接对象来获取文件大小
  int fileSize = conn.getContentLength();
  LoginFrame.bar.setMaximum(fileSize);
  LoginFrame.bar.setValue(0);
  conn.disconnect();
  // 创建本地随机访问文件, 
  RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
  randomAccessFile.setLength(fileSize);
  randomAccessFile.close();

  //线程数目
  int threadNum = current;
  int threadFileSize = fileSize / threadNum + 1;
  for (int i = 0; i < threadNum; i++) {
   RandomAccessFile threadFile = new RandomAccessFile(file, "rw");
   
   // 设置本地文件开始写入位置
   int startPosition = i * threadFileSize;
   //RandomAccessFile 这个类的好处就是可以设置file 的切点
   //就是说你可以在任意位置读取或写入 data
   threadFile.seek(startPosition);
   
   ThreadLoader tl = new ThreadLoader(url, startPosition, threadFile, threadFileSize, i+1,this);
   tl.start();

  }
 }
 
 private class ThreadLoader extends Thread{
  private int startPosition;
  private RandomAccessFile threadFile;
  private int threadFileSize;
  private int threadId;
  private URL url;
  private final MulThreadDownloaderService ms;
  public ThreadLoader(URL url , int startPosition, 
    RandomAccessFile threadFile, int threadFileSize, int threadId,MulThreadDownloaderService ms) {
   this.startPosition = startPosition;
   this.threadFile = threadFile;
   this.threadFileSize = threadFileSize;
   this.threadId = threadId;
   this.url = url;
   this.ms=ms;
  }

  @Override
  public void run() {
   try {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    // 设置远程文件的开始读入文件
    conn.setRequestProperty("Range", "bytes=" + startPosition + "-");
    
    InputStream inputStream = conn.getInputStream();
    byte buffer[] = new byte[1024];
    int location = -1;
    int totalSize = 0;
    while(totalSize <threadFileSize && (location = inputStream.read(buffer)) != -1){
     threadFile.write(buffer, 0, location);
     synchronized(LoginFrame.bar){
         LoginFrame.bar.setValue(LoginFrame.bar.getValue()+location);
     }
     totalSize = totalSize + location;
     System.out.println(totalSize);
    }
    threadFile.close();
    inputStream.close();
    conn.disconnect();
    System.out.println("ThreadLoader:"+threadId +" 线程下载完毕");
    synchronized(ms){
        ms.current-=1;
    }
   } catch (Exception e) {
       //NekoLauncher.handleException(e);
   }
  }
 }
}