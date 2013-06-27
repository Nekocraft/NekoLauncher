/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.io.File;
import java.util.*;

/**
 *
 * @author gjz010
 */
public class LaunchThread extends Thread{
    private MinecraftStructure mc;
    private ProcessBuilder proc;
    public LaunchThread(MinecraftStructure mc){
        this.mc=mc;
        proc=new ProcessBuilder();
        List<String> commandlist=new ArrayList<String>();
        commandlist.addAll(Arrays.asList(buildCommand().split(" ")));
        proc.command(commandlist);
        setEnvironmentVariables();
    }
    @Override
    public void run() {
        try {
            //Thanks to Indeed!
            Process p=proc.start();
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
    }
    private void setEnvironmentVariables(){
        Map<String,String> env=proc.environment();
        proc.directory(StaticRes.BIN);
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("win")){
            env.put("APPDATA", new File("./").getAbsolutePath());
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("linux")){
            env.put("HOME", new File("./").getAbsolutePath());
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("mac")){
            env.put("HOME", new File("./").getAbsolutePath());
        }
    }
    private String buildCommand(){
        StringBuilder command=new StringBuilder("java -Xmx1G -cp ");
        for(Library l:mc.getLibs()){
            command.append("lib/");
            command.append(l.getName());
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("win")){
            command.append(";");
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("linux")|System.getProperty("os.name").replace(" ", "").toLowerCase().contains("mac")){
            command.append(":");
        }
            command.append(";");
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("win")){
            command.append("lwjgl.jar;lwjgl_util.jar;jinput.jar;spoutcraft.jar;minecraft.jar -Djava.library.path=natives/ net.minecraft.client.Minecraft ");
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("linux")|System.getProperty("os.name").replace(" ", "").toLowerCase().contains("mac")){
            command.append("lwjgl.jar:lwjgl_util.jar:jinput.jar:spoutcraft.jar:minecraft.jar -Djava.library.path=natives/ net.minecraft.client.Minecraft ");
        }
        command.append(LoginFrame.lt.getUser());
        command.append(" ");
        command.append(LoginFrame.lt.getSession());
        return command.toString();
    }
}