package com.nekocraft.launcher;
import java.awt.*;  
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;  
//import javax.swing.text.html.StyleSheet;
public class LoginFrame extends JFrame{
    public static GradientProgressBar bar;
    public static JLabel login=new JLabel("");
    public static LoginFrame instance;
    private JTextField user,pass;
    private static Point origin = new Point(); 
    public boolean dragging;
    private JLabel newsTitle,newsDate,newsContent;
    public LoginFrame(){
        dragging=false;
        System.out.println(new File("").getAbsolutePath());
        instance=this;
        ////////窗体逻辑
        this.setTitle("NekoLauncher");
        this.setSize(new Dimension(600, 450));   
        setResizable(false); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setUndecorated(true);
        //this.setLayout(null);
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
        newsTitle=new JLabel("正在读取新闻中……");
        newsDate=new JLabel("");
        newsContent=new JLabel("");
        newsTitle.setBounds(189, 305, 198, 25); //50 less
        newsDate.setBounds(387, 305, 50, 25);
        newsContent.setBounds(189,330,248,65);
        newsTitle.setOpaque(false);
        newsDate.setOpaque(false);
        newsContent.setOpaque(false);
        this.getContentPane().add(newsTitle);
        this.getContentPane().add(newsDate);
        this.getContentPane().add(newsContent);
        newsTitle.setForeground(Color.decode("#673ab9"));
        newsDate.setForeground(Color.decode("#c0c0c0"));
        newsContent.setForeground(Color.gray);
        newsTitle.setFont(new Font("simhei",Font.TRUETYPE_FONT,16));
        newsDate.setFont(new Font("simsun",Font.TRUETYPE_FONT,13));
        setVisible(true);
        fetchNews();
    }
    private void fetchNews(){
        NekoNews news=NekoNews.fetchLatestNews();
        newsTitle.setText(news.getTitle());
        newsTitle.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                System.out.println("Clicked!");
        if(java.awt.Desktop.isDesktopSupported()){
            try {
                //创建一个URI实例
                java.net.URI uri = java.net.URI.create(NekoNews.fetchLatestNews().getLink()); 
                //获取当前系统桌面扩展
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                //判断系统桌面是否支持要执行的功能
                if(dp.isSupported(java.awt.Desktop.Action.BROWSE)){
                    //获取系统默认浏览器打开链接
                    dp.browse(uri);    
                }
            } catch(java.lang.NullPointerException ex){
                //此为uri为空时抛出异常
            } catch (java.io.IOException ex) {
                //此为无法获取系统默认浏览器
            }             
        }
            }
        });
        newsDate.setText(news.getDate());
        newsContent.setText("<html>"+news.getDesc()+"</html>");
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
            //
            }      
        };
        bk.setLayout(null);
        bk.setOpaque(false);
        this.setContentPane(bk);
        ComponentMover cm=new ComponentMover(JFrame.class,bk);
        /*
                this.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {  //按下（mousePressed 不是点击，而是鼠标被按下没有抬起）
                                NekoLauncher.mf.dragging=true;
                                System.out.println(NekoLauncher.mf.dragging);
                                origin.x = e.getX();  //当鼠标按下的时候获得窗口当前的位置
                                origin.y = e.getY();
                        }
                        public void mouseReleased(MouseEvent e){
                                NekoLauncher.mf.dragging=false;
                                System.out.println(NekoLauncher.mf.dragging);
                        }
                });
                this.addMouseMotionListener(new MouseMotionAdapter() {
                        public void mouseDragged(MouseEvent e) {  //拖动（mouseDragged 指的不是鼠标在窗口中移动，而是用鼠标拖动）
                                if(NekoLauncher.mf.dragging){
                                System.out.println(NekoLauncher.mf.getLocation().toString());
                                Point p = NekoLauncher.mf.getLocation();  //当鼠标拖动时获取窗口当前位置
                                //设置窗口的位置
                                //窗口当前的位置 + 鼠标当前在窗口的位置 - 鼠标按下的时候在窗口的位置
                                NekoLauncher.mf.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
                                System.out.println(NekoLauncher.mf.getLocation().toString());
                                }
                        }
                });*/
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
