package com.hotmail.shinyclef.rolydplus;

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
    private static final boolean development = true;
    private static boolean isConnected = false;
    private static boolean isLoggedIn = false;

    private static String SERVER_IP = "0.0.0.0";
    private static int SERVER_PORT = 0;

    private static Socket socket;
    private static boolean readyToQuit = false;

    private static String userName;
    private static String password;

    public static void main(String[] args)
    {
        try
        {
            setupConnection();
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host: " + SERVER_IP + ":" + SERVER_PORT);
            initializeExit(0);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + SERVER_IP + ":" + SERVER_PORT);
            initializeExit(0);
        }

        FramesManager.instantiateFrames();
        FramesManager.showFirstFrame();
    }

    private static void setupConnection() throws UnknownHostException, IOException
    {
        if (development)
        {
            SERVER_IP = "192.168.1.2";
            SERVER_PORT = 12003;
        }
        else //roly's server
        {
            SERVER_IP = "www.rolyd.com";
            SERVER_PORT = 14890;
        }

        //socket & reader/writer
        socket = new Socket(SERVER_IP, SERVER_PORT);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        //setup
        NetServerIn netServerIn = new NetServerIn(bufferedReader);
        NetServerOut netServerOut = new NetServerOut(printWriter);

        //start threads
        new Thread(netServerIn).start();
        new Thread(netServerOut).start();

        //set serverQueue in NetProtocol
        NetProtocol.setToServerQueue();

        //set connected state
        isConnected = true;
    }

    public static void initializeExit(int code)
    {
        if (isConnected)
        {
            NetProtocol.processOutput(NetProtocol.QUIT_MESSAGE);
            int retries = 0;
            while(!isReadyToQuit() && retries < 8)
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

    private static void closeSocket()
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

    public static void login()
    {
        NetProtocolHelper.requestOnlineList();
        FramesManager.getFrameChat().setVisible(true);
        FramesManager.getFrameLogin().setVisible(false);
        isLoggedIn = true;
    }

    /* Getters */

    public static boolean isConnected()
    {
        return isConnected;
    }

    public static boolean isLoggedIn()
    {
        return isLoggedIn;
    }

    public static synchronized boolean isReadyToQuit()
    {
        return readyToQuit;
    }

    /* Setters */

    public static void setConnected(boolean connected)
    {
        isConnected = connected;
    }

    public static synchronized void setReadyToQuit(boolean readyToQuit)
    {
        RolyDPlus.readyToQuit = readyToQuit;
    }
}
