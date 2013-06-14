/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nekocraft.launcher;
import java.io.*;
import java.net.*;
/**
 *
 * @author Administrator
 */
public class LoginThread implements Runnable{
    public static final String API="https://nekocraft.com/api/login/";
    //API usage : http://nekocraft.com/mclogin/?user=---&password=---
    private String user;
    private String password;
    private URL u;
    private HttpURLConnection con;
    private String session;
    public void run() {
        try{
            u=new URL(API);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            osw.write("user="+user+"&password="+password);
            osw.flush();
            osw.close();
            InputStream in=con.getInputStream();
            int count = 0;
            while (count == 0) {
             count = in.available();
             }
             byte[] b = new byte[count];
             in.read(b);
            session=new String(b);
        }
        catch (Exception e){
            NekoLauncher.handleException(e);
        }
    }
}
