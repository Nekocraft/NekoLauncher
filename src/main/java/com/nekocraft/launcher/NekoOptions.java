/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class NekoOptions {
    private long maxmem;
    private long minmem;
    private String jrelocation;
    private ArrayList<String> params;
    private int index;
    public NekoOptions(){
        maxmem=1024;
        minmem=1024;
        jrelocation=new File(System.getProperty("java.home"),"bin/java.exe").getAbsolutePath();
        params=new ArrayList<>();
        index=0;
    }
    public int addParam(String param){
        getParams().add(index, param);
        return index++;
    }
    public String getParam(int id){
        return getParams().get(id);
    }
    public static void main(String args[]){
        System.out.println(new File(System.getProperty("java.home"),"bin/java.exe"));
        NekoOptions o=new NekoOptions();
        int a=o.addParam("hello");
        int b=o.addParam("world");
        System.out.println(o.getParam(a));
        System.out.println(o.getParam(b));
    }

    /**
     * @return the maxmem
     */
    public long getMaxmem() {
        return maxmem;
    }

    /**
     * @param maxmem the maxmem to set
     */
    public void setMaxmem(long maxmem) {
        this.maxmem = maxmem;
    }

    /**
     * @return the minmem
     */
    public long getMinmem() {
        return minmem;
    }

    /**
     * @param minmem the minmem to set
     */
    public void setMinmem(long minmem) {
        this.minmem = minmem;
    }
    public void loadOptions(File source){
        Properties options=new Properties();
        options.setProperty("maxmemory",Long.toString(maxmem));
        options.setProperty("minmemory",Long.toString(minmem));
        StringBuilder par=new StringBuilder("");
        for(String s:getParams()){
            par.append(s);
            par.append("\n");
            
        }
        options.setProperty("params",Long.toString(minmem));
    }

    /**
     * @return the params
     */
    public ArrayList<String> getParams() {
        return params;
    }
}
