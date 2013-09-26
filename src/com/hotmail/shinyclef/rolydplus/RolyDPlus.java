package com.hotmail.shinyclef.rolydplus;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 2:28 AM
 */

public class RolyDPlus
{
    public static final boolean DEV_BUILD = false;
    public static final boolean LOCAL_SERVER = true;
    public static final String VERSION = "1.0.1";
    private static boolean isConnected = false;
    private static boolean hasLoggedIn = false;

    private static String SERVER_IP = "0.0.0.0";
    private static int SERVER_PORT = 0;
    private static Thread pinger;
    private static final int TIMEOUT_SECONDS = 30;
    public static final int PING_INTERVAL_SECONDS = 25;

    private static String username;
    private static String password;
    private static boolean isMod;

    private static Socket socket;
    private static boolean hasDisconnected = false;

    public static void main(String[] args)
    {
        String connectionError = "Sorry, it looks like you can't connect to RolyDPlus.\n" +
                "This happens when the server is down, when the R+ service is disabled,\n" +
                "or when something else is preventing you from reaching rolyd.com.\n" +
                "Please try again later!";

        try
        {
            setupConnection();
        }
        catch (UnknownHostException e)
        {
            JOptionPane.showConfirmDialog(new JFrame(), connectionError, "Cannot Connect",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (DEV_BUILD)
            {
                System.err.println("Don't know about host: " + SERVER_IP + ":" + SERVER_PORT);
            }
            initializeExit(0);
        }
        catch (IOException e)
        {
            JOptionPane.showConfirmDialog(new JFrame(), connectionError, "Cannot Connect",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (DEV_BUILD)
            {
                System.err.println("Couldn't get I/O for the connection to: " + SERVER_IP + ":" + SERVER_PORT);
            }
            initializeExit(0);
        }

        FramesManager.instantiateFrames();
        FramesManager.showFirstFrame();
    }

    private static void setupConnection() throws IOException
    {
        if (LOCAL_SERVER)
        {
            SERVER_IP = "192.168.1.2";
            SERVER_PORT = 12004;
        }
        else //roly's server
        {
            SERVER_IP = "www.rolyd.com";
            SERVER_PORT = 14890;
        }

        //socket & reader/writer
        socket = new Socket(SERVER_IP, SERVER_PORT);
        socket.setSoTimeout(TIMEOUT_SECONDS * 1000);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        //setup
        NetServerIn netServerIn = new NetServerIn(bufferedReader);
        NetServerOut netServerOut = new NetServerOut(printWriter);

        //start threads
        new Thread(netServerIn).start();
        new Thread(netServerOut).start();

        pinger = new Thread(new Pinger());
        pinger.start();

        //set serverQueue in NetProtocol
        NetProtocol.setToServerQueue();

        //set connected state
        isConnected = true;
    }

    public static void initializeExit(int code)
    {
        if (isConnected)
        {
            NetProtocol.processOutput(NetProtocol.QUIT_MESSAGE_CLOSING);
            int retries = 0;
            while(!hasDisconnected() && retries < 8)
            {
                try
                {
                    Thread.sleep(250); // 1/4 second
                }
                catch (InterruptedException e)
                {
                    Thread.interrupted();
                }
                retries++;
            }
            closeSocket();
        }
        System.exit(code);
    }

    public static void closeSocket()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            //already closed by server, swallow exception
        }
    }

    public static void login(boolean reconnecting, String[] args)
    {
        String userType = args[2];
        if (userType.equals(NetProtocolHelper.MOD_USER))
        {
            isMod = true;
        }
        else
        {
            isMod = false;
        }

        NetProtocolHelper.requestOnlineList();
        if (reconnecting)
        {
            FramesManager.enableServerInteraction();
        }
        else
        {
            FramesManager.getFrameChat().setVisible(true);
            FramesManager.getFrameLogin().setVisible(false);
            hasLoggedIn = true;
        }

    }

    public static void reconnect()
    {
        try
        {
            Thread.sleep(11500);
        }
        catch (InterruptedException e)
        {

        }

        //setup a new connection
        try
        {
            setupConnection();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + SERVER_IP + ":" + SERVER_PORT);
            initializeExit(0);
        }

        try
        {
            Thread.sleep(1500);
        }
        catch (InterruptedException e)
        {

        }

        //log back in if appropriate, or re-enable login frame controls
        if (hasLoggedIn())
        {
            NetProtocolHelper.attemptLogin(username, password);
        }
        else
        {
            FramesManager.enableServerInteraction();
            //FramesManager.getFrameLogin().enableControls();
        }
    }

    /* Getters */

    public static Thread getPinger()
    {
        return pinger;
    }

    public static boolean isConnected()
    {
        return isConnected;
    }

    public static boolean hasLoggedIn()
    {
        return hasLoggedIn;
    }

    public static synchronized boolean hasDisconnected()
    {
        return hasDisconnected;
    }

    public static String getUsername()
    {
        return username;
    }

    public static String getPassword()
    {
        return password;
    }

    public static boolean isMod()
    {
        return isMod;
    }

    /* Setters */

    public static void setConnected(boolean connected)
    {
        isConnected = connected;
    }

    public static synchronized void setHasDisconnected(boolean hasDisconnected)
    {
        RolyDPlus.hasDisconnected = hasDisconnected;
    }

    public static void setUsername(String username)
    {
        RolyDPlus.username = username;
    }

    public static void setPassword(String password)
    {
        RolyDPlus.password = password;
    }
}