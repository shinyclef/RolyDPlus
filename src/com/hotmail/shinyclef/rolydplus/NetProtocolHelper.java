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
        boolean isReconnecting = RolyDPlus.hasLoggedIn();

        //successful login
        if (args[1].equalsIgnoreCase("true"))
        {
            RolyDPlus.login(isReconnecting);
        }
        else //unsuccessful login
        {
            if (!isReconnecting)
            {
                FramesManager.getFrameLogin().unsuccessfulLoginReply();
            }
        }
    }

    public static void attemptLogin(String username, String password)
    {
        //formulate login request
        String message = "@Login:" + username + ":" + password;

        //send it
        processOutput(message);
    }

    public static void processDisconnect(String[] args)
    {

        //args[0] is "Disconnect. args[1] should be something.
        if (args.length < 2)
        {
            if (RolyDPlus.DEV_BUILD)
            {
                System.out.println("DEBUG: NetProtocolHelper.processDisconnect(): args length is < 2.");
            }
            return;
        }

        switch (args[1])
        {
            case "Quit":
                //do something
                break;

            case "Kick":
                //do something
                break;

            case "Ban":
                processBan("Ban", null, args[2]);
                break;

            case "TempBan":
                processBan("TempBan", args[2], args[3]);
                break;

            case "DuplicateLogin":
                processDuplicateLogin(args[2]);
                break;

            default:
                System.out.println("WARNING! NetProtocolHelper.processDisconnect()'s " +
                        "default case was triggered with: " +args[1]);
                break;
        }

    }

    private static void processBan(String type, String length, String reason)
    {
        String message = "";

        if (type.equals("Ban"))
        {
            message = GOLD + "You have been banned. Reason: " + YELLOW + reason;
        }
        else if (type.equals("TempBan"))
        {
            message = GOLD + "You have been temp-banned for " + YELLOW + length + GOLD + ". Reason: " +
                    YELLOW + reason + GOLD + "\nYou can re-register for RolyDPlus when your ban is lifted.";
        }

        FramesManager.getFrameChat().writeColouredLine(message);
    }

    private static void processDuplicateLogin(String message)
    {
        FramesManager.getFrameChat().writeColouredLine(message);
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
