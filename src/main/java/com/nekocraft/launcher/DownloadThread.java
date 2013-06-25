/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 *
 * @author gjz010
 */
public class DownloadThread extends Thread{
    MinecraftStructure mc;
    String current;
    @Override
    public void run(){
        fetchCurrentVersion();
        mc=new MinecraftStructure();
        try {
            parseXML();
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
    }
    public void fetchCurrentVersion(){ 
        LoginFrame.bar.setString("获取版本信息中...");
        FileWriter out = null;
        try {
            current=HTMLFetcher.getHTML(StaticRes.INFO_REPO+"current.xml", "UTF-8");
            out = new FileWriter(StaticRes.CURRENT_XML);
            BufferedWriter b=new BufferedWriter(out);
            b.write(current);
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
    public void updateGame(boolean isOld){
        if(!isOld){
            NekoLauncher.handleException(new Exception(){
                @Override
                public String getMessage(){
                    return "Unsupported Feature!";
                }
            });
        }///////////最烦1.6的结构了！
    }
    public void downloadFiles(String[] files){
    }
    public void parseXML() throws Exception{
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
        System.out.println(mc.toString());
    }
    public static void main(String args[]){
        DownloadThread dt=new DownloadThread();
        dt.run();
    }
}
