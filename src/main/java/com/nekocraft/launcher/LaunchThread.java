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
        //问：Minecraft里什么东西最揪心?
        //答：META-INF!
        //问：什么东西更揪心?
        //答：New MinecraftStructure!
        File mc_ori=new File(StaticRes.BIN,"minecraft.jar");
        File sc_ori=new File(StaticRes.BIN,"spoutcraft.jar");
        File mc_nometa=new File(StaticRes.BIN,"minecraft-tmp.jar");
        File sc_nometa=new File(StaticRes.BIN,"spoutcraft-tmp.jar");
        LoginFrame.bar.setString("Copying minecraft.jar");
        FileUtil.copyFile(mc_ori.getAbsolutePath(), mc_nometa.getAbsolutePath());
        LoginFrame.bar.setString("Copying spoutcraft.jar");
        FileUtil.copyFile(sc_ori.getAbsolutePath(), sc_nometa.getAbsolutePath());
        try{
        removeMeta(mc_nometa);
        removeMeta(sc_nometa);
        }
        catch(Exception e){
            NekoLauncher.handleException(e);
        }
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
            System.exit(0);
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
    }
    private void setEnvironmentVariables(){
        Map<String,String> env=proc.environment();
        proc.directory(StaticRes.BIN);
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("win")){
            env.put("APPDATA", new File("").getAbsolutePath());
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("linux")){
            env.put("HOME", new File("").getAbsolutePath()); //喵的居然不行
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("mac")){
            env.put("HOME", new File("").getAbsolutePath());
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
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("win")){
            command.append("lwjgl.jar;lwjgl_util.jar;jinput.jar;spoutcraft-tmp.jar;minecraft-tmp.jar -Djava.library.path=natives/ net.minecraft.client.Minecraft ");
        }
        if(System.getProperty("os.name").replace(" ", "").toLowerCase().contains("linux")|System.getProperty("os.name").replace(" ", "").toLowerCase().contains("mac")){
            command.append("lwjgl.jar:lwjgl_util.jar:jinput.jar:spoutcraft-tmp.jar:minecraft-tmp.jar -Djava.library.path=natives/ net.minecraft.client.Minecraft ");
        }
        command.append(LoginFrame.lt.getUser());
        command.append(" ");
        command.append(LoginFrame.lt.getSession());
        return command.toString();
    }
    private void removeMeta(File f) throws Exception{ //META-INF什么的最讨厌了！
        StaticRes.deleteZipEntry(f,new String[]{"META-INF/MOJANG_C.SF","META-INF/MOJANG_C.DSA","META-INF/MANIFEST.MF","META-INF/maven/org.spoutcraft/spoutcraft/pom.xml","META-INF/maven/org.spoutcraft/spoutcraft/pom.properties"});
    }
}