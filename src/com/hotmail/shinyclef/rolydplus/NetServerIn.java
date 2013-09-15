package com.hotmail.shinyclef.rolydplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 2:49 AM
 */

public class NetServerIn implements Runnable
{
    private BufferedReader in;

    public NetServerIn(BufferedReader in)
    {
        this.in = in;
    }

    @Override
    public void run()
    {
        String fromServer;

        try
        {
            while ((fromServer = in.readLine()) != null)
            {
                if (!fromServer.equals(""))
                {
                    NetProtocol.processInput(fromServer);
                }

                if (fromServer.startsWith(NetProtocol.QUIT_MESSAGE))
                {
                    //the quit message has already been sent to NetProtocol
                    break;
                }
            }
        }
        catch (IOException e)
        {
            FramesManager.getFrameChat().writeColouredLine(NetProtocol.PINK + "IO Exception: " + e.getMessage());
        }

        NetProtocol.processServerShutdown();
    }
}
