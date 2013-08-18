package com.hotmail.shinyclef.rolydplus;

/**
 * User: Shinyclef
 * Date: 14/08/13
 * Time: 4:03 AM
 */

public class NetProtocolHelper extends NetProtocol
{
    private static final String COLOUR_CHAR = String.valueOf('\u00A7');
    private static final String ONLINE_LIST_REQUEST = CUSTOM_COMMAND_MARKER + "RequestPlayerList";


    public static void processLoginReply(String[] args)
    {
        boolean reply;
        if (args[1].equalsIgnoreCase("true"))
        {
            reply = true;
        }
        else
        {
            reply = false;
        }

        FramesManager.getFrameLogin().loginReply(reply);
    }

    public static void attemptLogin(String username, String password)
    {
        //formulate login request
        String message = "@Login:" + username + ":" + password;

        //send it
        processOutput(message);
    }

    public static void requestOnlineList()
    {
        processOutput(ONLINE_LIST_REQUEST);
    }

    public static void processOnlineList(String onlinePlayerList)
    {
        FramesManager.getFrameChat().processFormattedOnlinePlayersList(onlinePlayerList);
    }

    public static void processOnlineChange(String action, String playerName, String currentPresence)
    {
        //get action String
        String actionMsg = "";
        if (action.equals("ServerJoin"))
        {
            actionMsg = " joined the game!";
        }
        else if (action.equals("ServerQuit"))
        {
            actionMsg = " left the game!";
        }
        else if (action.equals("ClientJoin"))
        {
            actionMsg = " joined RolyDPlus!";
        }
        else if (action.equals("ClientQuit"))
        {
            actionMsg = " left RolyDPlus!";
        }

        //write to chat
        String message = COLOUR_CHAR + "f" + playerName + COLOUR_CHAR + "e" + actionMsg;
        FramesManager.getFrameChat().writeColouredLine(message);

        //change online list
        FramesManager.getFrameChat().processOnlineChangeEvent(playerName, currentPresence);
    }
}
