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
package com.nekocraft.launcher.util;

import com.nekocraft.launcher.StartupParameters;
import com.nekocraft.launcher.exceptions.*;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
    private static File workDir = null;
    private static StartupParameters params = null;

    public static File getWorkingDirectory() {
        if (workDir == null) {
            workDir = getWorkingDirectory(".minecraft");
        }
        return workDir;
    }

    public static File getAssetsDirectory() {
        return new File(getWorkingDirectory(), "assets");
    }

    public static void setStartupParameters(StartupParameters params) {
        Utils.params = params;
    }

    public static StartupParameters getStartupParameters() {
        return params;
    }

    public static File getWorkingDirectory(String applicationName) {

        return new File(applicationName);
    }

    public static String executePost(String targetURL, String urlParameters, JProgressBar progress) throws PermissionDeniedException {
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(targetURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setConnectTimeout(10000);

            connection.connect();
            Certificate[] certs = connection.getServerCertificates();

            byte[] bytes = new byte[294];
            DataInputStream dis = new DataInputStream(StartupParameters.class.getResourceAsStream("resources/nekocraft.key.cer"));
            dis.readFully(bytes);
            dis.close();

            Certificate c = certs[0];
            PublicKey pk = c.getPublicKey();
            byte[] data = pk.getEncoded();

            for (int j = 0; j < data.length; j++) {
                if (data[j] == bytes[j]) {
                    continue;
                }
                //throw new RuntimeException("Public key mismatch");
            }

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            return response.toString();
        } catch (SocketException e) {
            if (e.getMessage().equalsIgnoreCase("Permission denied: connect")) {
                throw new PermissionDeniedException("Permission to login was denied");
            }
        } catch (Exception e) {
            String message = "Login failed...";
            progress.setString(message);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static String getFileExtention(String file) {
        if (!file.contains(".")) {
            return null;
        }

        return file.substring(file.lastIndexOf(".") + 1, file.length());
    }

    public static void copy(File input, File output) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(input);
            outputStream = new FileOutputStream(output);
            copy(inputStream, outputStream);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignore) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 16];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }

        return count;
    }

    public static String doLogin(String user, String pass, JProgressBar progress) throws BadLoginException, MCNetworkException, OutdatedMCLauncherException, UnsupportedEncodingException, MinecraftUserNotPremiumException, PermissionDeniedException {
        String parameters = "user=" + URLEncoder.encode(user, "UTF-8") + "&pass=" + URLEncoder.encode(pass, "UTF-8");
        String result = executePost("https://nekocraft.com/api/login/", parameters, progress);
        if (result.equals("")) {
            throw new BadLoginException();
        }
        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static void extractJar(JarFile jar, File dest) throws IOException {
        extractJar(jar, dest, null);
    }

    public static void extractJar(JarFile jar, File dest, List<String> ignores) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        } else {
            if (!dest.isDirectory()) {
                throw new IllegalArgumentException("The destination was not a directory");
            }
            FileUtils.cleanDirectory(dest);
        }
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            File file = new File(dest, entry.getName());
            if (ignores != null) {
                boolean skip = false;
                for (String path : ignores) {
                    if (entry.getName().startsWith(path)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }

            if (entry.getName().endsWith("/")) {
                if (!file.mkdir()) {
                    if (ignores == null) {
                        ignores = new ArrayList<String>();
                    }
                    ignores.add(entry.getName());
                }
                continue;
            }

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

            byte buffer[] = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
        }
        jar.close();
    }
}
