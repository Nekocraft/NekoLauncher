/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *
 * @author gjz010
 */
public class TransparentBackground extends JComponent implements ComponentListener,
  WindowFocusListener{ 
    private JFrame frame; 
    private Image background;

public TransparentBackground(JFrame frame) {
    this.frame = frame;
    this.addComponentListener(this);
    updateBackground();
}

public void updateBackground( ) {
    try {
        Robot rbt = new Robot( );
        Toolkit tk = Toolkit.getDefaultToolkit( );
        Dimension dim = tk.getScreenSize( );
        background = rbt.createScreenCapture(
        new Rectangle(0,0,(int)dim.getWidth( ),
                          (int)dim.getHeight( )));
    } catch (Exception ex) {
        System.out.println(ex.toString( ));
        ex.printStackTrace( );
    }
}
public void paintComponent(Graphics g) {
    Point pos = this.getLocationOnScreen( );
    Point offset = new Point(-pos.x,-pos.y);
    g.drawImage(background,offset.x,offset.y,null);
    try{
    g.drawImage(ImageIO.read(new File("/home/gjz010/NekoLauncher/src/main/resources/bg1.png")),0,0,null);
    }catch (Exception ex){}
}
 public void componentMoved(ComponentEvent e) {
  // TODO Auto-generated method stub
  System.out.println("moved");
  this.repaint();
 }

                     //窗口改变大小时
 public void componentResized(ComponentEvent e) {
  // TODO Auto-generated method stub
  System.out.println("resized");
  this.repaint();
 }

 public void componentShown(ComponentEvent e) {
  // TODO Auto-generated method stub
  System.out.println("shown");
 }

                     //窗口得到焦点后,用refresh()方法更新界面
                     //窗口失去焦点后,将其移出屏幕
 public void windowLostFocus(WindowEvent e) {
  frame.setLocation(-2000, -2000);
 }

    @Override
    public void componentHidden(ComponentEvent ce) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowGainedFocus(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}