package com.nekocraft.launcher;
import com.sun.awt.AWTUtilities;
import java.awt.*;  
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;  
//import javax.swing.text.html.StyleSheet;
public class LoginFrame extends TransparentFrame{
    public static JProgressBar bar;
    public static JLabel login=new JLabel("");
    public static JLabel settings=new JLabel("");
    public static LoginFrame instance;
    private JTextField user,pass;
    private JLabel newsTitle,newsDate,newsContent;
    private JLabel head;
    public static JCheckBox savepwd;
    public TransparentPanel main;
    public LoginFrame(){
        super("NekoLauncher",new Rectangle(8,9,605,433));
        System.out.println(new File("").getAbsolutePath());
        instance=this;
        ////////窗体逻辑
        this.setSize(new Dimension(620, 450));   
        setResizable(false); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(getOwner());
        setUndecorated(true);
        this.setLayout(null);
        //this.setBackground(new Color(100,0,0,0));
        try{
        initPanels();
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
        ////////登录按钮逻辑
        login.setBounds(516,367,47,47);
        login.setEnabled(true);
        this.getContentPane().add(login);
        login.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                //JOptionPane.showMessageDialog(null,"Hello!");
                NekoLauncher.lt=new LoginThread();
                NekoUser usr=NekoLauncher.du;
                usr.setUsername(user.getText());
                usr.setPassword(pass.getText());
                NekoLauncher.lt.setUser(usr);
                NekoLauncher.lt.start();
                login.setEnabled(false);
            }});
        settings.setBounds(516,414,47,47);
        this.getContentPane().add(settings);
        settings.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                JDialog od=new OptionDialog(NekoLauncher.mf,true);
                od.show();
            }});
        ////////进度条逻辑
        bar=new JProgressBar();
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
        ////////关闭按钮逻辑
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
        ////////用户名密码保存密码逻辑
        user=new JTextField();
        user.setBorder(null);
        user.setBounds(207,179,175,18);
        user.setOpaque(false);
        user.setText(NekoLauncher.du.getUsername());
        this.getContentPane().add(user);
        pass=new JPasswordField();
        pass.setBorder(null);
        pass.setBounds(207,224,175,18);
        pass.setOpaque(false);
        pass.setText(NekoLauncher.du.getPassword());
        this.getContentPane().add(pass);
        savepwd=new JCheckBox("保存密码");
        savepwd.setOpaque(false);
        savepwd.setBounds(385, 224,80,18);
        if(!pass.getText().equals("")){
            savepwd.setSelected(true);
        }
        this.getContentPane().add(savepwd);
        ////////新闻逻辑
        newsTitle=new JLabel("正在读取新闻中……");
        newsDate=new JLabel("");
        newsContent=new JLabel("<html></html>");
        newsTitle.setBounds(189, 305, 198, 25); //50 less
        newsDate.setBounds(387, 305, 50, 25);
        newsContent.setBounds(188,331,238,73);
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
        newsDate.setFont(new Font("simsun",Font.TRUETYPE_FONT,12));
        newsContent.setFont(new Font("simsun",Font.TRUETYPE_FONT,13));
        ////////头像逻辑
        head=new JLabel("");
        //mouse.setBackground(Color.red);
        head.setOpaque(false);
        head.setBounds(461,173,64,64);
        this.getContentPane().add(head);
        setVisible(true);
        new Thread(){
        public void run(){
        try{
        NekoUser userdata=NekoLauncher.du;
        head.setIcon(new ImageIcon(userdata.getHeadImage()));
        }catch(Exception ex){NekoLauncher.handleException(ex);
        }}}.start();
        new Thread(){
            public void run(){
                fetchNews();
            }
        }.start();
        
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
    private void initPanels() throws IOException{
        main=new TransparentPanel();
        //this.setContentPane(main);
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
        //this.setContentPane(panel);
        //JLabel logo=new JLabel(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("launch.png"))));
        //logo.setBounds(0,0,400,130);
        //this.getContentPane().add(logo);
    }
    public void reInit(){
        /*
        login.setEnabled(true);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        bar.setString("请登录");
        user.setText("");
        pass.setText("");
        */
        //No need for a desktop application
        System.exit(0);
    }
}
