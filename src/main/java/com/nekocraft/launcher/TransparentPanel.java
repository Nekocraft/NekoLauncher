/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.nekocraft.launcher.utils.ComponentMover;

/**
 *
 * @author Administrator
 */
public class TransparentPanel extends JPanel{
    public TransparentPanel(){
        super();
        this.setLayout(null);
        this.setOpaque(false);
        this.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                System.out.println("click!");
                //GraphicsEnvironment env =GraphicsEnvironment.getLocalGraphicsEnvironment();
                //((JFrame)((JPanel)e.getSource()).getParent().getParent().getParent()).;
            }
        });
        ComponentMover cm=new ComponentMover(JFrame.class,this);
    }
    @Override
    public void paintComponent (Graphics g){   
            super.paintComponent(g);  
            try{
            g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("bg1.png")),0,0,this); 
            }
            catch(Exception ignore){
                ignore.printStackTrace();
            }
            //
     }
    
}
