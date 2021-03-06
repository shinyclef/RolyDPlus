package com.hotmail.shinyclef.rolydplus;

import java.io.PrintWriter;
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
                if (RolyDPlus.DEV_BUILD)
                {
                    System.out.println("Out: " + msgOut);
                }

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

        RolyDPlus.setHasDisconnected(true);

        if (RolyDPlus.DEV_BUILD)
        {
            System.out.println("NetServerOut closing.");
        }
    }

    public static BlockingQueue<String> getToServerQueue()
    {
        return toServerQueue;
    }
}
