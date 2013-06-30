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

public class NekoLauncher extends JFrame{
    public static LoginFrame mf;
    public static boolean isLocal=false;
    
    //Threads
    public static LaunchThread launch;
    public static LoginThread lt;
    public static DownloadThread dt;
    public NekoLauncher(){
        this.setTitle("Nekocraft Launcher");  
        setResizable(false); 
        setLocationRelativeTo(getOwner());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.add(mf);
    }
    public static void main(String[] args){
        System.out.println("Woo Nekocraft Launcher!");
        isLocal=true;
        mf=new LoginFrame();
        JFrame jf=new JFrame();
        jf.setContentPane(mf);
        jf.setTitle("Nekocraft Launcher");  
        jf.setResizable(false); 
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mf.init();
        mf.start();
        jf.setSize(mf.getSize());
        jf.setLocationRelativeTo(jf.getOwner());
        jf.setVisible(true);
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
        if(r==2){
            exFrame.setTitle("Yooooooooo!You sold your soul to Lucifer!");
        }
        else{
            exFrame.setTitle("Whoooooops!Something went wrong!");
        }
        exFrame.setResizable(false); 
        exFrame.setLocationRelativeTo(exFrame.getOwner());
        exFrame.addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e) {
             exinit=false;
         if(isLocal){
         System.exit(0);//退出程序
         }
         else{
             //exFrame.dispose();
             //mf.reInit();
             mf.ref=true;
             try{
                 mf.getAppletContext().showDocument(new URL("http://nekocraft.com"));
             }catch(Exception ignore){
             ignore.printStackTrace();
             };
         }
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
        if(!dir.exists()){
            dir.mkdir();
        }
    }
    
}
