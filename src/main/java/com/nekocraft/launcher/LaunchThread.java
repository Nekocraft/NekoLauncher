/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.applet.*;
import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author gjz010
 */
public class LaunchThread extends URLClassLoader implements Runnable{
    private MinecraftStructure mc;
    Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    public LaunchThread(ClassLoader cl,MinecraftStructure mc){
        super(new URL[0],cl);
        this.mc=mc;
        try{
        loadLibraries();
        }catch(Exception e){
            NekoLauncher.handleException(e);
        }
    }
    @Override
    public void run() {
        try {
            Class<?> c=this.loadClass("net.minecraft.client.MinecraftApplet");
            //Applet mcapp=(Applet)c.newInstance();
            //yps62hq71w4z
            JFrame frame=new JFrame();
            frame.setTitle("Nekocraft");
            frame.setSize(600, 300);
            Applet mc=(Applet)c.newInstance();
            MinecraftAppletEnglober stub=new MinecraftAppletEnglober();
            stub.setMinecraftApplet(mc);
            mc.setStub(stub);
            frame.getContentPane().add(stub);
            //stub.setMinecraftApplet(mcapp);
            stub.addParameter("username", LoginFrame.lt.getUser());
            stub.addParameter("sessionid", LoginFrame.lt.getSession());
            stub.addParameter("portable", "true");
            String nativesPath = new File(".minecraft/bin/natives/").getAbsolutePath();
            System.setProperty("org.lwjgl.librarypath", nativesPath);
            System.setProperty("net.java.games.input.librarypath", nativesPath);
            System.setProperty("org.lwjgl.util.Debug", "false");
            System.setProperty("org.lwjgl.util.NoChecks", "false");
            stub.init();
            stub.setSize(frame.getWidth(),frame.getHeight());
            stub.start();
            frame.setVisible(true);
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
    }
    private void loadLibraries() throws Exception {
        //load libraries
        for(Library l:mc.getLibs()){
            loadLib(new File(".minecraft/bin/lib/"+l.getName()));
        }
        //load minecraft
        loadLib(new File(".minecraft/bin/minecraft.jar"));
        //load lwjgl
        loadLib(new File(".minecraft/bin/lwjgl.jar"));
        loadLib(new File(".minecraft/bin/lwjgl_util.jar"));
        loadLib(new File(".minecraft/bin/jinput.jar"));
        //load spoutcraft
        loadLib(new File(".minecraft/bin/spoutcraft.jar"));
    }
    private void loadLib(File lib) throws Exception{
        this.addURL(lib.toURI().toURL());
        //JarFile jar=new JarFile(lib);
        //Enumeration<JarEntry> es =jar.entries();
        //while(es.hasMoreElements()){
        //    JarEntry entry=(JarEntry)es.nextElement();
        //    String name=entry.getName();
        //    if(name!=null && name.endsWith(".class")){
         //       System.out.println(name.replace("/", ".").substring(0,name.length()-6));
         //       Class<?> c = this.loadClass(name.replace("/", ".").substring(0,name.length()-6));
         //       System.out.println(c);
        //        classes.add(c);
        //    }
       // }
    }
}