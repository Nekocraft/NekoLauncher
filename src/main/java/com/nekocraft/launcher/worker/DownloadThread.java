/*
 * This File is a part of NekoLauncher
 * of Nekocraft
 */
package com.nekocraft.launcher.worker;

import com.nekocraft.launcher.LoginFrame;
import com.nekocraft.launcher.NekoLauncher;
import com.nekocraft.launcher.structure.Library;
import com.nekocraft.launcher.structure.MinecraftStructure;
import com.nekocraft.launcher.structure.Mirror;
import com.nekocraft.launcher.utils.FileDigest;
import com.nekocraft.launcher.utils.HTMLFetcher;
import com.nekocraft.launcher.utils.MulThreadDownloaderService;
import com.nekocraft.launcher.utils.Utils;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 *
 * @author gjz010
 */
public class DownloadThread extends Thread{
    private MinecraftStructure mc;
    private String current;
    private String cmirror;
    private List<Mirror> mirrors;
    @Override
    public void run(){
        fetchCurrentVersion();
        fetchMirrors();
        mc=new MinecraftStructure();
        try {
            parseXML();
            updateGame();
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
        NekoLauncher.launch=new LaunchThread(mc);
        NekoLauncher.launch.start();
    }
    private void fetchCurrentVersion(){ 
        LoginFrame.bar.setString("获取版本信息中...");
        FileWriter out = null;
        try {
            current=HTMLFetcher.getHTML(Utils.INFO_REPO+"current.xml", "UTF-8");
            out = new FileWriter(Utils.CURRENT_XML);
            BufferedWriter b=new BufferedWriter(out);
            b.write(current);
            b.flush();
        } catch (IOException ex) {
            NekoLauncher.handleException(ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                NekoLauncher.handleException(ex);
            }
        }
    }
    private void fetchMirrors(){
        try {
            mirrors=new ArrayList<>();
            LoginFrame.bar.setString("获取镜像列表中...");
            FileWriter out = null;
            try {
                cmirror=HTMLFetcher.getHTML(Utils.INFO_REPO+"mirrors.xml", "UTF-8");
                out = new FileWriter(Utils.MIRRORS_XML);
                BufferedWriter b=new BufferedWriter(out);
                b.write(cmirror);
                b.flush();
            } catch (IOException ex) {
                NekoLauncher.handleException(ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    NekoLauncher.handleException(ex);
                }
            }
            DOMParser parser=new DOMParser();
            parser.parse(new InputSource(new StringReader(cmirror)));
            Document doc=parser.getDocument();
            NodeList mirrornodes=doc.getElementsByTagName("mirror");
            for(int i=0;i<mirrornodes.getLength();i++){
              Mirror tempm=new Mirror();
                for(int j=0;j<mirrornodes.item(i).getAttributes().getLength();j++){
                    if(mirrornodes.item(i).getAttributes().item(j).getNodeName().equals("name")){
                        tempm.setName(mirrornodes.item(i).getAttributes().item(j).getNodeValue());
                    }
                    if(mirrornodes.item(i).getAttributes().item(j).getNodeName().equals("spout")){
                        tempm.setSpoutcraftjar(mirrornodes.item(i).getAttributes().item(j).getNodeValue());
                    }
                    if(mirrornodes.item(i).getAttributes().item(j).getNodeName().equals("minecraft")){
                        tempm.setMinecraftjar(mirrornodes.item(i).getAttributes().item(j).getNodeValue());
                    }
                    if(mirrornodes.item(i).getAttributes().item(j).getNodeName().equals("librepo")){
                        tempm.setLibrepo(mirrornodes.item(i).getAttributes().item(j).getNodeValue());
                    }
                }
                mirrors.add(tempm);
            }
            Collections.sort(mirrors);
            for(Mirror m:mirrors){
                System.out.println(m.getName());
            }
        } catch (SAXException ex) {
            NekoLauncher.handleException(ex);
        } catch (IOException ex) {
            NekoLauncher.handleException(ex);
        }
    }
    private void updateGame()throws Exception{ 
        ///////////最烦1.6的结构了！ 在这里只写旧版本的更新。
        for (Library l:mc.getJars()){
            updateFile(l,0);
        }
        for (Library l:mc.getNatives()){
            updateFile(l,1);
        }
        for (Library l:mc.getLibs()){
            updateFile(l,2);
        }
        for (Library l:mc.getMods()){
            updateFile(l,3);
        }
    }
    private void updateFile(Library lib,int type) throws Exception{//////type值:0-jar 1-native 2-lib 3-mod
        LoginFrame.bar.setString("正在更新 "+lib.getName()+"...");
        LoginFrame.bar.setValue(0);
        File f=getFile(lib,type);
        String fmd5=FileDigest.getFileMD5(f);
        if(!lib.getMd5().equals(fmd5)){
            downloadFile(lib,f,type);
        }
    }
    private void downloadFile(Library lib,File target,int type){
        
        //for(int t=0;t<=3;t++){
            /*
        int bytesum = 0;
        int byteread = 0;
        URL u=new URL(url);
        HttpURLConnection con=(HttpURLConnection)u.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestMethod("GET");
        LoginFrame.bar.setMaximum(con.getContentLength());
        InputStream in=con.getInputStream();
        FileOutputStream out=new FileOutputStream(target);
        byte[] buffer = new byte[1024];
        while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                LoginFrame.bar.setValue(bytesum);
                out.write(buffer, 0, byteread);
        }
        out.flush();
        out.close();*/
        if(type==1){
            if(!System.getProperty("os.name").replace(" ", "").toLowerCase().contains(lib.getOs())){
                return;
            }
        }
        for(Mirror m:mirrors){
        StringBuilder url=new StringBuilder();
        if(lib.getName().equals("minecraft.jar")) {
                url.append(m.getMinecraftJar(mc.getMcversion()));
            }else
       if(lib.getName().equals("spoutcraft.jar")) {
                url.append(m.getSpoutcraftJar(mc.getScversion()));
            }else{
           url.append(m.getLibraryURL(lib));
       }
        for(int t=0;t<=3;t++){
        System.out.println(url.toString());
        MulThreadDownloaderService mds=new MulThreadDownloaderService();
                try {
                    mds.mulThreadDownloader(url.toString(), target);
                } catch (Exception ex) {
                    break;
                }
        while(mds.current!=0){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    NekoLauncher.handleException(ex);
                }
        }
            System.out.println("Required:"+lib.getMd5());
            System.out.println("Found:"+FileDigest.getFileMD5(target));    
        if(lib.getMd5().equals(FileDigest.getFileMD5(target))){
            System.out.println("完成");
            if(type==1){
                try {
                    unzipNative(target);
                } catch (Exception ex) {
                    NekoLauncher.handleException(ex);
                }
            }
            return;
        }else{
            System.out.println("Delete:"+target.delete());
            System.out.println("Exist:"+target.exists());
        }
        }
        }
      //  }
            NekoLauncher.handleException(
        new Exception(){
                @Override
                public String getMessage(){
                    return "Download Failed because MD5 verify not passed!";
                }
            });
        }
    private void unzipNative(File target)throws Exception{
        ZipFile zip=new ZipFile(target);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(target), Charset.forName("UTF-8"));
        ZipEntry entry=null;
        while((entry=zis.getNextEntry())!=null){
            String filename=entry.getName();
            File temp=new File(Utils.NATIVES,filename);
            OutputStream os = new FileOutputStream(temp);
            InputStream is = zip.getInputStream(entry);
            int len = 0;
            while((len=is.read())!=-1){
                os.write(len);
            }
            os.flush();
            os.close();
            is.close();
            
        }
        }
    private File getFile(Library lib,int type){
        //////type值:0-jar 1-native 2-lib 3-mod
        switch(type){
            case 0:
                return new File(Utils.BIN,lib.getName());//jar
            case 1:
                return new File(Utils.NATIVES,lib.getName());//native
            case 2:
                return new File(Utils.LIB,lib.getName());//lib
            case 3:
                return new File(Utils.MODS,lib.getName());//mod
        }
        return new File(Utils.MINECRAFT,lib.getName());
    }
    private void parseXML() throws Exception{
        DOMParser parser=new DOMParser();
        parser.parse(new InputSource(new StringReader(current)));
        Document doc=parser.getDocument();
        mc.setMcversion(doc.getElementsByTagName("mc").item(0).getTextContent());
        mc.setScversion(Integer.parseInt(doc.getElementsByTagName("scp").item(0).getTextContent()));
        NodeList files=doc.getElementsByTagName("jar");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addJar(jar);
        }
        files=doc.getElementsByTagName("native");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("os")){
                    jar.setOs(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addNative(jar);
        }
        files=doc.getElementsByTagName("lib");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addLib(jar);
        }
        files=doc.getElementsByTagName("mod");
        for(int i=0;i<files.getLength();i++){
          Library jar=new Library();
            for(int j=0;j<files.item(i).getAttributes().getLength();j++){
                if(files.item(i).getAttributes().item(j).getNodeName().equals("name")){
                    jar.setName(files.item(i).getAttributes().item(j).getNodeValue());
                }
                if(files.item(i).getAttributes().item(j).getNodeName().equals("md5")){
                    jar.setMd5(files.item(i).getAttributes().item(j).getNodeValue());
                }
            }
            mc.addMod(jar);
        }
        
        
        System.out.println(mc.toString());
    }

}
