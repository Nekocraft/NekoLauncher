/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;

import java.awt.Graphics;
import javax.imageio.ImageIO;
import javax.swing.JProgressBar;

/**
 *
 * @author gjz010
 */
public class GradientProgressBar extends JProgressBar{
    private String image;
    @Override
           protected void paintComponent(Graphics g) {
        System.out.println((float)this.getValue()/(this.getMaximum()-this.getMinimum()));
                // TODO Auto-generated method stub
                //super.paintComponent(g);//yps62hq71w4z
                try{
                //g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream(getImage())),(this.getValue()/(this.getMaximum()-this.getMinimum()))*this.getWidth(),this.getHeight(), this);
                g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream(getImage())),0,0,(int)(((float)this.getValue()/(this.getMaximum()-this.getMinimum()))*this.getWidth()),10,this);
                g.drawString(this.getString(),this.getWidth()/2,10);
                }
                catch (Exception e){
                    NekoLauncher.handleException(e);
                }
            }

    /**
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(String image) {
        this.image = image;
    }
}
