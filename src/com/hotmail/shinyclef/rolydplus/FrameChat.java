package com.hotmail.shinyclef.rolydplus;

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
    private static Map<Character, SimpleAttributeSet> colourMap;

    private static final int MAX_LINES = 3;
    private static final Color CHAT_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .1f);
    private static final Color LIST_BACKGROUND_COLOR = Color.getHSBColor(0, 0, .85f);

    private int currentLines = 0;
    private java.util.List<Integer> startPosList;
    private java.util.List<Integer> endPosList;
    private java.util.List<Character> colourCharList;

    private Container contentPane;
    private JPanel rightBox;
    private JTextPane textPane;
    private JScrollBar vScrollBar;
    private StyledDocument chatDoc;
    private JScrollPane listScrollPane;

    private JTextField textField;
    private JButton sendButton;

    private Map<String, JLabel> onlinePlayers;

    private SimpleAttributeSet style1;

    public FrameChat()
    {
        startPosList = new LinkedList<Integer>();
        endPosList = new LinkedList<Integer>();
        colourCharList = new LinkedList<Character>();
        onlinePlayers = new TreeMap<String, JLabel>();
        populateColourMap();
        initializeUI();
    }

    public void initializeUI()
    {
        //basic frame attributes
        setTitle("RolyDPlus Chat");
        setSize(550, 600);
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

    private static void populateColourMap()
    {
        //create a bunch of attribute sets
        SimpleAttributeSet s0 = new SimpleAttributeSet();
        SimpleAttributeSet s1 = new SimpleAttributeSet();
        SimpleAttributeSet s2 = new SimpleAttributeSet();
        SimpleAttributeSet s3 = new SimpleAttributeSet();
        SimpleAttributeSet s4 = new SimpleAttributeSet();
        SimpleAttributeSet s5 = new SimpleAttributeSet();
        SimpleAttributeSet s6 = new SimpleAttributeSet();
        SimpleAttributeSet s7 = new SimpleAttributeSet();
        SimpleAttributeSet s8 = new SimpleAttributeSet();
        SimpleAttributeSet s9 = new SimpleAttributeSet();
        SimpleAttributeSet sa = new SimpleAttributeSet();
        SimpleAttributeSet sb = new SimpleAttributeSet();
        SimpleAttributeSet sc = new SimpleAttributeSet();
        SimpleAttributeSet sd = new SimpleAttributeSet();
        SimpleAttributeSet se = new SimpleAttributeSet();
        SimpleAttributeSet sf = new SimpleAttributeSet();

        //set mc colours to the attributes sets
        StyleConstants.setForeground(s0, Color.getHSBColor(0, 0, 0));
        StyleConstants.setForeground(s1, Color.getHSBColor(.666666f, 1, .67f));
        StyleConstants.setForeground(s2, Color.getHSBColor(.333333f, 1, .67f));
        StyleConstants.setForeground(s3, Color.getHSBColor(.5f, 1, .67f));
        StyleConstants.setForeground(s4, Color.getHSBColor(0, 1, .67f));
        StyleConstants.setForeground(s5, Color.getHSBColor(.833333f, 1, .67f));
        StyleConstants.setForeground(s6, Color.getHSBColor(.111111f, 1, 1));
        StyleConstants.setForeground(s7, Color.getHSBColor(0, 0, .67f));
        StyleConstants.setForeground(s8, Color.getHSBColor(.236111f, 0, .33f));
        StyleConstants.setForeground(s9, Color.getHSBColor(.666666f, .67f, 1));
        StyleConstants.setForeground(sa, Color.getHSBColor(.333333f, .67f, 1));
        StyleConstants.setForeground(sb, Color.getHSBColor(.5f, .67f, 1));
        StyleConstants.setForeground(sc, Color.getHSBColor(0, .67f, 1));
        StyleConstants.setForeground(sd, Color.getHSBColor(.833333f, .67f, 1));
        StyleConstants.setForeground(se, Color.getHSBColor(.166666f, .67f, 1));
        StyleConstants.setForeground(sf, Color.getHSBColor(0, 0, 1));

        //put the attribute sets into the colour map
        colourMap = new HashMap<Character, SimpleAttributeSet>();
        colourMap.put('0', s0);
        colourMap.put('1', s1);
        colourMap.put('2', s2);
        colourMap.put('3', s3);
        colourMap.put('4', s4);
        colourMap.put('5', s5);
        colourMap.put('6', s6);
        colourMap.put('7', s7);
        colourMap.put('8', s8);
        colourMap.put('9', s9);
        colourMap.put('a', sa);
        colourMap.put('b', sb);
        colourMap.put('c', sc);
        colourMap.put('d', sd);
        colourMap.put('e', se);
        colourMap.put('f', sf);
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
        //get new starting point and user is scrolled to bottom
        int lineStart = chatDoc.getLength();
        int fromEnd = vScrollBar.getMaximum() - (vScrollBar.getValue() + vScrollBar.getModel().getExtent());

        //parse string and build colourMap
        while (line.contains(COLOUR_CHAR))
        {
            //get index and colour of first colour code
            int firstIndex = line.indexOf(COLOUR_CHAR);
            if (firstIndex >= line.length() - 1)
            {
                break;
            }
            char colour = line.charAt(firstIndex + 1);

            //remove first colour code and search for the next colour code
            line = line.substring(0, firstIndex) + line.substring(firstIndex + 2);
            int secondIndex = line.indexOf(COLOUR_CHAR);

            //add information to lists
            startPosList.add(firstIndex);
            endPosList.add(secondIndex);
            colourCharList.add(colour);
        }

        if (line.isEmpty())
        {
            return;
        }
        line = line + System.lineSeparator();
        try
        {
            chatDoc.insertString(chatDoc.getLength(), line, style1);
        }
        catch (BadLocationException e)
        {
            //error
        }

        //colour the line
        for (int i = 0; i < startPosList.size(); i++)
        {
            //get corresponding end position and colour
            int rawStart = startPosList.get(i) ;
            int rawEnd = endPosList.get(i);
            if (rawEnd == -1)
            {
                rawEnd = line.length();
            }

            int adjustedStart = lineStart + rawStart;
            int adjustedEnd = lineStart + rawEnd;
            char colourChar = colourCharList.get(i);

            //get the length and colour the string
            int length = adjustedEnd - adjustedStart;
            chatDoc.setCharacterAttributes(adjustedStart, length, colourMap.get(colourChar), false);
        }

        //scroll to the end if already at the end
        if (fromEnd < 5)
        {
            textPane.setCaretPosition(chatDoc.getLength());
        }

        //clear lists for next time
        startPosList.clear();
        endPosList.clear();
        colourCharList.clear();
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
