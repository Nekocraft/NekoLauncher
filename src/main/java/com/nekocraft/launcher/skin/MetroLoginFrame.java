/*
 * This file is part of Spoutcraft Launcher.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spoutcraft Launcher is licensed under the Spout License Version 1.
 *
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license,
 * including the MIT license.
 */
package com.nekocraft.launcher.skin;

import com.nekocraft.launcher.rest.RestAPI;
import com.nekocraft.launcher.skin.components.*;
import com.nekocraft.launcher.util.ImageUtils;
import com.nekocraft.launcher.util.OperatingSystem;
import com.nekocraft.launcher.util.ResourceUtils;
import org.spout.downpour.connector.DownloadURLConnector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static com.nekocraft.launcher.util.ResourceUtils.getResourceAsStream;

public class MetroLoginFrame extends LoginFrame implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final URL closeIcon = LoginFrame.class.getResource("/com/nekocraft/launcher/resources/close.png");
    private static final URL minimizeIcon = LoginFrame.class.getResource("/com/nekocraft/launcher/resources/minimize.png");
    private static final URL optionsIcon = LoginFrame.class.getResource("/com/nekocraft/launcher/resources/options.png");
    private static final int FRAME_WIDTH = 880, FRAME_HEIGHT = 520;
    private static int mouseX = 0, mouseY = 0;
    private static final String CLOSE_ACTION = "close";
    private static final String MINIMIZE_ACTION = "minimize";
    private static final String OPTIONS_ACTION = "options";
    private static final String LOGIN_ACTION = "login";
    private static final String IMAGE_LOGIN_ACTION = "image_login";
    private static final String REMOVE_USER = "remove";
    private static final String TEXT_CHANGE_ACTION = "text_change";
    private final Map<JButton, DynamicButton> removeButtons = new HashMap<JButton, DynamicButton>();
    private LiteTextBox name;
    private LitePasswordBox pass;
    private LiteButton login;
    private JCheckBox remember;
    private TransparentButton close, minimize, options;
    private LiteProgressBar progressBar;
    private OptionsMenu optionsMenu = null;

    public MetroLoginFrame() {
        initComponents();
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        addMouseListener(this);
        addMouseMotionListener(this);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().add(new BackgroundImage(FRAME_WIDTH, FRAME_HEIGHT));
    }

    private void initComponents() {
        int xShift = 0;
        int yShift = 0;
        if (this.isUndecorated()) {
            yShift += 30;
        }

        // Setup username box
        name = new LiteTextBox(this, "用户名");
        Font base = name.getFont().deriveFont(12f);
        name.setBounds(FRAME_WIDTH / 2 - 90, 339 + yShift, 180, 24);
        name.setFont(base);
        name.addKeyListener(this);

        // Setup password box
        pass = new LitePasswordBox(this, "密码");
        pass.setBounds(FRAME_WIDTH / 2 - 90, 368 + yShift, 180, 24);
        pass.setFont(base);
        pass.addKeyListener(this);

        // Setup remember checkbox
        remember = new JCheckBox("记住密码");
        remember.setBounds(FRAME_WIDTH / 2 - 90, 397 + yShift, 110, 24);
        remember.setFont(base);
        remember.setOpaque(false);
        remember.setBorderPainted(false);
        remember.setContentAreaFilled(false);
        remember.setBorder(null);
        remember.setForeground(Color.WHITE);
        remember.addKeyListener(this);

        // Setup login button
        login = new LiteButton("登录");
        login.setBounds(FRAME_WIDTH / 2 + 5, 397 + yShift, 85, 24);
        login.setFont(base.deriveFont(13f));
        login.setActionCommand(LOGIN_ACTION);
        login.addActionListener(this);
        login.addKeyListener(this);

        Font big = base.deriveFont(16f);
        // Nekocraft logo
        JLabel logo = new JLabel();
        logo.setBounds(FRAME_WIDTH / 2 - 200, 35, 400, 109);
        logo.setFont(big);
        setIcon(logo, "nekocraft.png", logo.getWidth(), logo.getHeight());

        // Progress Bar
        progressBar = new LiteProgressBar();
        progressBar.setBounds(FRAME_WIDTH / 2 - 192, pass.getY() + 90, 384, 23);
        progressBar.setFont(big);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setOpaque(true);
        progressBar.setTransparency(0.70F);
        progressBar.setHoverTransparency(0.70F);

        // 主页
        HyperlinkJLabel home = new HyperlinkJLabel("主页", "http://nekocraft.com/");
        home.setToolTipText("Visit our homepage");
        home.setFont(big);
        home.setBounds(10, FRAME_HEIGHT - 27, 65, 20);
        home.setForeground(Color.WHITE);
        home.setOpaque(false);
        home.setTransparency(0.70F);
        home.setHoverTransparency(1F);

        // Forums link
        HyperlinkJLabel forums = new HyperlinkJLabel("论坛", "http://bbs.nekonazo.com/u53.1/");
        forums.setToolTipText("Visit our community forums");
        forums.setFont(big);
        forums.setBounds(82, FRAME_HEIGHT - 27, 90, 20);
        forums.setForeground(Color.WHITE);
        forums.setOpaque(false);
        forums.setTransparency(0.70F);
        forums.setHoverTransparency(1F);

        // Donate link
        HyperlinkJLabel donate = new HyperlinkJLabel("捐助", "http://nekocraft.com/donate");
        donate.setToolTipText("Donate to the project");
        donate.setFont(big);
        donate.setBounds(185, FRAME_HEIGHT - 27, 85, 20);
        donate.setForeground(Color.WHITE);
        donate.setOpaque(false);
        donate.setTransparency(0.70F);
        donate.setHoverTransparency(1F);

        // Close button
        close = new TransparentButton();
        close.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closeIcon)));
        if (OperatingSystem.getOS().isMac()) {
            close.setBounds(0, 0, 37, 20);
        } else {
            close.setBounds(FRAME_WIDTH - 37, 0, 37, 20);
        }
        close.setTransparency(0.70F);
        close.setHoverTransparency(1F);
        close.setActionCommand(CLOSE_ACTION);
        close.addActionListener(this);
        close.setBorder(BorderFactory.createEmptyBorder());
        close.setContentAreaFilled(false);

        // Minimize button
        minimize = new TransparentButton();
        minimize.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(minimizeIcon)));
        if (OperatingSystem.getOS().isMac()) {
            minimize.setBounds(37, 0, 37, 20);
        } else {
            minimize.setBounds(FRAME_WIDTH - 74, 0, 37, 20);
        }
        minimize.setTransparency(0.70F);
        minimize.setHoverTransparency(1F);
        minimize.setActionCommand(MINIMIZE_ACTION);
        minimize.addActionListener(this);
        minimize.setBorder(BorderFactory.createEmptyBorder());
        minimize.setContentAreaFilled(false);

        // Options Button
        options = new TransparentButton();
        options.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(optionsIcon)));
        if (OperatingSystem.getOS().isMac()) {
            options.setBounds(74, 0, 37, 20);
        } else {
            options.setBounds(FRAME_WIDTH - 111, 0, 37, 20);
        }
        options.setTransparency(0.70F);
        options.setHoverTransparency(1F);
        options.setActionCommand(OPTIONS_ACTION);
        options.addActionListener(this);
        options.setBorder(BorderFactory.createEmptyBorder());
        options.setContentAreaFilled(false);

        // Rectangle
        JLabel bottomRectangle = new JLabel();
        bottomRectangle.setBounds(0, FRAME_HEIGHT - 34, FRAME_WIDTH, 34);
        bottomRectangle.setBackground(new Color(30, 30, 30, 180));
        bottomRectangle.setOpaque(true);

        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        java.util.List<String> savedUsers = getSavedUsernames();
        int users = Math.min(5, this.getSavedUsernames().size());
        for (int i = 0; i < users; i++) {
            String accountName = savedUsers.get(i);
            String userName = this.getUsername(accountName);

            if (i == 0) {
                name.setText(accountName);
                pass.setText(this.getSavedPassword(accountName));
                remember.setSelected(true);
            }

            // Create callable
            CallbackTask callback = getImage(userName);

            // Start callable
            FutureTask<BufferedImage> futureImage = new FutureTask<BufferedImage>(callback);
            Thread downloadThread = new Thread(futureImage, "Image download thread");
            downloadThread.setDaemon(true);
            downloadThread.start();

            // Create future image, using default mc avatar for now
            FutureImage userImage = new FutureImage(getDefaultImage());
            callback.setCallback(userImage);

            DynamicButton userButton = new DynamicButton(this, userImage, 44, accountName, userName);
            userButton.setFont(base.deriveFont(14F));

            userImage.setRepaintCallback(userButton);

            userButton.setBounds((FRAME_WIDTH - 90) * (i + 1) / (users + 1), (FRAME_HEIGHT - 110) / 2, 90, 90);
            contentPane.add(userButton);
            userButton.setActionCommand(IMAGE_LOGIN_ACTION);
            userButton.addActionListener(this);
            setIcon(userButton.getRemoveIcon(), "remove.png", 16);
            userButton.getRemoveIcon().addActionListener(this);
            userButton.getRemoveIcon().setActionCommand(REMOVE_USER);
            userButton.getRemoveIcon().setBorder(BorderFactory.createEmptyBorder());
            userButton.getRemoveIcon().setContentAreaFilled(false);
            removeButtons.put(userButton.getRemoveIcon(), userButton);
        }

        contentPane.add(name);
        contentPane.add(pass);
        contentPane.add(remember);
        contentPane.add(login);
        contentPane.add(home);
        contentPane.add(forums);
        contentPane.add(donate);
        contentPane.add(logo);
        contentPane.add(options);
        contentPane.add(close);
        contentPane.add(minimize);
        contentPane.add(progressBar);
        contentPane.add(bottomRectangle);
        setUndecorated(true);
        setFocusTraversalPolicy(new LoginFocusTraversalPolicy());
    }

    private void setIcon(JButton button, String iconName, int size) {
        try {
            button.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/com/nekocraft/launcher/resources/" + iconName)), size, size)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setIcon(JLabel label, String iconName, int w, int h) {
        try {
            label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/com/nekocraft/launcher/resources/" + iconName)), w, h)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage getDefaultImage() {
        try {
            return ImageIO.read(getResourceAsStream("/com/nekocraft/launcher/resources/face.png"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read backup image");
        }
    }

    private CallbackTask getImage(final String user) {
        return new CallbackTask(new Callable<BufferedImage>() {
            public BufferedImage call() throws Exception {
                try {
                    System.out.println("Attempting to grab avatar helm for " + user + "...");
                    InputStream stream = RestAPI.getCache().get(new URL("http://skins.technicpack.net/helm/" + user + "/100"), new DownloadURLConnector() {
                        @Override
                        public void setHeaders(URLConnection conn) {
                            conn.setDoInput(true);
                            conn.setDoOutput(false);
                            System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19");
                            HttpURLConnection.setFollowRedirects(true);
                            conn.setUseCaches(false);
                            ((HttpURLConnection) conn).setInstanceFollowRedirects(true);
                            conn.setConnectTimeout(10000);
                            conn.setReadTimeout(10000);
                        }
                    }, true);
                    BufferedImage image = ImageIO.read(stream);
                    if (image == null) {
                        throw new NullPointerException("No avatar helm downloaded!");
                    }
                    System.out.println("Completed avatar helm request!");
                    return image;
                } catch (Exception e) {
                    System.out.println("Failed avatar helm request!");
                    throw e;
                }
            }
        });
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        this.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
    }

    public void mouseMoved(MouseEvent e) {
    }

    private static class CallbackTask implements Callable<BufferedImage> {
        private final Callable<BufferedImage> task;
        private volatile ImageCallback callback;

        CallbackTask(Callable<BufferedImage> task) {
            this.task = task;
        }

        public void setCallback(ImageCallback callback) {
            this.callback = callback;
        }

        public BufferedImage call() throws Exception {
            BufferedImage image = null;
            try {
                image = task.call();
                return image;
            } finally {
                callback.done(image);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComponent) {
            action(e.getActionCommand(), (JComponent) e.getSource());
        }
    }

    private void action(String action, JComponent c) {
        if (action.equals(CLOSE_ACTION)) {
            System.exit(EXIT_ON_CLOSE);
        } else if (action.equals(MINIMIZE_ACTION)) {
            setState(Frame.ICONIFIED);
        } else if (action.equals(OPTIONS_ACTION)) {
            if (optionsMenu == null || !optionsMenu.isVisible()) {
                optionsMenu = new OptionsMenu();
                optionsMenu.setModal(true);
                optionsMenu.setVisible(true);
            }
        } else if (action.equals(LOGIN_ACTION)) {
            String pass = new String(this.pass.getPassword());
            if (getSelectedUser().length() > 0 && pass.length() > 0) {
                this.doLogin(getSelectedUser(), pass);
                if (remember.isSelected()) {
                    saveUsername(getSelectedUser(), pass);
                }
            }
        } else if (action.equals(IMAGE_LOGIN_ACTION)) {
            DynamicButton userButton = (DynamicButton) c;
            userButton.setEnabled(false);
            this.name.setText(userButton.getAccount());
            this.pass.setText(this.getSavedPassword(userButton.getAccount()));
            this.remember.setSelected(true);
            action(LOGIN_ACTION, userButton);
        } else if (action.equals(REMOVE_USER)) {
            DynamicButton userButton = removeButtons.get((JButton) c);
            if (userButton.getRemoveIcon().getTransparency() > 0.1F) {
                this.removeAccount(userButton.getAccount());
                userButton.setVisible(false);
                userButton.setEnabled(false);
                getContentPane().remove(userButton);
                c.setVisible(false);
                c.setEnabled(false);
                getContentPane().remove(c);
                removeButtons.remove(c);
                writeUsernameList();
            }
        }
    }

    public void stateChanged(final String status, final float progress) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int intProgress = Math.round(progress);
                progressBar.setValue(intProgress);
                String text = status;
                if (text.length() > 60) {
                    text = text.substring(0, 60) + "...";
                }
                progressBar.setString(intProgress + "% " + text);
            }
        });
    }

    @Override
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void disableForm() {
    }

    @Override
    public void enableForm() {
    }

    @Override
    public String getSelectedUser() {
        return this.name.getText();
    }

    // Emulates tab focus policy of name -> pass -> remember -> login
    private class LoginFocusTraversalPolicy extends FocusTraversalPolicy {
        public Component getComponentAfter(Container con, Component c) {
            if (c == name) {
                return pass;
            } else if (c == pass) {
                return remember;
            } else if (c == remember) {
                return login;
            } else if (c == login) {
                return name;
            }
            return getFirstComponent(con);
        }

        public Component getComponentBefore(Container con, Component c) {
            if (c == name) {
                return login;
            } else if (c == pass) {
                return name;
            } else if (c == remember) {
                return pass;
            } else if (c == login) {
                return remember;
            }
            return getFirstComponent(con);
        }

        public Component getFirstComponent(Container c) {
            return name;
        }

        public Component getLastComponent(Container c) {
            return login;
        }

        public Component getDefaultComponent(Container c) {
            return name;
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // Allows the user to press enter and log in from the login box focus, username box focus, or password box focus
            if (e.getComponent() == login || e.getComponent() == name || e.getComponent() == pass) {
                action(LOGIN_ACTION, (JComponent) e.getComponent());
            } else if (e.getComponent() == remember) {
                remember.setSelected(!remember.isSelected());
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
