/*
 * This File is a part of NekoLauncher
 * of Nekocraft
 */
package com.nekocraft.launcher;

import java.io.*;
import java.util.zip.*;

/**
 *
 * @author gjz010
 */
public class StaticRes {
    //A Slash after URL!
    public static final String NEKO="http://nekocraft.com/";
    public static final String NEWSFEED=NEKO+"feed/";
    public static final String API="https://nekocraft.com/api/login/";
    //API usage : http://nekocraft.com/api/login/?user=---&password=---
    public static final String INFO_REPO="http://neko.gjz010.tk/";
    public static final String SCP_REPO="http://ci.nekocraft.com/job/Spoutcraft/$VERSION$/artifact/target/Spoutcraft.jar";
    public static final String MC_REPO="http://assets.minecraft.net/$VERSION$/minecraft.jar";
    public static final File MINECRAFT=new File(".minecraft/");
    public static final File CURRENT_XML=new File(".minecraft/current.xml");
    public static final File MIRRORS_XML=new File(".minecraft/mirrors.xml");
    public static final File BIN=new File(".minecraft/bin/");
    public static final File NATIVES=new File(".minecraft/bin/natives/");
    public static final File LIB=new File(".minecraft/bin/lib/");
    public static final File MODS=new File(".minecraft/mods/");
//Link:http://www.javaer.org/java/1-zip/3-delete-zipentry-from-zip-file
    public static void deleteZipEntry(File zipFile,
             String[] files) throws IOException {
               // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
               // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();
        tempFile.deleteOnExit();
        FileUtil.copyFile(zipFile.getAbsolutePath(), tempFile.getAbsolutePath());
        byte[] buf = new byte[1024];
         
        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
         
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean toBeDeleted = false;
            for (String f : files) {
                if (f.equals(name)) {
                    toBeDeleted = true;
                    break;
                }
            }
            if (!toBeDeleted) {
                // Add ZIP entry to output stream.
                zout.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    zout.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams       
        zin.close();
        // Compress the files
        // Complete the ZIP file
        zout.close();
        tempFile.delete();
    }

}
