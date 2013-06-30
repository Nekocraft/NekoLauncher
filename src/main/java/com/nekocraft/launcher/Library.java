/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

/**
 *
 * @author gjz010
 */
public class Library{
    private String name;
    private String md5;
    private String os;
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        return md5;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param md5 the md5 to set
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(String os) {
        this.os = os;
    }
}