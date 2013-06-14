/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import javax.swing.*;

/**
 *
 * @author Administrator
 */
public class LabelTextField extends JSplitPane{
    private JTextField text;
    private JLabel label;
    public LabelTextField(String label,boolean password){
        this.label=new JLabel(label);
        if(password){
            this.text=new JPasswordField();
        }
        else{
        this.text=new JTextField();
        }
        this.setLeftComponent(this.label);
        this.setRightComponent(this.text);
    }
    public LabelTextField(String label){
        this(label,false);
    }
    public String getTextContent(){
        return text.getText();
    }
}
