/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher.utils;

import com.sun.awt.AWTUtilities;
import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author gjz010
 */
public class TransparentFrame extends JFrame{
    private Rectangle drag;
    private Point m;
    public TransparentFrame(String title,Rectangle drag){
        super(title);
        m=new Point(0,0);
        setUndecorated(true);
        this.drag=drag;
        //dragPanel.setOpaque(false);
        AWTUtilities.setWindowOpaque(this, false);
        setResizable(false); 
        //AWTUtilities.setWindowOpacity(this,0.8f);
        //this.add(dragPanel);
        //this.setContentPane(dragPanel);
        JPanel mainp=new JPanel(){
            public void paintComponent(Graphics g){
              try{
                    BufferedImage bk=ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("bg1.png"));
                    Graphics2D gb=bk.createGraphics();
                   if(!((TransparentFrame)this.getRootPane().getParent()).getDrag().contains(m)){
                    gb.setBackground(new Color(0,0,0,0));
                    gb.clearRect((int)m.getX(),(int)m.getY(),1,1);
                    gb.setColor(Color.red);      
                 }
                  g.drawImage(bk,0,0,this);
              }catch (Exception e){e.printStackTrace();}
              super.paintComponent(g);
          }
        };
        mainp.setOpaque(false);
        this.setContentPane(mainp);
        this.getContentPane().addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseMoved(MouseEvent e){
                m=e.getPoint();
                ((JPanel)e.getSource()).repaint();
            }
        });
        ComponentMover cm=new ComponentMover(JFrame.class,this.getContentPane());
    }
    public static void main(String args[]){
    JFrame frame = new TransparentFrame("Transparent Window",new Rectangle(9,10,604,432));
    frame.setSize(620,450);
    frame.setLocationRelativeTo(frame.getParent());
    frame.show( );
    }

    /**
     * @return the drag
     */
    public Rectangle getDrag() {
        return drag;
    }

    /**
     * @param drag the drag to set
     */
    public void setDrag(Rectangle drag) {
        this.drag = drag;
    }
}
