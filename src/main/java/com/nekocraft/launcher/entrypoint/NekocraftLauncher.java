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
package com.nekocraft.launcher.entrypoint;

import com.beust.jcommander.JCommander;
import com.nekocraft.launcher.*;
import com.nekocraft.launcher.api.Launcher;
import com.nekocraft.launcher.api.NekocraftDirectories;
import com.nekocraft.launcher.exceptions.RestfulAPIException;
import com.nekocraft.launcher.rest.NekocraftBuild;
import com.nekocraft.launcher.skin.ConsoleFrame;
import com.nekocraft.launcher.skin.ErrorDialog;
import com.nekocraft.launcher.skin.MetroLoginFrame;
import com.nekocraft.launcher.skin.components.LoginFrame;
import com.nekocraft.launcher.util.OperatingSystem;
import com.nekocraft.launcher.util.Utils;
import com.nekocraft.launcher.yml.YAMLFormat;
import com.nekocraft.launcher.yml.YAMLProcessor;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class NekocraftLauncher {
    private static Logger logger = null;
    protected static RotatingFileHandler handler = null;
    protected static ConsoleFrame console;

    public NekocraftLauncher() {
        main(new String[0]);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final long startupTime = start;

        // 设置java参数 Prefer IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");
        // 清空临时文件
        cleanup();
        // 加载界面
        SplashScreen splash = new SplashScreen(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/com/nekocraft/launcher/resources/splash.png")));
        splash.setVisible(true);

        StartupParameters params = setupParameters(args);
        NekocraftLauncher.logger = setupLogger();

        int launcherBuild = parseInt(getLauncherBuild(), -1);
        logger.info("---------------------------------------------");
        logger.info("Nekoecraft Launcher is starting...");
        logger.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        logger.info("Java Version: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " <" + System.getProperty("java.vendor.url") + ">");
        logger.info("Launcher Build: " + launcherBuild);

        params.logParameters(logger);

        // 初始化工作目录
        NekocraftDirectories dirs = new NekocraftDirectories();
        dirs.getSkinDir().mkdirs();
        dirs.getSpoutcraftDir().mkdirs();

        // 读取设置
        if (Settings.getYAML() == null) {
            YAMLProcessor settings = setupSettings();
            if (settings == null) {
                throw new NullPointerException("The YAMLProcessor object was null for settings.");
            }
            Settings.setYAML(settings);
        }
        Settings.setLauncherBuild(launcherBuild);
        // 设置代理服务器
        setupProxy();
        // 调试
        if (params.isDebugMode()) {
            Settings.setDebugMode(true);
            logger.info("Debug mode activated!");
        }
        if (Settings.isDebugMode()) {
            logger.info("Initial organization and look and feel time took " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
        }
        // 需要设置jvm参数重新启动
        if (params.relaunch(logger)) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
            return;
        }
        // 检查
        validateBuild(params);
        setLookAndFeel();

        if (params.isConsole() || Settings.isDebugMode()) {
            setupConsole();
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        Thread logThread = new LogFlushThread();
        logThread.start();

        if (Settings.isDebugMode()) {
            logger.info("Internet validation and look and feel took " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
        }

        // 启动主界面
        LoginFrame frame = new MetroLoginFrame();

        try {
            @SuppressWarnings("unused")
            Launcher launcher = new Launcher(new GameUpdater(), new GameLauncher(), frame);
        } catch (IOException failure) {
            failure.printStackTrace();
            ErrorDialog dialog = new ErrorDialog(frame, failure);
            splash.dispose();
            frame.setVisible(true);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
            return;
        }
        // 更新游戏
        Launcher.getGameUpdater().start();

        if (Settings.isDebugMode()) {
            logger.info("Loading default launcher skin took " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
        }

        splash.dispose();
        frame.setVisible(true);
        if (params.hasAccount()) {
            frame.disableForm();
            frame.doLogin(params.getUser(), params.getPass());
        }

        logger.info("Launcher startup took " + (System.currentTimeMillis() - startupTime) + "ms");
    }

    public static int pingURL(String urlLoc) {
        try {
            final URL url = new URL(urlLoc);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            int response = conn.getResponseCode();
            if (logger != null) {
                logger.info("Pinging [" + urlLoc + "], response: " + response);
            }
            return response;
        } catch (IOException e) {
        }
        if (logger != null) {
            logger.info("Pinged [" + urlLoc + "], no response.");
        }
        return HttpURLConnection.HTTP_NOT_FOUND;
    }

    private static void setupProxy() {
        Proxy proxy = new Proxy();
        proxy.setHost(Settings.getProxyHost());
        proxy.setPort(Settings.getProxyPort());
        proxy.setUser(Settings.getProxyUsername());
        String pass = Settings.getProxyPassword();
        proxy.setPass(pass != null ? pass.toCharArray() : null);
        proxy.setup();
    }

    private static void validateBuild(StartupParameters params) {
        if (params.getSpoutcraftBuild() > 0) {
            List<NekocraftBuild> builds;
            try {
                builds = NekocraftBuild.getBuildList();
                String build = String.valueOf(params.getSpoutcraftBuild());
                for (NekocraftBuild b : builds) {
                    if (b.getBuildNumber().equals(build)) {
                        return;
                    }
                }
            } catch (RestfulAPIException e) {
                e.printStackTrace();
            }
            params.setSpoutcraftBuild(-1);
        }
    }

    private static void cleanup() {
        File temp = new File(Utils.getWorkingDirectory(), "temp.jar");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "temp.exe");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "Spoutcraft-Launcher.jar");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "launcherVersion");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "mc.patch");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "config/libraries.yml");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "config/spoutcraft.yml");
        temp.delete();
        temp = new File(Utils.getWorkingDirectory(), "config/minecraft.yml");
        temp.delete();
    }

    private static void setLookAndFeel() {
        OperatingSystem os = OperatingSystem.getOS();
        if (os.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Nekocraft");
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to setup look and feel", e);
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected static Logger setupLogger() {
        final Logger logger = Logger.getLogger("launcher");
        File logDirectory = new File(Utils.getWorkingDirectory(), "logs");
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }
        File logs = new File(logDirectory, "nekocraft_%D.log");
        RotatingFileHandler fileHandler = new RotatingFileHandler(logs.getPath());

        fileHandler.setFormatter(new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")));

        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        logger.addHandler(fileHandler);

        NekocraftLauncher.handler = fileHandler;

        logger.setUseParentHandlers(false);

        System.setOut(new PrintStream(new LoggerOutputStream(Level.INFO, logger), true));
        System.setErr(new PrintStream(new LoggerOutputStream(Level.SEVERE, logger), true));

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                logger.log(Level.SEVERE, "Unhandled Exception in " + t, e);
            }
        });

        return logger;
    }

    private static StartupParameters setupParameters(String[] args) {
        StartupParameters params = new StartupParameters(args);
        try {
            new JCommander(params, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Utils.setStartupParameters(params);

        params.setupProxy();

        return params;
    }

    public static String getLauncherBuild() {
        String build = "0";
        try {
            build = IOUtils.toString(NekocraftLauncher.class.getResource("/com/nekocraft/launcher/resources/version").openStream(), "UTF-8");
        } catch (Exception e) {
        }
        return build;
    }

    protected static YAMLProcessor setupSettings() {
        File file = new File(Utils.getWorkingDirectory(), "config" + File.separator + "settings.yml");

        if (!file.exists()) {
            try {
                InputStream input = NekocraftLauncher.class.getResource("resources/settings.yml").openStream();
                if (input != null) {
                    FileOutputStream output = null;
                    try {
                        file.getParentFile().mkdirs();
                        output = new FileOutputStream(file);
                        byte[] buf = new byte[8192];
                        int length;

                        while ((length = input.read(buf)) > 0) {
                            output.write(buf, 0, length);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            input.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            if (output != null) {
                                output.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        return new YAMLProcessor(file, false, YAMLFormat.EXTENDED);
    }

    public static void flush() {
        if (handler != null) handler.flush();
    }

    public static void setupConsole() {
        if (console == null) {
            console = new ConsoleFrame(2500, true);
            console.setVisible(true);
        }
    }

    public static void destroyConsole() {
        if (console != null) {
            console.setVisible(false);
            console.dispose();
        }
    }
}

class LogFlushThread extends Thread {
    public LogFlushThread() {
        super("Log Flush Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            if (NekocraftLauncher.handler != null) {
                NekocraftLauncher.handler.flush();
            }
            try {
                sleep(60000);
            } catch (InterruptedException e) {
            }
        }
    }
}

class ShutdownThread extends Thread {
    public ShutdownThread() {
        super("Shutdown Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        if (NekocraftLauncher.handler != null) {
            NekocraftLauncher.handler.flush();
        }
    }
}

class LoggerOutputStream extends ByteArrayOutputStream {
    private final String separator = System.getProperty("line.separator");
    private final Level level;
    private final Logger log;

    public LoggerOutputStream(Level level, Logger log) {
        super();
        this.level = level;
        this.log = log;
    }

    @Override
    public synchronized void flush() throws IOException {
        super.flush();
        String record = this.toString();
        super.reset();

        if (record.length() > 0 && !record.equals(separator)) {
            log.logp(level, "LoggerOutputStream", "log" + level, record);
            if (NekocraftLauncher.console != null) {
                NekocraftLauncher.console.log(record + "\n");
            }
        }
    }
}

class RotatingFileHandler extends StreamHandler {
    private final SimpleDateFormat date;
    private final String logFile;
    private String filename;

    public RotatingFileHandler(String logFile) {
        this.logFile = logFile;
        date = new SimpleDateFormat("yyyy-MM-dd");
        filename = calculateFilename();
        try {
            setOutputStream(new FileOutputStream(filename, true));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void flush() {
        if (!filename.equals(calculateFilename())) {
            filename = calculateFilename();
            try {
                setOutputStream(new FileOutputStream(filename, true));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        super.flush();
    }

    private String calculateFilename() {
        return logFile.replace("%D", date.format(new Date()));
    }
}

class DateOutputFormatter extends Formatter {
    private final SimpleDateFormat date;

    public DateOutputFormatter(SimpleDateFormat date) {
        this.date = date;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        builder.append(date.format(record.getMillis()));
        builder.append(" [");
        builder.append(record.getLevel().getLocalizedName().toUpperCase());
        builder.append("] ");
        builder.append(formatMessage(record));
        builder.append('\n');

        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            builder.append(writer.toString());
        }

        return builder.toString();
    }
}
