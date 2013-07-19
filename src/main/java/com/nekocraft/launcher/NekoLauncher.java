package com.nekocraft.launcher;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class NekoLauncher{
    public static LoginFrame mf;
    public static boolean isLocal=false;
    
    //Threads
    public static LaunchThread launch;
    public static LoginThread lt;
    public static DownloadThread dt;
    public static NekoUser du;
    public static String[] runargs;
    public NekoLauncher(){
    }
    public static void init(){
      try{
        NekoLauncher.initDir(Utils.MINECRAFT);
        NekoLauncher.initDir(Utils.BIN);
        NekoLauncher.initDir(Utils.LIB);
        NekoLauncher.initDir(Utils.NATIVES);
        NekoLauncher.initDir(Utils.MODS);
        if(!new File(".minecraft/options.txt").exists()){
        FileUtil.createFile(new File(".minecraft/options.txt").getAbsolutePath(),"lang:zh_CN");
        }
        if(!Utils.USERDATA.exists()){
        FileUtil.createFile(Utils.USERDATA.getAbsolutePath(),"");
        }
        du=new NekoUser();
        du.loadUser(Utils.USERDATA);
        du.saveUser(Utils.USERDATA,true);
        }
        catch(Exception ex){
            NekoLauncher.handleException(ex);
        }
    }
    public static void main(String[] args){
        System.out.println("Woo Nekocraft Launcher!");
        init();
            String laf = UIManager.getSystemLookAndFeelClassName();
            System.out.println(laf);
    try {
         UIManager.setLookAndFeel(laf);
    } catch (UnsupportedLookAndFeelException exc) {
     System.err.println("Warning: UnsupportedLookAndFeel: " + laf);
    } catch (Exception exc) {
     System.err.println("Error loading " + laf + ": " + exc);
    }
        runargs=args;
        mf=new LoginFrame();
    }
    private static JDialog exFrame;
    private static boolean exinit=false;
    private static JTextArea text;
    public static void handleException(Exception e){
        try{
        launch.suspend();
        lt.suspend();
        dt.suspend();
        }catch(Exception ignore){}
        Logger.getLogger(NekoLauncher.class.getName()).log(Level.SEVERE, null, e);
        if (!exinit){
        exinit=true;
        exFrame=new JDialog();
        exFrame.setModal(true);
        exFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        exFrame.setAlwaysOnTop(true);
        exFrame.setSize(new Dimension(400, 500)); 
        Random random = new Random();//我会说这里有个彩蛋吗
        int r=random.nextInt(10);
        switch(r){
            case 2:
                exFrame.setTitle("Yooooooooo!You sold your soul to Lucifer!");
                break;
            case 3:
                exFrame.setTitle("Threeeeeee!You sold your soul to Lucifer!");
                break;
            case 5:
                exFrame.setTitle("Aieeeeeeee!Something went wrong!");
                break;
            default:
                exFrame.setTitle("Whoooooops!Something went wrong!");
                break;
        }
        //好吧我有点过于无聊了
        exFrame.setResizable(false); 
        exFrame.setLocationRelativeTo(exFrame.getOwner());
        exFrame.addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e) {
         exinit=false;
         System.exit(0);//退出程序
        }
        });
        text=new JTextArea();
        text.setLineWrap(true);
        text.setEditable(false);
        if(r==2){
            text.append("Yooooooooo!Something happened to the Launcher!\n");
        }else{
            text.append("Whoooooops!Something happened to the Launcher!\n");
        }
        text.append(e.toString()+"\n");
        
        StackTraceElement[] ste=e.getStackTrace();
        int i;
        for(i=0;i<ste.length;i++){
            text.append("	at ");
            text.append(ste[i].toString()+"\n");
        }
        exFrame.getContentPane().add(text);
        exFrame.setVisible(true);}
    }
    public static void initDir(File dir) throws Exception{
        System.out.println("Dir:"+dir.getName()+" "+dir.exists());
        if(!dir.exists()){
            System.out.println("Mkdir:"+dir.getName()+" "+dir.mkdir());
        }
    }
}
