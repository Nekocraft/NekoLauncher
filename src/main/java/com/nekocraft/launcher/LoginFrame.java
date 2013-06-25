package com.nekocraft.launcher;
import java.awt.*;  
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;  
public class LoginFrame extends JFrame{
    public static JProgressBar bar=new JProgressBar();
    public static JButton login=new JButton("登录");
    public LoginFrame(){
        ////////窗体逻辑
        this.setSize(new Dimension(400, 500));  
        this.setTitle("Nekocraft Launcher");  
        setResizable(false); 
        setLocationRelativeTo(getOwner());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        try{
        initPic();
        }
        catch (Exception e){
            NekoLauncher.handleException(e);
        }
        ////////文本框逻辑
        final LabelTextField user=new LabelTextField("用户名");
        final LabelTextField pass=new LabelTextField("密　码",true);
        user.setBounds(100, 280, 200, 25);
        pass.setBounds(100,330,200,25);
        this.getContentPane().add(user);
        this.getContentPane().add(pass);
        ////////按钮逻辑
        login.setBounds(160,400,80,30);
        this.getContentPane().add(login);
        login.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                login.setEnabled(false);
                LoginThread lt=new LoginThread();
                lt.setUser(user.getTextContent());
                lt.setPassword(pass.getTextContent());
                lt.start();
            }
            
        });
        ////////进度条逻辑
        bar.setBounds(100,230, 200, 25);
        bar.setStringPainted(true);
        bar.setString("请登录");
        this.getContentPane().add(bar);
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
}
