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

    public static final String CORRECT = "Correct";
    public static final String STANDARD_USER = "Standard";
    public static final String MOD_USER = "Mod";
    public static final String INCORRECT = "Incorrect";
    public static final String REASON_NO_USER = "NoUser";
    public static final String REASON_BAD_PASSWORD = "UserPass";
    public static final String REASON_OUT_OF_DATE = "OutOfDate";
    public static final String DUPLICATE = "Duplicate";


    public static boolean isUpToDate(String serverVersion)
    {
        String[] verString = serverVersion.split("\\.");
        int[] clientVer = RolyDPlus.getClientVersionParts();
        int[] serverVer = new int[3];
        serverVer[0] = Integer.parseInt(verString[0]);
        serverVer[1] = Integer.parseInt(verString[1]);
        serverVer[2] = Integer.parseInt(verString[2]);

        if (serverVer[0] != clientVer[0])
        {
            if (serverVer[0] > clientVer[0])
            {
                return false;
            }

            if (serverVer[0] < clientVer[0])
            {
                return true;
            }
        }

        if (serverVer[1] != clientVer[1])
        {
            if (serverVer[1] > clientVer[1])
            {
                return false;
            }

            if (serverVer[1] < clientVer[1])
            {
                return true;
            }
        }

        if (serverVer[2] != clientVer[2])
        {
            if (serverVer[2] > clientVer[2])
            {
                return false;
            }

            if (serverVer[2] < clientVer[2])
            {
                return true;
            }
        }

        return true;
    }

    public static void processLoginReply(String[] args)
    {
        boolean isReconnecting = RolyDPlus.hasLoggedIn();

        //successful login check
        switch (args[1])
        {
            case CORRECT:
                RolyDPlus.login(isReconnecting, args);
                break;

            case INCORRECT:
                processFailedLoginReply(args[2]);
                break;

            default:
                if (RolyDPlus.DEV_BUILD)
                {
                    System.out.println("WARNING: Default case triggered in NetProtocolHelper.processLoginReply. " +
                            "args[1] == " + args[1]);
                }
                break;
        }
    }

    private static void processFailedLoginReply(String reason)
    {
        FramesManager.getFrameLogin().unsuccessfulLoginReply(reason);
    }

    public static void attemptLogin(String username, String password)
    {
        //formulate login request
        String message = "@Login:" + RolyDPlus.CLIENT_VERSION + ":" + username + ":" + password;

        //send it
        processOutput(message);
    }

    public static void logout()
    {
        processOutput(LOGOUT_MESSAGE);
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

    public static void processStatusChange(String playerName, String statusInfo)
    {
        //change online list
        FramesManager.getFrameChat().processStatusChangeEvent(playerName, statusInfo);
    }
}
