/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.*;
import org.w3c.dom.Document;
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
        System.out.println(doc.getElementsByTagName("mc").item(0).getTextContent());
    }
    public static void main(String args[]){
        DownloadThread dt=new DownloadThread();
        dt.run();
    }
}
