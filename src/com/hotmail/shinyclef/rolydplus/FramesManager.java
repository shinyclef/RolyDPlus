package com.hotmail.shinyclef.rolydplus;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * User: Shinyclef
 * Date: 11/08/13
 * Time: 8:36 PM
 */

public class FramesManager
{
    //frames
    private static FrameLogin frameLogin;
    private static FrameChat frameChat;
    public static volatile boolean isReady = false;

    /* Instantiates all frames safely (avoiding concurrency problems). Does not make any visible. */
    public static void instantiateFrames()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                frameLogin = new FrameLogin();
                frameChat = new FrameChat();
                setFramesIcon();
                isReady = true;
            }
        });
    }

    /* Makes the login frame visible, beginning user interaction. */
    public static void showFirstFrame()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                frameLogin.setVisible(true);
            }
        });
    }

    public static void disableServerInteraction()
    {
        frameChat.disableServerInteraction();
        frameLogin.disableControls();
    }

    public static void enableServerInteraction()
    {
        frameChat.enableServerInteraction();
        frameLogin.enableControls();
    }

    private static void setFramesIcon()
    {
        URL imageURL = RolyDPlus.class.getResource("/images/icon16.png");
        frameLogin.setIconImage(Toolkit.getDefaultToolkit().getImage(imageURL));
        frameChat.setIconImage(Toolkit.getDefaultToolkit().getImage(imageURL));
    }


    /* ---------- Getters ---------- */

    public static FrameLogin getFrameLogin()
    {
        return frameLogin;
    }

    public static FrameChat getFrameChat()
    {
        return frameChat;
    }
}
