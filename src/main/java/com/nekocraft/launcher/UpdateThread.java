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
package com.nekocraft.launcher;

import com.nekocraft.launcher.api.Launcher;
import com.nekocraft.launcher.api.NekocraftDirectories;
import com.nekocraft.launcher.exceptions.RestfulAPIException;
import com.nekocraft.launcher.exceptions.UnsupportedOSException;
import com.nekocraft.launcher.launch.MinecraftClassLoader;
import com.nekocraft.launcher.launch.MinecraftLauncher;
import com.nekocraft.launcher.rest.Library;
import com.nekocraft.launcher.rest.Minecraft;
import com.nekocraft.launcher.rest.RestAPI;
import com.nekocraft.launcher.rest.Versions;
import com.nekocraft.launcher.util.*;
import com.nekocraft.launcher.util.Download.Result;
import com.nekocraft.launcher.yml.Resources;
import com.nekocraft.launcher.yml.YAMLProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateThread extends Thread {
    /**
     * We only want to clean the old logs, temp folders once per startup
     */
    private static final AtomicBoolean cleaned = new AtomicBoolean(false);
    private static final int PRELOAD_CLASSES = 100;

    // Temporarily hardcoded
    private static final String WINDOWS_NATIVES_URL = "http://play.nekocraft.com/lib/lwjgl-2.8.4/native/windows_natives.jar";
    private static final String OSX_NATIVES_URL = "http://play.nekocraft.com/lib/lwjgl-2.8.4/native/macosx_natives.jar";
    private static final String LINUX_NATIVES_URL = "http://play.nekocraft.com/lib/lwjgl-2.8.4/native/linux_natives.jar";

    private final Logger logger = Logger.getLogger("launcher");
    private final AtomicBoolean waiting = new AtomicBoolean(false);
    private final AtomicBoolean valid = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final StartupParameters params = Utils.getStartupParameters();
    private final DownloadListener listener = new DownloadListenerWrapper();
    private volatile NekocraftData build;
    private final Thread previous;

    public UpdateThread() {
        this(null);
    }

    public UpdateThread(Thread previous) {
        super("Update Thread");
        setDaemon(true);
        this.previous = previous;
    }

    public NekocraftData getBuild() {
        return build;
    }

    @Override
    public void run() {
        try {
            build = new NekocraftData();
            boolean interrupted = false;
            while (true) {
                try {
                    if (previous != null) {
                        previous.join();
                    }
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (!interrupted) {
                runTasks();
            }
        } catch (Exception e) {
            if (!this.isInterrupted()) {
                Launcher.getLoginFrame().handleException(e);
            }
        }
    }

    private void runTasks() throws IOException {
        while (!valid.get()) {
            boolean minecraftUpdate = isMinecraftUpdateAvailable(build);
            boolean spoutcraftUpdate = minecraftUpdate || isSpoutcraftUpdateAvailable(build);

            if (minecraftUpdate) {
                updateMinecraft(build);
            }
            if (spoutcraftUpdate) {
                updateSpoutcraft(build);
            }

            updateAssets();

            // Download assets
            if (cleaned.compareAndSet(false, true)) {
                Resources.VIP.getYAML();
                Resources.Special.getYAML();
                Versions.getMinecraftVersions();

                cleanLogs();
                cleanTemp();
                updateFiles();

                RestAPI.getCache().cleanup();
            }

            Validator validate = new Validator();
            if (!(params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
                validate.run(build);
                valid.set(validate.isValid());
            } else {
                valid.set(true);
            }
        }

        MinecraftClassLoader loader;
        loader = MinecraftLauncher.getClassLoader(build.getLibraries());

        int loaded = 0;
        while (!waiting.get()) {
            int pass = loader.preloadClasses(PRELOAD_CLASSES);
            loaded += pass;
            // Less than the preload amount, so we are finished
            if (pass != PRELOAD_CLASSES) {
                break;
            }
        }
        logger.info("Preloaded " + loaded + " classes in advance");
        finished.set(true);
    }

    private void updateAssets() {
        YAMLProcessor assets = Resources.Assets.getYAML();
        if (assets != null) {
            updateAssets(assets.getMap(), Utils.getAssetsDirectory());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateAssets(Map<String, Object> assets, File directory) {
        directory.mkdirs();
        for (Entry<String, Object> entry : assets.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                updateAssets((Map<String, Object>) value, new File(directory, key));
            } else if (value instanceof String) {
                String url = (String) value;
                String name = getFileName(url);
                File asset = new File(directory, name);

                stateChanged("Verifying Asset: " + name, 0);

                boolean needDownload = true;
                if (asset.exists() && !(params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
                    String md5 = MD5Utils.getMD5(asset);
                    Launcher.debug("Checking MD5 of " + asset.getName() + ". Expected MD5: " + key + " | Actual MD5: " + md5);
                    needDownload = md5 == null || !md5.equals(key);
                } else if (asset.exists() && (params.isIgnoreMD5() || Settings.isIgnoreMD5())) {
                    needDownload = false;
                }

                if (needDownload) {
                    try {
                        DownloadUtils.downloadFile(url, asset.getPath(), null, key, listener);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Failed to download asset [" + url + "]", e);
                    }
                }
                stateChanged("Verified Asset: " + name, 100);
            } else {
                logger.warning("Unknown asset type for " + key + ". Type is " + value);
            }
        }
    }

    private String getFileName(String url) {
        String[] split = url.split("/");
        return split[split.length - 1];
    }

    private void updateFiles() {
        NekocraftDirectories dirs = new NekocraftDirectories();
        File oldConfig = new File(Utils.getWorkingDirectory(), ".minecraft");
        if (oldConfig.exists() && oldConfig.isDirectory()) {
            moveDirectory(oldConfig, dirs.getSpoutcraftDir());
            FileUtils.deleteQuietly(oldConfig);
        }
    }

    private void moveDirectory(File dir, File newDir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                moveDirectory(file, new File(newDir, file.getName()));
            } else {
                file.renameTo(new File(newDir, file.getName()));
            }
        }
    }

    private void cleanTemp() {
        NekocraftDirectories dirs = new NekocraftDirectories();
        File binDir = dirs.getBinDir();
        for (File f : binDir.listFiles()) {
            if (f.isDirectory() && f.getName().startsWith("temp_")) {
                FileUtils.deleteQuietly(f);
            }
        }
    }

    private void cleanLogs() {
        File logDirectory = new File(Utils.getWorkingDirectory(), "logs");
        if (logDirectory.exists() && logDirectory.isDirectory()) {
            for (File log : logDirectory.listFiles()) {
                if (!log.getName().endsWith(".log")) {
                    log.delete();
                    continue;
                }

                if (!log.getName().startsWith("nekocraft")) {
                    log.delete();
                    continue;
                }

                String[] split = log.getName().split("_");
                if (split.length != 2) {
                    log.delete();
                    continue;
                }

                String[] date = split[1].split("-");
                if (date.length != 3) {
                    log.delete();
                    continue;
                }
                date[2] = date[2].substring(0, date[2].length() - 4); // Trim .log extension
                try {
                    int logYear = Integer.parseInt(date[0]);
                    int logMonth = Integer.parseInt(date[1]);
                    int logDay = Integer.parseInt(date[2]);

                    Calendar logDate = Calendar.getInstance();
                    // Add a month to the calendar (clear logs older than 1 month)
                    if (logMonth < 12) {
                        logMonth++;
                    } else {
                        logMonth = 1;
                        logYear++;
                    }
                    logDate.set(logYear, logMonth, logDay);

                    if (Calendar.getInstance().after(logDate)) {
                        log.delete();
                    }
                } catch (NumberFormatException ignore) {
                    log.delete();
                    continue;
                }
            }
        }
    }

    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    public boolean isFinished() {
        return finished.get();
    }

    public boolean isValidInstall() {
        return valid.get();
    }

    public boolean isSpoutcraftUpdateAvailable(NekocraftData build) throws RestfulAPIException {
        if (!Utils.getWorkingDirectory().exists()) {
            return true;
        }
        if (!Launcher.getGameUpdater().getSpoutcraftDir().exists()) {
            return true;
        }

        List<Library> libraries = build.getLibraries();
        int steps = libraries.size() + 2;
        float progress = 100F;

        stateChanged("检查更新....", progress / steps);
        progress += 100F;
        File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "spoutcraft.jar");
        if (!spoutcraft.exists()) {
            return true;
        }
        if (!Settings.isIgnoreMD5() && !build.getMD5().equalsIgnoreCase(MD5Utils.getMD5(spoutcraft))) {
            return true;
        }
        stateChanged("检查更新....", progress / steps);
        progress += 100F;
        File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
        libDir.mkdir();

        for (Library lib : libraries) {
            File libraryFile = new File(libDir, lib.name() + ".jar");
            if (!libraryFile.exists()) {
                return true;
            }
            stateChanged("检查更新....", progress / steps);
            progress += 100F;
        }

        return false;
    }

    public boolean isMinecraftUpdateAvailable(NekocraftData build) {
        int steps = 7;
        if (!Launcher.getGameUpdater().getBinDir().exists()) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 100F / steps);
        File nativesDir = new File(Launcher.getGameUpdater().getBinDir(), "natives");
        if (!nativesDir.exists()) {
            return true;
        }
        // Empty dir
        if (nativesDir.listFiles().length == 0) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 200F / steps);
        File minecraft = new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar");
        if (!minecraft.exists()) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 300F / steps);
        File lib = new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar");
        if (!lib.exists()) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 400F / steps);
        lib = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar");
        if (!lib.exists()) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 500F / steps);
        lib = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar");
        if (!lib.exists()) {
            return true;
        }
        stateChanged("Checking for Minecraft update...", 600F / steps);
        String installed = Settings.getInstalledMC();
        stateChanged("Checking for Minecraft update...", 700F / steps);
        String required = build.getMinecraft().getVersion();
        return installed == null || !installed.equals(required);
    }

    // Searches the MC libraries for the one matching the given artifact id, returns the md5
    protected static String findMd5(String artifactId, OperatingSystem platform, List<Library> libraries) {
        for (Library lib : libraries) {
            if (lib.getArtifactId().equalsIgnoreCase(artifactId)) {
                if (platform == null
                        || platform.isWindows() && lib.getVersion().toLowerCase().endsWith("windows")
                        || platform.isMac() && lib.getVersion().toLowerCase().endsWith("osx")
                        || platform.isUnix() && lib.getVersion().toLowerCase().endsWith("linux")) {
                    return lib.getMd5();
                }
            }
        }
        // Should never get here...
        throw new IllegalArgumentException("No valid library for " + artifactId + ", with platform " + platform + " was found");
    }

    public void updateMinecraft(NekocraftData build) throws IOException {
        Launcher.getGameUpdater().getBinDir().mkdir();
        Launcher.getGameUpdater().getBinCacheDir().mkdir();
        if (Launcher.getGameUpdater().getUpdateDir().exists()) {
            FileUtils.deleteDirectory(Launcher.getGameUpdater().getUpdateDir());
        }
        Launcher.getGameUpdater().getUpdateDir().mkdir();

        final Minecraft minecraft = build.getMinecraft();
        final String minecraftMD5 = minecraft.getMd5();
        final String jinputMD5 = findMd5("jinput", null, minecraft.getLibraries());
        final String lwjgl_utilMD5 = findMd5("lwjgl_util", null, minecraft.getLibraries());
        final String lwjglMD5 = findMd5("lwjgl", null, minecraft.getLibraries());

        // Processs minecraft.jar
        logger.info("Minecraft Version: " + build.getMinecraft());
        File mcCache = new File(Launcher.getGameUpdater().getBinCacheDir(), "minecraft_" + minecraft.getVersion() + ".jar");
        if (!mcCache.exists() || (minecraftMD5 == null || !minecraftMD5.equals(MD5Utils.getMD5(mcCache)))) {
            DownloadUtils.downloadFile("http://assets.minecraft.net/" + minecraft.getVersion().replaceAll("\\.", "_") + "/minecraft.jar", mcCache.getPath(), null, minecraftMD5, listener);
        }
        Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar"));

        File nativesDir = new File(Launcher.getGameUpdater().getBinDir(), "natives");
        nativesDir.mkdir();

        // Process other downloads
        mcCache = new File(Launcher.getGameUpdater().getBinCacheDir(), "jinput.jar");
        if (!mcCache.exists() || !jinputMD5.equals(MD5Utils.getMD5(mcCache))) {
            DownloadUtils.downloadFile(getNativesUrl() + "jinput.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "jinput.jar", "jinput.jar", jinputMD5, listener);
        } else {
            Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar"));
        }

        mcCache = new File(Launcher.getGameUpdater().getBinCacheDir(), "lwjgl.jar");
        if (!mcCache.exists() || !lwjglMD5.equals(MD5Utils.getMD5(mcCache))) {
            DownloadUtils.downloadFile(getNativesUrl() + "lwjgl.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "lwjgl.jar", "lwjgl.jar", lwjglMD5, listener);
        } else {
            Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar"));
        }

        mcCache = new File(Launcher.getGameUpdater().getBinCacheDir(), "lwjgl_util.jar");
        if (!mcCache.exists() || !lwjgl_utilMD5.equals(MD5Utils.getMD5(mcCache))) {
            DownloadUtils.downloadFile(getNativesUrl() + "lwjgl_util.jar", Launcher.getGameUpdater().getBinDir().getPath() + File.separator + "lwjgl_util.jar", "lwjgl_util.jar", lwjgl_utilMD5, listener);
        } else {
            Utils.copy(mcCache, new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar"));
        }

        try {
            downloadNatives(minecraft);
        } catch (Exception e) {
            e.printStackTrace();
        }

        stateChanged("Extracting Files...", 0);

        Settings.setInstalledMC(build.getMinecraft().getVersion());
        Settings.getYAML().save();
    }

    public String getNativesUrl() {
        return GameUpdater.baseURL;
    }

    public void downloadNatives(Minecraft minecraft) throws IOException, UnsupportedOSException {
        final String url;

        OperatingSystem os = OperatingSystem.getOS();
        if (os.isUnix()) {
            url = LINUX_NATIVES_URL;
        } else if (os.isMac()) {
            url = OSX_NATIVES_URL;
        } else if (os.isWindows()) {
            url = WINDOWS_NATIVES_URL;
        } else {
            throw new UnsupportedOperationException("Unknown OS: " + os);
        }
        final String md5 = findMd5("lwjgl-platform", OperatingSystem.getOS(), minecraft.getLibraries());

        // Download natives
        File nativesJar = new File(Launcher.getGameUpdater().getUpdateDir(), "natives.jar");
        DownloadUtils.downloadFile(url, nativesJar.getPath(), null, md5, listener);

        // Extract natives
        List<String> ignores = new ArrayList<String>();
        ignores.add("META-INF");
        File tempNatives = new File(Launcher.getGameUpdater().getUpdateDir(), "natives");
        Utils.extractJar(new JarFile(nativesJar), tempNatives, ignores);
        FileUtils.moveDirectory(tempNatives, new File(Launcher.getGameUpdater().getBinDir(), "natives"));
    }

    public void updateSpoutcraft(NekocraftData build) throws IOException {
        cleanupBinFoldersFor(build);

        Launcher.getGameUpdater().getUpdateDir().mkdirs();
        Launcher.getGameUpdater().getBinCacheDir().mkdirs();
        Launcher.getGameUpdater().getSpoutcraftDir().mkdirs();
        File cacheDir = new File(Launcher.getGameUpdater().getBinDir(), "cache");
        cacheDir.mkdir();

        // Process spoutcraft.jar
        logger.info("Spoutcraft Build: " + build.getBuild());
        File mcCache = new File(Launcher.getGameUpdater().getBinCacheDir(), "minecraft_" + build.getMinecraft().getVersion() + ".jar");
        File updateMC = new File(Launcher.getGameUpdater().getUpdateDir().getPath() + File.separator + "minecraft.jar");
        if (mcCache.exists()) {
            Utils.copy(mcCache, updateMC);
        }

        File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "spoutcraft.jar");
        if (spoutcraft.exists()) {
            File spoutcraftCache;
            if (Integer.parseInt(build.getInstalledBuild()) > 0) {
                // Save our installed copy
                spoutcraftCache = new File(cacheDir, "spoutcraft_" + build.getInstalledBuild() + ".jar");
                if (!spoutcraftCache.exists()) {
                    Utils.copy(spoutcraft, spoutcraftCache);
                }
            }
            spoutcraft.delete();
            // Check for an old copy of this build if it is already saved
            spoutcraftCache = new File(cacheDir, "spoutcraft_" + build.getBuild() + ".jar");
            if (spoutcraftCache.exists()) {
                Utils.copy(spoutcraftCache, spoutcraft);
            }
        }

        stateChanged("Looking Up Mirrors...", 0F);

        String url = build.getSpoutcraftURL();

        if (!spoutcraft.exists()) {
            Download download = DownloadUtils.downloadFile(url, Launcher.getGameUpdater().getUpdateDir() + File.separator + "spoutcraft.jar", null, build.getMD5(), listener);
            if (download.getResult() == Result.SUCCESS) {
                Utils.copy(download.getOutFile(), spoutcraft);
            }
        }

        File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
        libDir.mkdir();

        List<Library> libraries = build.getLibraries();
        for (Library lib : libraries) {
            File libraryFile = new File(libDir, lib.name() + ".jar");
            if (libraryFile.exists()) {
                String computedMD5 = MD5Utils.getMD5(libraryFile);
                if (!lib.valid(computedMD5)) {
                    logger.warning("MD5 check of " + libraryFile.getName() + " failed. Deleting and Redownloading.");
                    libraryFile.delete();
                }
            }
            File cachedLibraryFile = new File(cacheDir, lib.name() + ".jar");
            if (cachedLibraryFile.exists()) {
                String computedMD5 = MD5Utils.getMD5(cachedLibraryFile);
                if (lib.valid(computedMD5)) {
                    Utils.copy(cachedLibraryFile, libraryFile);
                }
            }
            if (!libraryFile.exists()) {
                lib.download(libraryFile, listener);
            }
        }
    }

    public void cleanupBinFoldersFor(NekocraftData build) {
        try {
            if (!Launcher.getGameUpdater().getBinDir().exists()) {
                return;
            }

            HashSet<String> neededBinFiles = new HashSet<String>(Arrays.asList(new String[]{"spoutcraft.jar", "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"}));
            for (File file : Launcher.getGameUpdater().getBinDir().listFiles()) {
                if (!file.isFile()) {
                    continue;
                }
                if (neededBinFiles.contains(file.getName())) {
                    continue;
                }
                file.delete();
            }
        } catch (Exception e) {
            System.err.println("Error while cleaning files: ");
            e.printStackTrace();
        }
    }

    public DownloadListener getDownloadListener() {
        return listener;
    }

    public void setDownloadListener(DownloadListener listener) {
        ((DownloadListenerWrapper) this.listener).setDownloadListener(listener);
    }

    public void stateChanged(String message, float progress) {
        if (listener != null) {
            listener.stateChanged(message, progress);
        }
    }

    private class DownloadListenerWrapper implements DownloadListener {
        private final AtomicReference<DownloadListener> wrapped = new AtomicReference<DownloadListener>(null);

        public void stateChanged(String fileName, float progress) {
            fileName = (new File(fileName)).getName();
            DownloadListener listener = wrapped.get();
            if (listener != null) {
                listener.stateChanged(fileName, progress);
            }
        }

        public void setDownloadListener(DownloadListener listener) {
            wrapped.set(listener);
        }
    }
}