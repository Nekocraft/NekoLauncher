/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.awt.BorderLayout;
import javax.swing.*;

/**
 *
 * @author gjz010
 */
public class TransparencyFrame extends JFrame{
    public static void main(String args[]){
    JFrame frame = new JFrame("Transparent Window");
    TransparentBackground bg = new TransparentBackground(frame);
    bg.setLayout(null);
    //JButton button = new JButton("This is a button");
    //bg.add("North",button);
   // JLabel label = new JLabel("");
    //label.setBounds(0,0,620,450);
    //label.setIcon(new ImageIcon("/home/gjz010/NekoLauncher/src/main/resources"));
    //bg.add(label);
    frame.getContentPane().add("Center",bg);
    frame.pack( );
    frame.setSize(650,500);
    frame.setLocationRelativeTo(frame.getParent());
    frame.show( );
    }
}
