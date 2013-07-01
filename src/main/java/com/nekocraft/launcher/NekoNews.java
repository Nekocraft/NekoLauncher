/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.util.*;
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
    public static List<NekoNews> fetchNews(){
        List<NekoNews> news=new ArrayList<>();
        return news;
    }
}
