/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.util.List;

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
    private List<Library> textures;
    private List<Library> mods;

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
     * @return the textures
     */
    public List<Library> getTextures() {
        return textures;
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
    public void addTexture(Library l){
        textures.add(l);
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
}
class Library{
    private String name;
    private String md5;
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
}
class Native extends Library{
    private String os;

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