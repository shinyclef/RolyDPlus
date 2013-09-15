package com.hotmail.shinyclef.rolydplus;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 2:49 AM
 */

public class NetServerOut implements Runnable
{
    private static BlockingQueue<String> toServerQueue;
    private PrintWriter out;

    public NetServerOut(PrintWriter out)
    {
        this.out = out;
        toServerQueue = new ArrayBlockingQueue<String>(50);
    }

    @Override
    public void run()
    {
        try
        {
            String msgOut = "";

            //read outPut queue
            while (!msgOut.startsWith(NetProtocol.QUIT_MESSAGE))
            {
                msgOut = toServerQueue.take();
                if (msgOut.startsWith(NetProtocol.POISON_PILL_OUT))
                {
                    break;
                }
                out.println(msgOut);
                out.flush();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        RolyDPlus.setReadyToQuit(true);
    }

    public static BlockingQueue<String> getToServerQueue()
    {
        return toServerQueue;
    }

    private class Pinger implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    //wait 28 seconds
                    wait(60000);

                    //send another ping
                    NetProtocol.processOutput(NetProtocol.PING);
                }

            }
            catch (InterruptedException e)
            {
                FramesManager.getFrameChat().writeColouredLine("An error has occurred pinging server: " +
                        e.getMessage());
            }
        }
    }
}
