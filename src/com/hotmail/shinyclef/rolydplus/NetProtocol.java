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
    public static final String GOLD = COLOUR_CHAR + "6";
    public static final String PINK = COLOUR_CHAR + "d";
    public static final String YELLOW = COLOUR_CHAR + "e";

    protected static final String CHAT_MARKER = "*";
    protected static final String CUSTOM_COMMAND_MARKER = "@";
    protected static final String MC_COMMAND_MARKER = "/";
    public static final String PING = "P";

    public static final String QUIT_MESSAGE = CUSTOM_COMMAND_MARKER + "Disconnect";
    public static final String QUIT_MESSAGE_CLOSING = QUIT_MESSAGE + ":Closing";
    public static final String QUIT_MESSAGE_TIMEOUT = QUIT_MESSAGE + ":Timeout";
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

        switch (input.substring(0, 1))
        {
            case CHAT_MARKER:
                processChat(input);
                break;

            case CUSTOM_COMMAND_MARKER:
                processCustomCommand(input);
                break;

            case MC_COMMAND_MARKER:
                processMCCommand(input);
                break;

            default:
                System.out.println("WARNING: processInput()'s default case was triggered with input: " + input);
                break;
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

        switch (args[0])
        {
            case "Disconnect":
                NetProtocolHelper.processDisconnect(args);
                break;

            case "Login":
                NetProtocolHelper.processLoginReply(args);
                break;

            case "PlayerList":
                NetProtocolHelper.processOnlineList(args[1]);
                break;

            case "ServerJoin": case "ServerQuit": case "ClientJoin": case "ClientQuit":
                if (RolyDPlus.hasLoggedIn())
                {
                    NetProtocolHelper.processOnlineChange(args[0], args[1], args[2]);
                }
                break;
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
        //don't process any chat if client has not logged in yet
        if (!RolyDPlus.hasLoggedIn())
        {
            return;
        }

        if (input.length() < 3)
        {
            return;
        }

        //remove first character and send it to chat
        String message = input.substring(1);

        //transform
        FramesManager.getFrameChat().writeColouredLine(message);
    }

    public static void processTimeout()
    {
        NetProtocol.processDisconnect("Connection timed out. Attempting to reconnect.");
        RolyDPlus.reconnect();
    }

    public static void processDisconnect(String disconnectMsg)
    {
        //stop pinger
        RolyDPlus.getPinger().interrupt();

        //send poison pill to out
        processOutput(POISON_PILL_OUT);

        //close socket
        RolyDPlus.closeSocket();

        //inform user that server has shut down
        String message = PINK + disconnectMsg;
        FramesManager.getFrameChat().writeColouredLine(message);

        //set to disconnected mode
        RolyDPlus.setConnected(false);
        FramesManager.disableServerInteraction();
    }
}
