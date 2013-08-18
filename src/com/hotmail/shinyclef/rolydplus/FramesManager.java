package com.hotmail.shinyclef.rolydplus;

import javax.swing.*;

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
        frameChat.disableControls();
        frameLogin.disableControls();
    }

    public static void enableServerInteracton()
    {
        frameChat.enableControls();
        frameLogin.enableControls();
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
