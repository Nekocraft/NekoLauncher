package com.nekocraft.launcher;
import java.awt.*;  
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;  
public class LoginFrame extends JFrame{
    public static GradientProgressBar bar;
    public static JLabel login=new JLabel("");
    public static LoginFrame instance;
    private JTextField user,pass;
    private static Point origin = new Point(); 
    public static boolean dragging;
    public LoginFrame(){
        dragging=false;
        System.out.println(new File("").getAbsolutePath());
        instance=this;
        ////////窗体逻辑
        this.setTitle("NekoLauncher");
        this.setSize(new Dimension(600, 450));   
        setResizable(false); 
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setUndecorated(true);
        setVisible(true);
        this.setLayout(null);
        try{
        initPic();
        }
        catch (Exception e){
            System.out.println(e);
            NekoLauncher.handleException(e);
        }/*
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
            
        });*/
        ////////按钮逻辑
        login.setBounds(516,367,47,47);
        login.setEnabled(true);
        this.getContentPane().add(login);
        login.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null,"Hello!");
                NekoLauncher.lt=new LoginThread();
                NekoLauncher.lt.setUser(user.getText());
                NekoLauncher.lt.setPassword(pass.getText());
                NekoLauncher.lt.start();
                login.setEnabled(false);
            }});
        ////////进度条逻辑
        bar=new GradientProgressBar();
        bar.setImage("progressbar.png");
        bar.setBounds(145,133, 260, 26);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(50);
        //bar.setForeground(new Color(144,69,196));
        //bar.setBackground(Color.PINK);
        bar.setBorderPainted(false);
        bar.setStringPainted(true);
        bar.setString("请登录");
        this.getContentPane().add(bar);
        JLabel close=new JLabel("");
        close.setBounds(563, 14, 24, 18);
        close.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent me) {
                int i=JOptionPane.showConfirmDialog(me.getComponent().getParent(),"退出启动器吗？","提示",JOptionPane.OK_CANCEL_OPTION);
                if(i==JOptionPane.OK_OPTION){
                    System.exit(0);
                }
            }
        });
        this.getContentPane().add(close);
        user=new JTextField();
        user.setBorder(null);
        user.setBounds(207,179,175,18);
        this.getContentPane().add(user);
        pass=new JPasswordField();
        pass.setBorder(null);
        pass.setBounds(207,224,175,18);
        this.getContentPane().add(pass);
    }
    private void initPic() throws IOException{
        JPanel bk=new JPanel(){
            public void paintComponent (Graphics g)  
            {   
            super.paintComponent(g);  
            try{
            g.drawImage(ImageIO.read(getClass().getClassLoader().getResourceAsStream("launch.png")),0,0,this); 
            }
            catch(Exception ignore){
                ignore.printStackTrace();
            }
            }      
        };
        bk.setLayout(null);
                bk.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {  //按下（mousePressed 不是点击，而是鼠标被按下没有抬起）
                                LoginFrame.dragging=true;
                                System.out.println(LoginFrame.dragging);
                                origin.x = e.getX();  //当鼠标按下的时候获得窗口当前的位置
                                origin.y = e.getY();
                        }
                        public void mouseReleased(MouseEvent e){
                                LoginFrame.dragging=false;
                                System.out.println(LoginFrame.dragging);
                        }
                });
                bk.addMouseMotionListener(new MouseMotionAdapter() {
                        public void mouseDragged(MouseEvent e) {  //拖动（mouseDragged 指的不是鼠标在窗口中移动，而是用鼠标拖动）
                                if(LoginFrame.dragging){
                                System.out.println(NekoLauncher.mf.getLocation().toString());
                                Point p = NekoLauncher.mf.getLocation();  //当鼠标拖动时获取窗口当前位置
                                //设置窗口的位置
                                //窗口当前的位置 + 鼠标当前在窗口的位置 - 鼠标按下的时候在窗口的位置
                                NekoLauncher.mf.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
                                System.out.println(NekoLauncher.mf.getLocation().toString());
                                }
                        }
                });
               this.setContentPane(bk);
        //this.setContentPane(panel);
        //JLabel logo=new JLabel(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("launch.png"))));
        //logo.setBounds(0,0,400,130);
        //this.getContentPane().add(logo);
    }
    public void reInit(){
        
        login.setEnabled(true);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        bar.setString("请登录");
        user.setText("");
        pass.setText("");
    }
}
