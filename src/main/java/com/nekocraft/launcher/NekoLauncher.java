package com.nekocraft.launcher;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class NekoLauncher{
    public static void main(String[] args){
        System.out.println("Woo Nekocraft Launcher!");
        LoginFrame mf=new LoginFrame();
        mf.setVisible(true);
    }
    public static void handleException(Exception e){
        JDialog exFrame=new JDialog();
        exFrame.setModal(true);
        exFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        exFrame.setAlwaysOnTop(true);
        exFrame.setSize(new Dimension(400, 500)); 
        exFrame.setTitle("Whooooops!Something went wrong!");  
        exFrame.setResizable(false); 
        exFrame.setLocationRelativeTo(exFrame.getOwner());
        exFrame.addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e) {
         System.exit(0);   //这里可以根据需要设定，不一定非得system 退出
        }
        });
        JTextArea text=new JTextArea();
        text.setEditable(false);
        text.append("Whooooops!Something happened to the Launcher!\n");
        text.append(e.toString()+"\n");
        StackTraceElement[] ste=e.getStackTrace();
        int i;
        for(i=0;i<ste.length;i++){
            text.append(ste[i].toString()+"\n");
        }
        exFrame.getContentPane().add(text);
        exFrame.setVisible(true);
    }
}
