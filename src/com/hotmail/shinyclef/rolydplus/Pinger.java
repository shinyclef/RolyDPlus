package com.hotmail.shinyclef.rolydplus;

/**
 * User: Shinyclef
 * Date: 16/09/13
 * Time: 8:52 PM
 */

public class Pinger implements Runnable
{
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                //wait for ping interval and send another ping
                Thread.sleep(RolyDPlus.PING_INTERVAL_SECONDS * 1000);
                NetProtocol.processOutput(NetProtocol.PING);
            }
        }
        catch (InterruptedException e)
        {

        }

        if (RolyDPlus.DEV_BUILD)
        {
            System.out.println("Pinger closing.");
        }
    }
}
