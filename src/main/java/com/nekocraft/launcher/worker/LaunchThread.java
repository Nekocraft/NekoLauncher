/*
 * This File is a part of NekoLauncher
 * of Nekocraft
 */
package com.nekocraft.launcher.worker;

import java.io.File;
import java.util.*;

import com.nekocraft.launcher.LoginFrame;
import com.nekocraft.launcher.NekoLauncher;
import com.nekocraft.launcher.structure.Library;
import com.nekocraft.launcher.structure.MinecraftStructure;
import com.nekocraft.launcher.structure.NekoUser;
import com.nekocraft.launcher.utils.FileUtil;
import com.nekocraft.launcher.utils.Utils;

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
        File mc_ori=new File(Utils.BIN,"minecraft.jar");
        File sc_ori=new File(Utils.BIN,"spoutcraft.jar");
        File mc_nometa=new File(Utils.BIN,"minecraft-tmp.jar");
        File sc_nometa=new File(Utils.BIN,"spoutcraft-tmp.jar");
        LoginFrame.bar.setString("正在复制minecraft.jar...");
        FileUtil.copyFile(mc_ori.getAbsolutePath(), mc_nometa.getAbsolutePath());
        LoginFrame.bar.setString("正在复制spoutcraft.jar...");
        FileUtil.copyFile(sc_ori.getAbsolutePath(), sc_nometa.getAbsolutePath());
        try{
        LoginFrame.bar.setString("正在删除META-INF...");
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
            if (NekoLauncher.isLocal){
            System.exit(0);
            }else{
                LoginFrame.instance.reInit();
            }
        } catch (Exception ex) {
            NekoLauncher.handleException(ex);
        }
    }
    private void setEnvironmentVariables(){
        Map<String,String> env=proc.environment();
        proc.directory(Utils.BIN);
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
        StringBuilder command=new StringBuilder("\"");
        NekoUser su=NekoLauncher.du;
        command.append(new File(System.getProperty("java.home"),"bin/java.exe").getAbsolutePath());
        command.append("\" -Xms");
        command.append(su.getMinmem());
        command.append("M -Xms");
        command.append(su.getMaxmem());
        command.append("M -cp ");
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
            command.append("lwjgl.jar:lwjgl_util.jar:jinput.jar:spoutcraft-tmp.jar:minecraft-tmp.jar -Djava.library.path=natives/ -Duser.home=").append(new File("").getAbsolutePath()).append(" net.minecraft.client.Minecraft ");
        }
        command.append(NekoLauncher.lt.getUser());
        command.append(" ");
        command.append(NekoLauncher.lt.getSession());
        System.out.println(command.toString());
        return command.toString();
    }
    private void removeMeta(File f) throws Exception{ //META-INF什么的最讨厌了！
        Utils.deleteZipEntry(f,new String[]{"META-INF/MOJANG_C.SF","META-INF/MOJANG_C.DSA","META-INF/MANIFEST.MF","META-INF/maven/org.spoutcraft/spoutcraft/pom.xml","META-INF/maven/org.spoutcraft/spoutcraft/pom.properties"});
    }
}