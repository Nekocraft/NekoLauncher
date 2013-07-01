/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 *
 * @author gjz010
 */
public class NekoNews {
    private String title;
    private String link;
    private String date;
    private String desc;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String toString(){
        StringBuilder sb=new StringBuilder("NekoNews\n");
        sb.append("Title:");
        sb.append(getTitle());
        sb.append("\n");
        sb.append("Link:");
        sb.append(getLink());
        sb.append("\n");
        sb.append("Date:");
        sb.append(getDate());
        sb.append("\n");
        sb.append("Description:");
        sb.append(getDesc());
        sb.append("\n");
        return sb.toString();
    }
    public static List<NekoNews> fetchNews() throws Exception{
        List<NekoNews> news=new ArrayList<>();
        String rss=HTMLFetcher.getHTML(StaticRes.NEWSFEED, "UTF-8");
            DOMParser parser=new DOMParser();
            parser.parse(new InputSource(new StringReader(rss)));
            Document doc=parser.getDocument();
            NodeList newsnodes=doc.getElementsByTagName("item");
            for(int i=0;i<newsnodes.getLength();i++){
                NekoNews tm=new NekoNews();
                for(int j=0;j<newsnodes.item(i).getChildNodes().getLength();j++){
                    
                    if(newsnodes.item(i).getChildNodes().item(j).getNodeName().equals("title")){
                        tm.setTitle(newsnodes.item(i).getChildNodes().item(j).getTextContent());
                    }
                    if(newsnodes.item(i).getChildNodes().item(j).getNodeName().equals("pubDate")){
                        tm.setDate(newsnodes.item(i).getChildNodes().item(j).getTextContent());
                        tm.setDate(tm.parseSpan());
                    }
                    if(newsnodes.item(i).getChildNodes().item(j).getNodeName().equals("link")){
                        tm.setLink(newsnodes.item(i).getChildNodes().item(j).getTextContent());
                    }
                    if(newsnodes.item(i).getChildNodes().item(j).getNodeName().equals("description")){
                        tm.setDesc(newsnodes.item(i).getChildNodes().item(j).getTextContent());
                    }
                }
                news.add(tm);
            }
        return news;
    }
    public static NekoNews fetchLatestNews(){
        try{
        return fetchNews().get(0);
        }catch(Exception e){return null;}
    }
    public String parseSpan(){
        StringBuilder span=new StringBuilder("");
        SimpleDateFormat df=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH);
        Date posttime;
        Date now=new Date();
        try{
        posttime=df.parse(getDate());
        }catch(Exception e){
            e.printStackTrace();
            posttime=new Date();}
        double mm=now.getTime()-posttime.getTime();
        double mmabs=Math.abs(mm);
        int j[] ={1000,60,60,24,7,30/7,365/12,12};
        String k[] = new String[]{"毫秒","秒","分","小时","天","周","月","年"};
        for(int i=0;i<=7;i++){
            if(mmabs<j[i]){
               span.append((int)mmabs);
               span.append(k[i]);
               break;
            }else{
                mmabs/=j[i];
            }
        }
        span.append(mm>0?"前":"后");
        return span.toString();
    }
    public static void main(String args[]) throws Exception{
        //NekoNews news=NekoNews.fetchLatestNews();
        //System.out.println(news.toString());
        for(NekoNews n:fetchNews()){
            System.out.println(n.toString());
        }
    }
}
