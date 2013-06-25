//Link:http://gaofeihang.blog.163.com/blog/static/845082852010315103625825/
package com.nekocraft.launcher;
import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTMLFetcher {
    public static String getHTML(String pageURL, String encoding) {
        StringBuilder pageHTML = new StringBuilder();
        try {
            URL url = new URL(pageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "MSIE 7.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
            String line = null;
            while ((line = br.readLine()) != null) {
                pageHTML.append(line);
                pageHTML.append("\r\n");
            }
            connection.disconnect();
        } catch (Exception e) {
            NekoLauncher.handleException(e);
        }
        return pageHTML.toString();
    }    
} 