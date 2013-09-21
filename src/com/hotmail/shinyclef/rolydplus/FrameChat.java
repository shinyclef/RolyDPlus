package com.hotmail.shinyclef.rolydplus;

import sun.font.FontFamily;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * User: Shinyclef
 * Date: 11/08/13
 * Time: 7:22 PM
 */

public class FrameChat extends JFrame
{
    private static final boolean TEST_BUTTON_VISIBLE = false;

    private static final String COLOUR_CHAR = String.valueOf('\u00A7');
    private static Map<Character, SimpleAttributeSet> codeMap;
    private static Map<Integer, Character> colourMap;
    private static Map<Integer, Character> styleMap;
    private static Map<Integer, Character> activeStyles;

    private static final int MAX_LINES = 3;
    private static final Color CHAT_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .1f);
    private static final Color LIST_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .85f);

    private int currentLines = 0;

    private Container contentPane;
    private JPanel rightBox;
    private JTextPane textPane;
    private JScrollBar vScrollBar;
    private StyledDocument chatDoc;
    private JScrollPane listScrollPane;

    private JTextField textField;
    private static JButton sendButton;

    private Map<String, JLabel> onlinePlayers;

    private SimpleAttributeSet style1;

    public FrameChat()
    {
        onlinePlayers = new TreeMap<>();
        colourMap = new LinkedHashMap<>();
        styleMap = new LinkedHashMap<>();
        activeStyles = new LinkedHashMap<>();
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
        JPanel topFlow = new JPanel();
        rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setBorder(new EmptyBorder(new Insets(5, 5, 5, 18)));
        JPanel bottomGridBag = new JPanel(new GridBagLayout());
        GridBagConstraints c;
        contentPane.add(topFlow, BorderLayout.NORTH);
        contentPane.add(bottomGridBag, BorderLayout.SOUTH);

        //create the components
        textPane = new JTextPane();
        JScrollPane chatScrollPane = new JScrollPane(textPane);
        listScrollPane = new JScrollPane(rightBox);
        vScrollBar = chatScrollPane.getVerticalScrollBar();
        textField = new JTextField();
        chatDoc = textPane.getStyledDocument();
        JButton testButton = new JButton("Test");
        sendButton = new JButton("Send");

        //add the components to the panels
        contentPane.add(chatScrollPane, BorderLayout.CENTER);
        contentPane.add(listScrollPane, BorderLayout.EAST);
        topFlow.add(testButton);

        //set some properties
        textField.setPreferredSize(new Dimension(0, 25));
        textPane.setBackground(CHAT_BACKGROUND_COLOR);
        rightBox.setBackground(LIST_BACKGROUND_COLOR);
        sendButton.setFont(new Font("Arial", 1, 10));
        sendButton.setPreferredSize(new Dimension(60, 24));
        textPane.setEditable(false);
        //listScrollPane.setPreferredSize(new Dimension(120, 0));
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

        //the listener
        ActionListener listener = new InputListener();

        //add the listener to the buttons
        textField.addActionListener(listener);
        sendButton.addActionListener(listener);
        testButton.addActionListener(listener);


        testButton.setVisible(TEST_BUTTON_VISIBLE);
    }

    class InputListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("Test"))
            {
                test();
            }
            else
            {
                sendChatLine();
            }
        }
    }

    public void addOrAlterPlayer(String playerName, String currentPresence)
    {
        //get suffix
        String suffix = "";

        if (currentPresence.equals("Client"))
        {
            suffix = "(-)";
        }
        else if (currentPresence.equals("Both"))
        {
            suffix = "(+)";
        }
        //(if "Server", do nothing)

        //add player to online list and redraw
        onlinePlayers.put(playerName, new JLabel(playerName + suffix));
        redrawList();
    }

    public void removePlayer(String playerName)
    {
        //remove player from online list and redraw
        onlinePlayers.remove(playerName);
        redrawList();
    }

    //sorting can occur here
    private void redrawList()
    {
        rightBox.removeAll();
        for (JLabel label : onlinePlayers.values())
        {
            rightBox.add(label);
        }

        for (JLabel label : onlinePlayers.values())
        {
            label.revalidate();
        }

        //listScrollPane.setPreferredSize(new Dimension(rightBox.getWidth()+ 12, 0));
        rightBox.repaint();
    }

    public void processFormattedOnlinePlayersList(String formattedListString)
    {
        onlinePlayers.clear();
        String[] players = formattedListString.split(",");

        for (String player : players)
        {
            //the name will be prefixed with '-' (client only), '+' (both), or nothing (server only) attached

            String suffix;
            if (player.startsWith("-"))
            {
                player = player.substring(1);
                suffix = "(-)";
            }
            else if (player.startsWith("+"))
            {
                player = player.substring(1);
                suffix = "(+)";
            }
            else
            {
                suffix = "";
            }

            onlinePlayers.put(player, new JLabel(player + suffix));
        }

        redrawList();
    }

    //handles the online list, not the chat notification
    public void processOnlineChangeEvent(String playerName, String currentPresence)
    {
        //user has logged out of everything
        if (currentPresence.equals("None"))
        {
            removePlayer(playerName);
        }
        else
        {
            addOrAlterPlayer(playerName, currentPresence);
        }
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
        StyleConstants.setForeground(s0, Color.getHSBColor(0, 0, 0));           //black
        StyleConstants.setForeground(s1, Color.getHSBColor(.666666f, 1, .67f)); //dark-blue
        StyleConstants.setForeground(s2, Color.getHSBColor(.333333f, 1, .67f)); //dark-green
        StyleConstants.setForeground(s3, Color.getHSBColor(.5f, 1, .67f));      //dark-aqua
        StyleConstants.setForeground(s4, Color.getHSBColor(0, 1, .67f));        //dark-red
        StyleConstants.setForeground(s5, Color.getHSBColor(.833333f, 1, .67f)); //dark-purple
        StyleConstants.setForeground(s6, Color.getHSBColor(.111111f, 1, 1));    //gold
        StyleConstants.setForeground(s7, Color.getHSBColor(0, 0, .67f));        //gray
        StyleConstants.setForeground(s8, Color.getHSBColor(.236111f, 0, .33f)); //dark-gray
        StyleConstants.setForeground(s9, Color.getHSBColor(.666666f, .67f, 1)); //blue
        StyleConstants.setForeground(sa, Color.getHSBColor(.333333f, .67f, 1)); //green
        StyleConstants.setForeground(sb, Color.getHSBColor(.5f, .67f, 1));      //aqua
        StyleConstants.setForeground(sc, Color.getHSBColor(0, .67f, 1));        //red
        StyleConstants.setForeground(sd, Color.getHSBColor(.833333f, .67f, 1)); //light-purple
        StyleConstants.setForeground(se, Color.getHSBColor(.166666f, .67f, 1)); //yellow
        StyleConstants.setForeground(sf, Color.getHSBColor(0, 0, 1));           //white
        StyleConstants.setForeground(sk, Color.DARK_GRAY);//magic
        StyleConstants.setBackground(sk, Color.DARK_GRAY);//magic
        StyleConstants.setBold(sl, true);           //bold
        StyleConstants.setStrikeThrough(sm, true);  //strike-through
        StyleConstants.setUnderline(sn, true);      //underline
        StyleConstants.setItalic(so, true);         //italic
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

    /* Handles all the steps that happen when a chat line is sent. */
    private void sendChatLine()
    {
        String line = textField.getText();
        if (line.equals(""))
        {
            return;
        }

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

        //move codes with their indecies from string to colourMap and styleMap
        while (line.contains(COLOUR_CHAR))
        {
            //get index and colour of first colour code
            int codeIndex = line.indexOf(COLOUR_CHAR);
            if (codeIndex >= line.length() - 1)
            {
                line = line.substring(0, line.length() - 2);
                break;
            }
            char codeCharacter = line.charAt(codeIndex + 1);

            //put it in the appropriate map/s
            if (codeCharacter == 'r') //colour and style procedures will use reset, put it in both
            {
                colourMap.put(codeIndex, codeCharacter);
                styleMap.put(codeIndex, codeCharacter);
            }
            else if ((int)codeCharacter > lastColourCharLetterInt) //if codeCharacter is great than 'f'
            {
                styleMap.put(codeIndex, codeCharacter);
            }
            else //it's a colour
            {
                colourMap.put(codeIndex, codeCharacter);
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

        //scroll to the end if already at the end
        if (fromEnd < 5)
        {
            textPane.setCaretPosition(chatDoc.getLength());
        }

    }

    private void applyColour(int lineStart)
    {
        //loop through colourMap to insert colour
        int length = 0;
        int previousRelativeIndex, currentRelativeIndex = -1;
        int previousAbsoluteIndex, currentAbsoluteIndex = -1;
        char previousColourChar, currentColourChar = '-';
        for (Map.Entry<Integer, Character> entry : colourMap.entrySet())
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

        //clear colourMap for next time
        colourMap.clear();
    }

    private  void applyStyles(int lineStart)
    {
        //loop through styleMap to insert style
        int currentIndex;

        //let's go through the style map
        for (Map.Entry<Integer, Character> entry : styleMap.entrySet())
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
        styleMap.clear();
        activeStyles.clear();
    }

    public void enableControls()
    {
        textField.setEnabled(true);
        sendButton.setEnabled(true);
    }

    public void disableControls()
    {
        textField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    private void test()
    {

    }
}
