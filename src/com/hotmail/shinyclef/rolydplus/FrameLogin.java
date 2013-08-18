package com.hotmail.shinyclef.rolydplus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Author: Shinyclef
 * Date: 21/09/12
 * Time: 1:46 AM
 * Description: The login frame. Taken from a former project.
 */

public class FrameLogin extends JFrame
{
    private static final int ATTEMPTS = 5;
    private int attemptsRemaining;
    private boolean firstAttempt = true;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel feedbackLabel;
    private JButton loginButton;

    /* Constructor. */
    public FrameLogin()
    {
        attemptsRemaining = ATTEMPTS;
        initializeUI();
    }

    /* The method responsible for initializing the frame with all of its components and settings. */
    public final void initializeUI()
    {
        //the listener
        ActionListener listener = new ButtonListener();

        //basic frame attributes
        setTitle("RolyDPlus Login");
        setSize(350, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                RolyDPlus.initializeExit(0);
                //setVisible(false);
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

        //add the components to the panels
        boxPanel.add(titleLabel);
        boxPanel.add(Box.createVerticalStrut(20));
        boxPanel.add(usernamePanel);
        boxPanel.add(passwordPanel);

        //boxPanel.add(Box.createVerticalStrut(0));
        boxPanel.add(feedbackLabel);
        boxPanel.add(Box.createVerticalStrut(15));

        boxPanel.add(buttonsPanel);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(quitButton);

        //add the listener to the buttons
        loginButton.addActionListener(listener);
        quitButton.addActionListener(listener);
    }

    /* The action listen for the buttons. */
    class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent ev)
        {
            if (ev.getActionCommand().equals("Login"))
            {
                String usernameInput = usernameField.getText();
                char[] passwordInput = passwordField.getPassword();

                //check for null/empty
                if (usernameInput.equals("") || (passwordInput.length == 0))
                {
                    incompleteFieldsFeedback();
                    return;
                }

                //send login request then wait
                NetProtocolHelper.attemptLogin(usernameInput, new String(passwordInput));
                attemptingLoginFeedback();
            }
            else
            {
                RolyDPlus.initializeExit(0);
            }
        }
    }

    public void loginReply(boolean successfulLogin)
    {
        if (successfulLogin)
        {
            RolyDPlus.login();
        }
        else
        {
            if (attemptsRemaining > 1)
            {
                if (firstAttempt)
                {
                    feedbackLabel.setVisible(true);
                    firstAttempt = false;
                }

                attemptsRemaining = attemptsRemaining - 1;
                unsuccessfulAttemptFeedback();
            }
            else
            {
                //Trigger Lockout!!
                RolyDPlus.initializeExit(0);
            }
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

    public void enableControls()
    {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        feedbackLabel.setForeground(Color.BLUE);
        feedbackLabel.setText("Server has restart. You can now login.");
    }

    public void disableControls()
    {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
        feedbackLabel.setForeground(Color.BLUE);
        feedbackLabel.setText("Server has shut down. Waiting for restart...");
        feedbackLabel.setVisible(true);

    }
}