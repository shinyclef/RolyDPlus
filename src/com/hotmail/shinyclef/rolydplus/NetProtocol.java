package com.hotmail.shinyclef.rolydplus;

import java.util.concurrent.BlockingQueue;

/**
 * User: Shinyclef
 * Date: 3/08/13
 * Time: 2:54 AM
 */

public class NetProtocol
{
    private static final String COLOUR_CHAR = String.valueOf('\u00A7');

    protected static final String CHAT_MARKER = "*";
    protected static final String CUSTOM_COMMAND_MARKER = "@";
    protected static final String MC_COMMAND_MARKER = "/";
    public static final String QUIT_MESSAGE = CUSTOM_COMMAND_MARKER + "Disconnect";
    public static final String POISON_PILL_OUT = CUSTOM_COMMAND_MARKER + "PoisonPill";

    private static final int SECOND_UNTIL_FIRST_RETRY = 20;
    private static final int SECONDS_BETWEEN_RETRIES = 5;
    private static final int MAX_RETRIES = 10;

    private static BlockingQueue<String> toServerQueue;

    public static void setToServerQueue()
    {
        toServerQueue = NetServerOut.getToServerQueue();
    }

    public static synchronized void processInput(String input)
    {
        //at least 2 characters
        if (input.length() < 2)
        {
            return;
        }

        if (input.startsWith("*")) //chat
        {
            processChat(input);
        }
        else if (input.startsWith("@")) //custom command
        {
            processCustomCommand(input);
        }
        else if (input.startsWith("/")) //mc command
        {
            processMCCommand(input);
        }
    }

    public static void processOutput(String output)
    {
        try
        {
            toServerQueue.put(output);
        }
        catch (InterruptedException e)
        {

        }
    }

    private static void processCustomCommand(String input)
    {
        if (input.length() < 3)
        {
            return;
        }

        //remove first character and get args
        String command = input.substring(1);
        String[] args = command.split(":");

        if (args[0].equals("Login"))
        {
            NetProtocolHelper.processLoginReply(args);
        }

        else if (args[0].equals("PlayerList"))
        {
            NetProtocolHelper.processOnlineList(args[1]);
        }

        else if (args[0].equals("ServerJoin") || args[0].equals("ServerQuit") ||
                args[0].equals("ClientJoin") || args[0].equals("ClientQuit"))
        {
            NetProtocolHelper.processOnlineChange(args[0], args[1], args[2]);
        }
    }

    private static void processMCCommand(String input)
    {
        if (input.length() < 3)
        {
            return;
        }

        //remove first character and get args
        String command = input.substring(1);
        String[] args = command.split(":");

        if (args[0].equals(""))
        {

        }
    }

    private static void processChat(String input)
    {
        if (input.length() < 3)
        {
            return;
        }

        //remove first character and send it to chat
        String message = input.substring(1);

        //transform
        FramesManager.getFrameChat().writeColouredLine(message);
    }

    public static void processServerShutdown()
    {
        //send poison pill to out
        processOutput(POISON_PILL_OUT);

        //inform user that server has shut down
        String pink = COLOUR_CHAR + "d";
        String message = pink + "Server has shut down. Restart RolyDPlus when server has restarted.";
        FramesManager.getFrameChat().writeColouredLine(message);

        //set to disconnected mode
        RolyDPlus.setConnected(false);
        FramesManager.disableServerInteraction();

        //initialize auto reconnect
    }
}
