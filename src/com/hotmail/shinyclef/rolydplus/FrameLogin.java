package com.hotmail.shinyclef.rolydplus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Author: Shinyclef
 * Date: 21/09/12
 * Time: 1:46 AM
 * Description: The login frame. Taken from a former project.
 */

public class FrameLogin extends JFrame
{
    private int attemptsRemaining;
    private boolean firstAttempt = true;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel feedbackLabel;
    private JButton loginButton;
    private JButton reconnectButton;

    /* Constructor. */
    public FrameLogin()
    {
        initializeUI();
    }

    /* The method responsible for initializing the frame with all of its components and settings. */
    public final void initializeUI()
    {
        //basic frame attributes
        setTitle("RolyDPlus Login");
        setSize(350, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                RolyDPlus.initializeExit(0);
            }
        });

        //create the box panel and add it content pane
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBorder(new EmptyBorder(new Insets(20, 20, 30, 20)));
        getContentPane().add(boxPanel);

        //create some default flow panels to use for the other components
        JPanel usernamePanel = new JPanel();
        JPanel passwordPanel = new JPanel();
        JPanel buttonsPanel = new JPanel();

        //create the components
        JLabel titleLabel = new JLabel("<html><center>Welcome to RolyDPlus." +
                "<br>Please enter your username and password.</center></html>", SwingConstants.CENTER);
        JLabel usernameLabel = new JLabel("Username:", SwingConstants.RIGHT);
        JLabel passwordLabel = new JLabel("Password:", SwingConstants.RIGHT);
        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        usernameField = new JTextField(14);
        passwordField = new JPasswordField(14);
        loginButton = new JButton("Login");
        JButton quitButton = new JButton("Quit");
        reconnectButton = new JButton("Reconnect");

        //component formatting
        Dimension labelDimension = new Dimension(65, 30);
        Dimension buttonDimension = new Dimension(80, 30);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        feedbackLabel.setAlignmentX(CENTER_ALIGNMENT);
        feedbackLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        feedbackLabel.setVisible(false);
        usernameLabel.setPreferredSize(labelDimension);
        passwordLabel.setPreferredSize(labelDimension);
        loginButton.setPreferredSize(buttonDimension);
        quitButton.setPreferredSize(buttonDimension);
        reconnectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        reconnectButton.setVisible(false);

        //add the components to the panels
        boxPanel.add(titleLabel);
        boxPanel.add(Box.createVerticalStrut(20));
        boxPanel.add(usernamePanel);
        boxPanel.add(passwordPanel);

        //boxPanel.add(Box.createVerticalStrut(0));
        boxPanel.add(feedbackLabel);
        boxPanel.add(Box.createVerticalStrut(15));
        boxPanel.add(buttonsPanel);
        boxPanel.add(Box.createVerticalStrut(5));
        boxPanel.add(reconnectButton);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(quitButton);

        //the listeners
        MyActionListener myActionListener = new MyActionListener();
        MyKeyListener myKeyListener = new MyKeyListener();

        //add the listeners to the components
        loginButton.addActionListener(myActionListener);
        quitButton.addActionListener(myActionListener);
        reconnectButton.addActionListener(myActionListener);
        usernameField.addKeyListener(myKeyListener);
        passwordField.addKeyListener(myKeyListener);
    }

    /* The action listen for the buttons. */
    class MyActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            String buttonName = ev.getActionCommand();
            if (buttonName.equals("Login"))
            {
                login();
            }
            else if (buttonName.equals("Quit"))
            {
                RolyDPlus.initializeExit(0);
            }
            else if(buttonName.equals("Reconnect"))
            {
                reconnect();
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
            if (e.getKeyCode() == 10) //10 is 'enter' key
            {
                login();
            }
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
        }
    }

    private void login()
    {
        String usernameInput = usernameField.getText();
        char[] passwordInput = passwordField.getPassword();

        //check for null/empty
        if (usernameInput.equals("") || (passwordInput.length == 0))
        {
            incompleteFieldsFeedback();
            return;
        }

        //set account details assuming success
        RolyDPlus.setUsername(usernameInput);
        RolyDPlus.setPassword(new String(passwordInput));

        //send login request then wait
        NetProtocolHelper.attemptLogin(usernameInput, new String(passwordInput));
        attemptingLoginFeedback();
    }

    private void reconnect()
    {
        attemptingReconnectFeedback();
        RolyDPlus.reconnect(1);
    }

    public void unsuccessfulLoginReply(String reason)
    {
        if (reason.equalsIgnoreCase(NetProtocolHelper.REASON_OUT_OF_DATE))
        {
            clientOutOfDateFeedback();
            return;
        }

        if (attemptsRemaining > 1)
        {
            if (firstAttempt)
            {
                feedbackLabel.setVisible(true);
                firstAttempt = false;
            }

            attemptsRemaining = attemptsRemaining - 1;

            switch (reason)
            {
                case NetProtocolHelper.REASON_NO_USER:
                case NetProtocolHelper.REASON_BAD_PASSWORD:
                    unsuccessfulAttemptFeedback();
                    break;

                default:
                    if (RolyDPlus.DEV_BUILD)
                    {
                        System.out.println("WARNING: Default case triggered in FrameLogin.unsuccessfulLoginReply.");
                    }
                    break;
            }
        }
        else
        {
            //Trigger Lockout!!
            RolyDPlus.initializeExit(0);
        }
    }

    private void attemptingLoginFeedback()
    {
        feedbackLabel.setForeground(Color.GREEN);
        feedbackLabel.setText("<html><center>Logging in...</center></html>");
    }

    private void unsuccessfulAttemptFeedback()
    {
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setText("<html><center>Incorrect username/password. " + attemptsRemaining +
                (attemptsRemaining == 1 ? " attempt" : " attempts") +
                " remaining before 30 minute lockout.</center></html>");
    }

    private void incompleteFieldsFeedback()
    {
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setText("<html><center>You must enter a valid username and password.</center></html>");
        feedbackLabel.setVisible(true);
    }

    public void updateAvailableFeedback()
    {
        feedbackLabel.setForeground(Color.BLUE);
        feedbackLabel.setText("<html><center>A new version of RolyDPlus is available for download at " +
                "www.rolyd.com/roldyplus.</center></html>");
        feedbackLabel.setVisible(true);
    }

    private void clientOutOfDateFeedback()
    {
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setText("<html><center>This version of RolyDPlus is out of date. " +
                "Please download the latest version from: www.rolyd.com/rolydplus</center></html>");
        feedbackLabel.setVisible(true);
    }

    private void attemptingReconnectFeedback()
    {
        feedbackLabel.setForeground(Color.YELLOW);
        feedbackLabel.setText("<html><center>Attempting to reconnect to server...</center></html>");
        feedbackLabel.setVisible(true);
    }

    public void reconnectSuccessfulFeedback()
    {
        feedbackLabel.setForeground(Color.GREEN);
        feedbackLabel.setText("<html><center>Successfully reconnected to server.</center></html>");
        feedbackLabel.setVisible(true);
    }

    public void reconnectUnsuccessfulFeedback()
    {
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setText("<html><center>Could not connect to server.</center></html>");
        feedbackLabel.setVisible(true);
    }

    public void enableControls()
    {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        reconnectButton.setVisible(false);
        feedbackLabel.setForeground(Color.BLUE);
        feedbackLabel.setText("Connection established. You can now log in.");
    }

    public void disableControls()
    {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
        reconnectButton.setVisible(true);
        feedbackLabel.setForeground(Color.BLUE);
        feedbackLabel.setText("Lost connection with server.");
        feedbackLabel.setVisible(true);

    }

    public void reset()
    {
        usernameField.setText("");
        passwordField.setText("");
        feedbackLabel.setVisible(false);
    }
}