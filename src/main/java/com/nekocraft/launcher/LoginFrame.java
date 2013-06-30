package com.nekocraft.launcher;
import java.awt.*;  
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;  
public class LoginFrame extends JApplet{
    public static JProgressBar bar=new JProgressBar();
    public static JButton login=new JButton("登录");
    public static LabelTextField user,pass;
    public static LoginFrame instance;
    public boolean ref;
    public boolean needRefresh(){
        return ref;
    }
    public LoginFrame(){
        ref=false;
        instance=this;
        ////////窗体逻辑
        this.setSize(new Dimension(400, 500));  
        //this.setTitle("Nekocraft Launcher");  
        //setResizable(false); 
        //setLocationRelativeTo(getOwner());
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(null);
        try{
        initPic();
        }
        catch (Exception e){
            NekoLauncher.handleException(e);
        }
        ////////文本框逻辑
        user=new LabelTextField("用户名");
        pass=new LabelTextField("密　码",true);
        user.setBounds(100, 280, 200, 25);
        pass.setBounds(100,330,200,25);
        this.getContentPane().add(user);
        this.getContentPane().add(pass);
        ////////按钮逻辑
        login.setBounds(160,400,80,30);
        login.setEnabled(true);
        this.getContentPane().add(login);
        login.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                login.setEnabled(false);
                NekoLauncher.lt=new LoginThread();
                NekoLauncher.lt.setUser(user.getTextContent());
                NekoLauncher.lt.setPassword(pass.getTextContent());
                NekoLauncher.lt.start();
            }
            
        });
        ////////进度条逻辑
        bar.setBounds(50,230, 300, 25);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setString("请登录");
        this.getContentPane().add(bar);
    }
    @Override
    public void init(){
      try{
        NekoLauncher.initDir(StaticRes.MINECRAFT);
        NekoLauncher.initDir(StaticRes.BIN);
        NekoLauncher.initDir(StaticRes.LIB);
        NekoLauncher.initDir(StaticRes.NATIVES);
        NekoLauncher.initDir(StaticRes.MODS);
        if(!new File(".minecraft/options.txt").exists()){
        FileUtil.createFile(new File(".minecraft/options.txt").getAbsolutePath(),"lang:zh_CN");
        }
        }
        catch(Exception ex){
            NekoLauncher.handleException(ex);
        }
      super.init();
    }
    private void initPic() throws IOException{
        JPanel panel=new JPanel(){
                    @Override
            protected void paintComponent(Graphics g) {
                // TODO Auto-generated method stub
                super.paintComponent(g);
                try{
                g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("background.png")), 0, 0, this);
                }
                catch (Exception e){
                    NekoLauncher.handleException(e);
                }
            }
    };
                panel.setLayout(null);
        this.setContentPane(panel);
        JLabel logo=new JLabel(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("nekocraft.png"))));
        logo.setBounds(0,0,400,130);
        this.getContentPane().add(logo);
    }
    public void reInit(){
        
        login.setEnabled(true);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        bar.setString("请登录");
        user.setTextContent("");
        pass.setTextContent("");
    }
}
