/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gjz010
 */
public class Mirror implements Comparable{
    private String name;
    private String spoutcraftjar;
    private String minecraftjar;
    private String librepo;
    private long delay;
    /**
     * @param spoutcraftjar the spoutcraftjar to set
     */
    public void setSpoutcraftjar(String spoutcraftjar) {
        this.spoutcraftjar = spoutcraftjar;
    }

    /**
     * @param minecraftjar the minecraftjar to set
     */
    public void setMinecraftjar(String minecraftjar) {
        this.minecraftjar = minecraftjar;
    }

    /**
     * @param librepo the librepo to set
     */
    public void setLibrepo(String librepo) {
        this.librepo = librepo;
    }
    public URL getMinecraftJar(String version){
        try {
            return new URL(minecraftjar.replace("$VERSION$",version.replace(".", "_")));
        } catch (MalformedURLException ex) {    
            return null;
        }
    }
    public URL getSpoutcraftJar(int version){
        try {
            return new URL(spoutcraftjar.replace("$VERSION$",Integer.toString(version)));
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    public URL getLibraryURL(Library lib){
        try {
            return new URL(librepo+lib.getName());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * @return the delay
     */
    public long getDelay() {
        setDelay();
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay() {
        try {
            System.out.println(InetAddress.getByName(new URL(librepo).getHost()).getHostAddress());
            long startMili=System.currentTimeMillis();
            InetAddress.getByName(new URL(librepo).getHost()).isReachable(10000);
            long endMili=System.currentTimeMillis();
            delay=endMili-startMili;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object t) {
        Mirror am=(Mirror)t;
        return new Long(this.getDelay()).compareTo(new Long(am.getDelay()));
    }
    public static void main(String[] args) throws Exception{
        Mirror m1=new Mirror();
        Mirror m2=new Mirror();
        m1.setMinecraftjar("http://assets.minecraft.net/$VERSION$/minecraft.jar");
        m2.setMinecraftjar("http://assets.minecraft.net/$VERSION$/minecraft.jar");
        m1.setSpoutcraftjar("http://ci.nekocraft.com/job/Spoutcraft/$VERSION$/artifact/target/Spoutcraft.jar");
        m2.setSpoutcraftjar("http://ci.nekocraft.com/job/Spoutcraft/$VERSION$/artifact/target/Spoutcraft.jar");
        m1.setLibrepo("https://raw.github.com/gjz010/neko-game-repo/gh-pages/");
        m2.setLibrepo("https://gitcafe.com/gjz010/neko-mirror/raw/master/");
        System.out.println(m1.getDelay());
        System.out.println(m2.getDelay());
    }
}
