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
public class MinecraftStructure {  ///For Notch's Structure
    private String mcversion;
    private int scversion;
    private List<Library> jars;
    private List<Library> natives;
    private List<Library> libs;
    private List<Library> mods;
    public MinecraftStructure(){
        jars=new ArrayList<Library>();
        natives=new ArrayList<Library>();
        libs=new ArrayList<Library>();
        mods=new ArrayList<Library>();
    }
    /**
     * @return the jars
     */
    public List<Library> getJars() {
        return jars;
    }

    /**
     * @return the natives
     */
    public List<Library> getNatives() {
        return natives;
    }

    /**
     * @return the libs
     */
    public List<Library> getLibs() {
        return libs;
    }

    /**
     * @return the mods
     */
    public List<Library> getMods() {
        return mods;
    }
    public void addJar(Library l){
        jars.add(l);
    }
    public void addLib(Library l){
        libs.add(l);
    }
    public void addMod(Library l){
        mods.add(l);
    }
    public void addNative(Library l){
        natives.add(l);
    }

    /**
     * @return the mcversion
     */
    public String getMcversion() {
        return mcversion;
    }

    /**
     * @return the scversion
     */
    public int getScversion() {
        return scversion;
    }

    /**
     * @param mcversion the mcversion to set
     */
    public void setMcversion(String mcversion) {
        this.mcversion = mcversion;
    }

    /**
     * @param scversion the scversion to set
     */
    public void setScversion(int scversion) {
        this.scversion = scversion;
    }
    public String toString(){
        StringBuilder str=new StringBuilder("MinecraftStructure");
        str.append("MC:");
        str.append(mcversion);
        str.append("SC:");
        str.append(scversion);
        str.append("Libraries:");
        str.append("Jars:");
        for(Library l:jars){
            str.append(l.getName());
        }
        str.append("Natives:");
        for(Library l:natives){
            str.append(l.getName());
        }
        str.append("Libs:");
        for(Library l:libs){
            str.append(l.getName());
        }
        str.append("Mods:");
        for(Library l:mods){
            str.append(l.getName());
        }
        return str.toString();
    }
}
class Library{
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