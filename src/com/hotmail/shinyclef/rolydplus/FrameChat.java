package com.hotmail.shinyclef.rolydplus;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * User: Shinyclef
 * Date: 11/08/13
 * Time: 7:22 PM
 */

public class FrameChat extends JFrame
{
    private static final String COLOUR_CHAR = String.valueOf('\u00A7');
    private static Map<Character, Color> colourMap;
    private static Map<Character, SimpleAttributeSet> codeMap;
    private static Map<Integer, Character> colourCharMap;
    private static Map<Integer, Character> styleCharMap;
    private static Map<Integer, Character> magicCharMap;
    private static Map<Integer, Character> activeStyles;
    private static List<String> messageHistory;

    private static final int MESSAGE_HISTORY_LENGTH = 20;
    private static final int MAX_LINE_LENGTH = 200;
    private static final int MAX_LINES = 3;
    private static final Color CHAT_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .1f);
    private static final Color LIST_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .1f);

    private int nextMessageHistoryIndex = 0;
    private int currentLines = 0;

    private Container contentPane;
    private JPanel rightBox;
    private JTextPane textPane;
    private JScrollBar vScrollBar;
    private StyledDocument chatDoc;
    private JScrollPane listScrollPane;

    private JButton reconnectButton;
    private JTextField textField;
    private static JButton sendButton;

    private Map<String, OnlinePlayer> onlinePlayers;

    private SimpleAttributeSet style1;

    public FrameChat()
    {
        onlinePlayers = new TreeMap<>();
        colourCharMap = new LinkedHashMap<>();
        styleCharMap = new LinkedHashMap<>();
        magicCharMap = new LinkedHashMap<>();
        activeStyles = new LinkedHashMap<>();
        messageHistory = new LinkedList<>();
        setupColourMap();
        setupCodeMap();
        initializeUI();
    }

    public void initializeUI()
    {
        //basic frame attributes
        setTitle("RolyDPlus Chat");
        setSize(550, 420);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent we)
            {
                RolyDPlus.initializeExit(0);
                //setVisible(false);
            }
        });

        //get the content pane
        contentPane = getContentPane();

        //some containers for layout
        JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setBorder(new EmptyBorder(new Insets(5, 0, 5, 4)));
        JPanel bottomGridBag = new JPanel(new GridBagLayout());
        GridBagConstraints c;
        contentPane.add(topFlow, BorderLayout.NORTH);
        contentPane.add(bottomGridBag, BorderLayout.SOUTH);

        //create the components
        textPane = new JTextPane();
        textPane.setEditorKit(new WrapEditorKit()); //WORD WRAP BUG WORKAROUND
        JScrollPane chatScrollPane = new JScrollPane(textPane);
        listScrollPane = new JScrollPane(rightBox);
        vScrollBar = chatScrollPane.getVerticalScrollBar();
        reconnectButton = new JButton("Reconnect");
        textField = new JTextField();
        chatDoc = textPane.getStyledDocument();
        JButton logoutButton = new JButton("Logout");
        sendButton = new JButton("Send");

        //add the components to the panels
        contentPane.add(chatScrollPane, BorderLayout.CENTER);
        contentPane.add(listScrollPane, BorderLayout.EAST);
        topFlow.add(reconnectButton);
        topFlow.add(logoutButton);

        //set some properties
        textField.setPreferredSize(new Dimension(0, 25));
        textPane.setBackground(CHAT_BACKGROUND_COLOR);
        rightBox.setBackground(LIST_BACKGROUND_COLOR);
        Font buttonFont = new Font("Arial", 1, 10);
        sendButton.setFont(buttonFont);
        reconnectButton.setFont(buttonFont);
        reconnectButton.setPreferredSize(new Dimension(86, 15));
        reconnectButton.setVisible(false);
        logoutButton.setFont(buttonFont);
        sendButton.setPreferredSize(new Dimension(60, 24));
        logoutButton.setPreferredSize(new Dimension(68, 15));
        textPane.setEditable(false);
        listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //grid bag text field
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0.9;
        c.fill = GridBagConstraints.HORIZONTAL;
        bottomGridBag.add(textField, c);

        //grid bag send button
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        bottomGridBag.add(sendButton, c);

        //prepare document appending and style
        style1 = new SimpleAttributeSet();
        StyleConstants.setFontSize(style1, 14);
        StyleConstants.setLeftIndent(style1, 16);
        StyleConstants.setRightIndent(style1, 16);

        //the listeners
        ActionListener listener = new InputListener();
        MyKeyListener myKeyListener = new MyKeyListener();

        //add the listeners
        textField.addActionListener(listener);
        textField.addKeyListener(myKeyListener);
        sendButton.addActionListener(listener);
        reconnectButton.addActionListener(listener);
        logoutButton.addActionListener(listener);

        //text field listener to watch for max line length
        setupTextFieldDocumentListener();
    }

    private class InputListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String button = e.getActionCommand();
            if (button.equals("Logout"))
            {
                logout();
            }
            else if(button.equals("Reconnect"))
            {
                RolyDPlus.reconnect(0);
            }
            else //handles send button and text field
            {
                sendChatLine();
            }
        }
    }

    class MyKeyListener implements KeyListener
    {
        @Override
        public void keyTyped(KeyEvent e)
        {
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            if (e.getKeyCode() == 38)
            {
                cycleMessageHistory();
            }
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
        }
    }

    private class OnlinePlayer
    {
        public String playerName;
        public JLabel label;
        public JLabel picLabel;
        public char labelColourCode;
        public char onlineLocations;
        public char invisibleLocations;

        OnlinePlayer(String playerName, String statusInfo)
        {
            this.playerName = playerName;
            this.onlineLocations = statusInfo.charAt(0);
            this.invisibleLocations = statusInfo.charAt(1);
            this.labelColourCode = statusInfo.charAt(2);

            String prefix = getInvisibilityPrefix(statusInfo);
            label = new JLabel(prefix + playerName);
            label.setForeground(colourMap.get(labelColourCode));

            BufferedImage icon = getIcon(getVisibleOnlineStatus(statusInfo));
            picLabel = new JLabel(new ImageIcon(icon));
        }
    }

    private void setupTextFieldDocumentListener()
    {
        textField.getDocument().addDocumentListener(new DocumentListener()
        {
            Document doc = textField.getDocument();

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                if (doc.getLength() > MAX_LINE_LENGTH)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                int cursorPos = textField.getCaretPosition();
                                textField.setText(doc.getText(0, MAX_LINE_LENGTH));
                                textField.setCaretPosition(cursorPos > MAX_LINE_LENGTH ? MAX_LINE_LENGTH : cursorPos);
                            }
                            catch (BadLocationException e)
                            {
                                if (RolyDPlus.DEV_BUILD)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                //nothing needed
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                //nothing needed
            }
        });
    }

    private static void setupColourMap()
    {
        colourMap = new HashMap<>();
        colourMap.put('0', Color.getHSBColor(0, 0, 0));           //black
        colourMap.put('1', Color.getHSBColor(.666666f, 1, .67f)); //dark-blue
        colourMap.put('2', Color.getHSBColor(.333333f, 1, .67f)); //dark-green
        colourMap.put('3', Color.getHSBColor(.5f, 1, .67f));      //dark-aqua
        colourMap.put('4', Color.getHSBColor(0, 1, .67f));        //dark-red
        colourMap.put('5', Color.getHSBColor(.833333f, 1, .67f)); //dark-purple
        colourMap.put('6', Color.getHSBColor(.111111f, 1, 1));    //gold
        colourMap.put('7', Color.getHSBColor(0, 0, .67f));        //gray
        colourMap.put('8', Color.getHSBColor(.236111f, 0, .33f)); //dark-gray
        colourMap.put('9', Color.getHSBColor(.666666f, .67f, 1)); //blue
        colourMap.put('a', Color.getHSBColor(.333333f, .67f, 1)); //green
        colourMap.put('b', Color.getHSBColor(.5f, .67f, 1));      //aqua
        colourMap.put('c', Color.getHSBColor(0, .67f, 1));        //red
        colourMap.put('d', Color.getHSBColor(.833333f, .67f, 1)); //light-purple
        colourMap.put('e', Color.getHSBColor(.166666f, .67f, 1)); //yellow
        colourMap.put('f', Color.getHSBColor(0, 0, 1));           //white
    }

    private static void setupCodeMap()
    {
        //instantiate the attribute sets
        SimpleAttributeSet s0 = new SimpleAttributeSet(); //black
        SimpleAttributeSet s1 = new SimpleAttributeSet(); //dark-blue
        SimpleAttributeSet s2 = new SimpleAttributeSet(); //dark-green
        SimpleAttributeSet s3 = new SimpleAttributeSet(); //dark-aqua
        SimpleAttributeSet s4 = new SimpleAttributeSet(); //dark-red
        SimpleAttributeSet s5 = new SimpleAttributeSet(); //dark-purple
        SimpleAttributeSet s6 = new SimpleAttributeSet(); //gold
        SimpleAttributeSet s7 = new SimpleAttributeSet(); //gray
        SimpleAttributeSet s8 = new SimpleAttributeSet(); //dark-gray
        SimpleAttributeSet s9 = new SimpleAttributeSet(); //blue
        SimpleAttributeSet sa = new SimpleAttributeSet(); //green
        SimpleAttributeSet sb = new SimpleAttributeSet(); //aqua
        SimpleAttributeSet sc = new SimpleAttributeSet(); //red
        SimpleAttributeSet sd = new SimpleAttributeSet(); //light-purple
        SimpleAttributeSet se = new SimpleAttributeSet(); //yellow
        SimpleAttributeSet sf = new SimpleAttributeSet(); //white
        SimpleAttributeSet sr = new SimpleAttributeSet(); //reset
        SimpleAttributeSet sk = new SimpleAttributeSet(); //magic (obfuscation, randomly changing)
        SimpleAttributeSet sl = new SimpleAttributeSet(); //bold
        SimpleAttributeSet sm = new SimpleAttributeSet(); //strike-through
        SimpleAttributeSet sn = new SimpleAttributeSet(); //underline
        SimpleAttributeSet so = new SimpleAttributeSet(); //italic

        //assign mc colours to the attributes sets
        StyleConstants.setForeground(s0, colourMap.get('0')); //black
        StyleConstants.setForeground(s1, colourMap.get('1')); //dark-blue
        StyleConstants.setForeground(s2, colourMap.get('2')); //dark-green
        StyleConstants.setForeground(s3, colourMap.get('3')); //dark-aqua
        StyleConstants.setForeground(s4, colourMap.get('4')); //dark-red
        StyleConstants.setForeground(s5, colourMap.get('5')); //dark-purple
        StyleConstants.setForeground(s6, colourMap.get('6')); //gold
        StyleConstants.setForeground(s7, colourMap.get('7')); //gray
        StyleConstants.setForeground(s8, colourMap.get('8')); //dark-gray
        StyleConstants.setForeground(s9, colourMap.get('9')); //blue
        StyleConstants.setForeground(sa, colourMap.get('a')); //green
        StyleConstants.setForeground(sb, colourMap.get('b')); //aqua
        StyleConstants.setForeground(sc, colourMap.get('c')); //red
        StyleConstants.setForeground(sd, colourMap.get('d')); //light-purple
        StyleConstants.setForeground(se, colourMap.get('e')); //yellow
        StyleConstants.setForeground(sf, colourMap.get('f')); //white
        StyleConstants.setForeground(sk, Color.DARK_GRAY);  //magic
        StyleConstants.setBackground(sk, Color.DARK_GRAY);  //magic
        StyleConstants.setBold(sl, true);                   //bold
        StyleConstants.setStrikeThrough(sm, true);          //strike-through
        StyleConstants.setUnderline(sn, true);              //underline
        StyleConstants.setItalic(so, true);                 //italic
        //reset
        StyleConstants.setForeground(sr, Color.getHSBColor(0, 0, 1)); //reset to white
        StyleConstants.setBold(sr, false);          //reset bold
        StyleConstants.setStrikeThrough(sr, false); //reset strike-through
        StyleConstants.setUnderline(sr, false);     //reset underline
        StyleConstants.setItalic(sr, false);        //reset italic

        //put the attribute sets into the colour map
        codeMap = new HashMap<>();
        codeMap.put('0', s0);
        codeMap.put('1', s1);
        codeMap.put('2', s2);
        codeMap.put('3', s3);
        codeMap.put('4', s4);
        codeMap.put('5', s5);
        codeMap.put('6', s6);
        codeMap.put('7', s7);
        codeMap.put('8', s8);
        codeMap.put('9', s9);
        codeMap.put('a', sa);
        codeMap.put('b', sb);
        codeMap.put('c', sc);
        codeMap.put('d', sd);
        codeMap.put('e', se);
        codeMap.put('f', sf);
        codeMap.put('k', sk);
        codeMap.put('l', sl);
        codeMap.put('m', sm);
        codeMap.put('n', sn);
        codeMap.put('o', so);
        codeMap.put('r', sr);
    }

    public void addOrAlterPlayer(String playerName, String statusInfo)
    {
        //add player to list and redraw
        onlinePlayers.put(playerName.toLowerCase(), new OnlinePlayer(playerName, statusInfo));
        redrawList();
    }

    public void removePlayer(String playerName)
    {
        //remove player from online list and redraw
        onlinePlayers.remove(playerName.toLowerCase());
        redrawList();
    }

    //sorting can occur here
    private void redrawList()
    {
        rightBox.removeAll();
        for (OnlinePlayer listing : onlinePlayers.values())
        {
            JPanel listingContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            listingContainer.setBackground(LIST_BACKGROUND_COLOR);
            listingContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, 15));

            //add the image and name labels to the container
            listingContainer.add(listing.picLabel);
            listingContainer.add(listing.label);

            //add the container to player list box
            rightBox.add(listingContainer);
        }

        for (OnlinePlayer listing : onlinePlayers.values())
        {
            listing.label.revalidate();
        }

        revalidate();
        repaint();
    }

    private BufferedImage getIcon(char onlineLocationsChar)
    {
        String iconFileName;
        switch (onlineLocationsChar)
        {
            case 'S':
                iconFileName = "onlineserver.png";
                break;

            case 'C':
                iconFileName = "onlineclient.png";
                break;

            case 'B':
                iconFileName = "onlineboth.png";
                break;

            default:
                if (RolyDPlus.DEV_BUILD)
                {
                    System.out.println("Warning! Default case triggered in FrameChat.getIcon with char: " +
                            onlineLocationsChar);
                }
                iconFileName = "onlineserver.png";
                break;
        }

        URL imageURL = RolyDPlus.class.getResource("/images/" + iconFileName);
        BufferedImage icon;
        try
        {
            icon = ImageIO.read(imageURL);
        }
        catch (IOException e)
        {
            if (RolyDPlus.DEV_BUILD)
            {
                System.out.println("Warning! IOException in FrameChat.getIcon with path: " + imageURL);
            }
            return null;
        }

        return icon;
    }

    public void processFormattedOnlinePlayersList(String formattedListString)
    {
        onlinePlayers.clear();
        String[] onlineList = formattedListString.split(",");

        for (String listItem : onlineList)
        {
            /* 'S' server, 'C' client, 'B' both, 'N' none (should never happen)
            * first char upper case for online locations, second char lower case for invisible locations
            * third char is colour char.
            * '.' to separate special chars with name
            * eg. Ss.johnny, sammy, Cn.shiny, Ns:david */

            String statusInfo = listItem.substring(0, listItem.indexOf("."));
            String playerName = listItem.substring(listItem.indexOf(".") + 1);

            if (canSeeThisPlayer(statusInfo))
            {
                onlinePlayers.put(playerName.toLowerCase(), new OnlinePlayer(playerName, statusInfo));
            }
        }
        redrawList();
    }

    //handles the online list, not the chat notification
    public void processStatusChangeEvent(String playerName, String statusInfo)
    {
        //user has logged out of everything
        if (!canSeeThisPlayer(statusInfo))
        {
            removePlayer(playerName);
        }
        else
        {
            addOrAlterPlayer(playerName, statusInfo);
        }
    }

    private String getInvisibilityPrefix(String locationsInfo)
    {
        char onlineLocations = locationsInfo.charAt(0);
        char invisibleLocations = locationsInfo.charAt(1);

        //get invisibility prefix for mods
        if (RolyDPlus.isMod())
        {
            if (onlineLocations == 'S' && (invisibleLocations == 's' || invisibleLocations == 'b'))
            {
                return "(s)";
            }
            else if (onlineLocations == 'C' && (invisibleLocations == 'c' || invisibleLocations == 'b'))
            {
                return "(c)";
            }
            else if (onlineLocations == 'B')
            {
                if (invisibleLocations != 'n')
                {
                    return "(" + invisibleLocations + ")";
                }
            }
        }

        return "";
    }

    private char getVisibleOnlineStatus(String statusInfo)
    {
        char onlineLocations = statusInfo.charAt(0);
        char invisibleLocations = statusInfo.charAt(1);

        //get online locations for mods
        if (RolyDPlus.isMod())
        {
            return onlineLocations;
        }
        else //online location as seen by non-mods
        {
            if (onlineLocations == 'S' && invisibleLocations != 's' && invisibleLocations != 'b')
            {
                return 'S';
            }
            else if (onlineLocations == 'C' && invisibleLocations != 'c' && invisibleLocations != 'b')
            {
                return 'C';
            }
            if (onlineLocations == 'B')
            {
                if (invisibleLocations == 'n')
                {
                    return 'B';
                }
                else if (invisibleLocations == 's')
                {
                    return 'C';
                }
                else if (invisibleLocations == 'c')
                {
                    return 'S';
                }
            }
        }
        return 'N';
    }

    private boolean canSeeThisPlayer(String locationsInfo)
    {
        char onlineLocations = locationsInfo.charAt(0);
        char invisibleLocations = locationsInfo.charAt(1);

        if (RolyDPlus.isMod() && onlineLocations != 'N')
        {
            return true;
        }

        //determine if a this non-mod client can see the player
        if (onlineLocations == 'B' && invisibleLocations != 'b')
        {
            return true;
        }
        else if (onlineLocations == 'S' && invisibleLocations != 's' && invisibleLocations != 'b')
        {
            return true;
        }
        else if (onlineLocations == 'C' && invisibleLocations != 'c' && invisibleLocations != 'b')
        {
            return true;
        }

        return false;
    }

    /* Handles all the steps that happen when a chat line is sent. */
    private void sendChatLine()
    {
        String line = textField.getText();
        if (line.equals(""))
        {
            return;
        }

        addToMessageHistory(line);
        networkOutput(line);
        textField.setText("");
        textField.requestFocusInWindow();
    }

    /* Passes text to network output. */
    private void networkOutput(String output)
    {
        //kill any '§'
        output = output.replaceAll(COLOUR_CHAR, "");

        //add '*' if there is no '/'
        if (!output.startsWith("/"))
        {
            output = "*" + output;
        }
        NetProtocol.processOutput(output);
    }

    /* Writes a newline to the chatDoc with colour */
    public void writeColouredLine(String line)
    {
        final int lineStart = chatDoc.getLength();
        final int lastColourCharLetterInt = (int)'f';
        final int fromEnd = vScrollBar.getMaximum() - (vScrollBar.getValue() + vScrollBar.getModel().getExtent());

        //move codes with their indecies from string to colourCharMap and styleCharMap
        while (line.contains(COLOUR_CHAR))
        {
            //get index and colour of first colour code
            int codeIndex = line.indexOf(COLOUR_CHAR);
            if (codeIndex >= line.length() - 1) //if the code char is second last or last character,
            {                                   //remove the special char + code char and break
                line = line.substring(0, line.length() - 2);
                break;
            }
            char codeCharacter = line.charAt(codeIndex + 1);

            //put it in the appropriate map/s
            if (codeCharacter == 'r') //all procedures will use reset, put it in both
            {
                colourCharMap.put(codeIndex, codeCharacter);
                styleCharMap.put(codeIndex, codeCharacter);
                magicCharMap.put(codeIndex, codeCharacter);
            }
            else if (codeCharacter == 'k') //'k' belongs to magic function only
            {
                magicCharMap.put(codeIndex, codeCharacter);
            }
            else if ((int)codeCharacter > lastColourCharLetterInt) //if codeCharacter is great than 'f', it's a style
            {
                styleCharMap.put(codeIndex, codeCharacter);
                magicCharMap.put(codeIndex, codeCharacter);
            }
            else //it's a colour
            {
                colourCharMap.put(codeIndex, codeCharacter);
                magicCharMap.put(codeIndex, codeCharacter);
            }

            //remove code character
            line = line.substring(0, codeIndex) + line.substring(codeIndex + 2);
        }

        if (line.isEmpty())
        {
            return; //don't send empty lines that contain nothing but character codes
        }

        //write the line to the document
        line = line + System.lineSeparator();
        try
        {
            chatDoc.insertString(chatDoc.getLength(), line, style1);
        }
        catch (BadLocationException e)
        {
            //error
        }

        //format the added line
        applyColour(lineStart);
        applyStyles(lineStart);
        applyMagic(lineStart);

        //scroll to the end if already at the end
        if (fromEnd < 10)
        {
            textPane.setCaretPosition(chatDoc.getLength());
        }
    }

    private void cycleMessageHistory()
    {
        if (messageHistory.isEmpty())
        {
            return;
        }

        textField.setText(messageHistory.get(nextMessageHistoryIndex));
        nextMessageHistoryIndex--;
        if (nextMessageHistoryIndex < 0)
        {
            nextMessageHistoryIndex = messageHistory.size() - 1;
        }
    }

    private void addToMessageHistory(String line)
    {
        if (messageHistory.contains(line))
        {
            messageHistory.remove(line);
        }
        else
        {
            if (!messageHistory.isEmpty() && messageHistory.size() >= MESSAGE_HISTORY_LENGTH)
            {
                messageHistory.remove(0);
            }
        }

        messageHistory.add(line);

        //reset the message history index
        nextMessageHistoryIndex = messageHistory.size() - 1;
    }

    private void applyColour(int lineStart)
    {
        //loop through colourCharMap to insert colour
        int length = 0;
        int previousRelativeIndex, currentRelativeIndex = -1;
        int previousAbsoluteIndex, currentAbsoluteIndex = -1;
        char previousColourChar, currentColourChar = 'f';
        for (Map.Entry<Integer, Character> entry : colourCharMap.entrySet())
        {
            //update indecies and colours
            previousRelativeIndex = currentRelativeIndex;
            previousColourChar = currentColourChar;
            currentRelativeIndex = entry.getKey();
            currentColourChar = entry.getValue();

            if (previousRelativeIndex == -1)
            {
                continue;
            }

            //get corresponding absolute start/end indecies within the whole document, and the colourChar
            previousAbsoluteIndex = lineStart + previousRelativeIndex;
            currentAbsoluteIndex = lineStart + currentRelativeIndex;

            //get the length and colour the string
            length = currentRelativeIndex - previousRelativeIndex;
            chatDoc.setCharacterAttributes(previousAbsoluteIndex, length, codeMap.get(previousColourChar), false);
        }

        //apply last colour
        if (currentAbsoluteIndex == -1)
        {
            currentAbsoluteIndex = lineStart + currentRelativeIndex;
        }
        length = chatDoc.getLength() - currentAbsoluteIndex;
        chatDoc.setCharacterAttributes(currentAbsoluteIndex, length, codeMap.get(currentColourChar), false);

        //clear colourCharMap for next time
        colourCharMap.clear();
    }

    private  void applyStyles(int lineStart)
    {
        //loop through styleCharMap to insert style
        int currentIndex;

        //let's go through the style map
        for (Map.Entry<Integer, Character> entry : styleCharMap.entrySet())
        {
            //make a record of all styles to write before we reach a reset, or the end of line
            if (entry.getValue() != 'r' && entry.getValue() != 'R')
            {
                activeStyles.put(entry.getKey(), entry.getValue());
            }
            else //it's a reset!
            {
                //write all the active styles with current index as the end point, then clear activeStyles map
                currentIndex = entry.getKey();
                for (Map.Entry<Integer, Character> activeStyle : activeStyles.entrySet())
                {
                    //get values we need
                    int styleStartIndex = activeStyle.getKey();
                    int absoluteActiveStyleIndex = lineStart + styleStartIndex;
                    int length = currentIndex - styleStartIndex;

                    //apply the style
                    chatDoc.setCharacterAttributes(absoluteActiveStyleIndex, length,
                            codeMap.get(activeStyle.getValue()), false);
                }

                //clear active styles map
                activeStyles.clear();
            }
        }

        //end of the line! make sure we finish writing all active styles to the end of the line
        int absoluteEndOfLineIndex = chatDoc.getLength();
        for (Map.Entry<Integer, Character> activeStyle : activeStyles.entrySet())
        {
            int styleStartIndex = activeStyle.getKey();
            int absoluteActiveStyleIndex = lineStart + styleStartIndex;
            int length = absoluteEndOfLineIndex - absoluteActiveStyleIndex;

            //apply the style
            chatDoc.setCharacterAttributes(absoluteActiveStyleIndex, length,
                    codeMap.get(activeStyle.getValue()), false);
        }

        //clear maps for next time
        styleCharMap.clear();
        activeStyles.clear();
    }

    private void applyMagic(int lineStart)
    {
        if (!magicCharMap.containsValue('k')) //finish early most of the time when there is no k
        {
            return;
        }

        //loop through magicCharMap to insert colour
        int length = 0;
        int previousRelativeKIndex = -1, currentRelativeIndex = -1;
        int previousAbsoluteKIndex = -1, currentAbsoluteIndex = -1;
        char currentCodeChar = 'f';
        boolean previousWasK = false;

        for (Map.Entry<Integer, Character> entry : magicCharMap.entrySet())
        {
            currentCodeChar = entry.getValue();
            currentRelativeIndex = entry.getKey();
            if (currentCodeChar == 'k') //we've hit a k
            {
                if (previousWasK) //last was also a k, no need to change anything
                {
                    continue;
                }
                else //last was not a k, but this is. We need to set last k index to this index, then move on
                {
                    previousRelativeKIndex = currentRelativeIndex;
                    previousWasK = true;
                    continue;
                }
            }

            //this code character is not a k, apply k formatting from last k to here if we have a previous k
            if (previousWasK)
            {
                //get corresponding absolute start/end indecies within the whole document, and the colourChar
                previousAbsoluteKIndex = lineStart + previousRelativeKIndex;
                currentAbsoluteIndex = lineStart + currentRelativeIndex;

                //get the length and colour the string
                length = currentRelativeIndex - previousRelativeKIndex;
                chatDoc.setCharacterAttributes(previousAbsoluteKIndex, length, codeMap.get('k'), false);

                previousWasK = false;
            }
        }

        //apply last magic if required
        if (previousWasK)
        {
            currentAbsoluteIndex = lineStart + currentRelativeIndex;
            length = chatDoc.getLength() - currentAbsoluteIndex;
            chatDoc.setCharacterAttributes(currentAbsoluteIndex, length, codeMap.get('k'), false);
        }

        //clear magicCharMap for next time
        magicCharMap.clear();
    }

    public void enableServerInteraction()
    {
        textField.setEnabled(true);
        sendButton.setEnabled(true);
        reconnectButton.setVisible(false);
    }

    public void disableServerInteraction()
    {
        textField.setEnabled(false);
        sendButton.setEnabled(false);
        reconnectButton.setVisible(true);
    }

    public void showFrame()
    {
        FramesManager.getFrameChat().setVisible(true);
        setFocusToTextField();
    }

    private void setFocusToTextField()
    {
        textField.requestFocusInWindow();
    }

    private void toggleAlwaysOnTop()
    {
        if (isAlwaysOnTop())
        {
            setAlwaysOnTop(false);
        }
        else
        {
            setAlwaysOnTop(true);
        }
    }

    private void clearChat()
    {
        try
        {
            chatDoc.remove(0, chatDoc.getLength());
        }
        catch (BadLocationException e)
        {
            //shouldn't really be a bad location... using location given by the chatDoc itself.
        }
    }

    private void logout()
    {
        clearChat();
        RolyDPlus.logout();
    }

    /* WORKAROUND FOR JAVA 7 WORD WRAP BUG */
    class WrapEditorKit extends StyledEditorKit
    {
        ViewFactory defaultFactory=new WrapColumnFactory();
        public ViewFactory getViewFactory()
        {
            return defaultFactory;
        }
    }

    class WrapColumnFactory implements ViewFactory
    {
        public View create(Element elem)
        {
            String kind = elem.getName();
            if (kind != null)
            {
                switch (kind)
                {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);

                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);

                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);

                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);

                    case StyleConstants.IconElementName:
                        return new IconView(elem);

                    default:
                        return new LabelView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    class WrapLabelView extends LabelView
    {
        public WrapLabelView(Element elem)
        {
            super(elem);
        }

        public float getMinimumSpan(int axis)
        {
            switch (axis)
            {
                case View.X_AXIS:
                    return 0;

                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);

                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }

    private void test()
    {
        for (String s : messageHistory)
        {
            System.out.println(s);
        }
    }

}
