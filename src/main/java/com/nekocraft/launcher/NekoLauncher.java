package com.nekocraft.launcher;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JTextArea;

public class NekoLauncher{
    public static LoginFrame mf;
    public static void main(String[] args){
        System.out.println("Woo Nekocraft Launcher!");
        mf=new LoginFrame();
        mf.setVisible(true);
    }
    private static JDialog exFrame;
    private static boolean exinit=false;
    private static JTextArea text;
    public static void handleException(Exception e){
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
        if(r==3){//我会说这里有个彩蛋吗
            exFrame.setTitle("Threeeeeee!Something went wrong!");
        }
        else{
            exFrame.setTitle("Yooooooooo!Something went wrong!");
        }
        exFrame.setResizable(false); 
        exFrame.setLocationRelativeTo(exFrame.getOwner());
        exFrame.addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e) {
         System.exit(0);   //退出程序
        }
        });
        text=new JTextArea();
        text.setLineWrap(true);
        text.setEditable(false);
        if(r==3){//我会说这里有个彩蛋吗
            text.append("Threeeeeee!Something happened to the Launcher!");
        }else{
            text.append("Yooooooooo!Something happened to the Launcher!\n");
        }
        text.append(e.toString()+"\n");
        exFrame.getContentPane().add(text);
        exFrame.setVisible(true);
        
        StackTraceElement[] ste=e.getStackTrace();
        int i;
        for(i=0;i<ste.length;i++){
            text.append(ste[i].toString()+"\n");
        }}
    }
}
