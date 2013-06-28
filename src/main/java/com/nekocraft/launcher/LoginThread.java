/*
 * This File is a part of NekoLauncher
 * of Nekocraft
 */
package com.nekocraft.launcher;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
/**
 *
 * @author Administrator
 */
public class LoginThread extends Thread{
    private String user;
    private String password;
    private URL u;
    private HttpURLConnection con;
    private String session;
    @Override
    public void run() {
        LoginFrame.bar.setString("登录中...");
        try{
            u=new URL(StaticRes.API);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            osw.write("user="+getUser()+"&password="+getPassword());
            osw.flush();
            osw.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String line = null; 
            while ((line = br.readLine()) != null) {
                session=line;
            } 
            if (session==null){
                throw new LoginFailedException();
            }
            new DownloadThread().start();
        }
        catch (LoginFailedException e){
            JOptionPane.showMessageDialog(NekoLauncher.mf,"用户名或密码错误！");
            LoginFrame.bar.setString("请登录");
            LoginFrame.login.setEnabled(true);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(NekoLauncher.mf,"无法连接网络！");
            LoginFrame.bar.setString("请登录");
            LoginFrame.login.setEnabled(true);
        }
        catch (Exception e){
            NekoLauncher.handleException(e);
        }
        finally{
            con.disconnect();
        }
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the session
     */
    public String getSession() {
        return session;
    }
}
