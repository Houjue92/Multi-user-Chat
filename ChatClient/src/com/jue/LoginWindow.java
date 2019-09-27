package com.jue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField loginInputField = new JTextField();
    JPasswordField passwordInputField = new JPasswordField();
    JButton loginClickButton = new JButton("Login");

    public static void main(String[] args) {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);
    }

    public LoginWindow() {
        super("Login");
        this.client = new ChatClient("localhost", 8818);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginInputField);
        p.add(passwordInputField);
        p.add(loginClickButton);

        loginClickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);
        // just pack the components fit well in LoginWindows
        pack();
        setVisible(true);
    }

    private void doLogin() {
        String login = loginInputField.getText();
        String password = passwordInputField.getText();

        try {
            if (client.login(login, password)) {
                //open the user list window
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("UserList");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //set size for login window
                frame.setSize(300,500);
                frame.getContentPane().add(userListPane,BorderLayout.CENTER);
                frame.setVisible(true);
                setVisible(false);
            }
            else{
                //display error if the login or password not match
                JOptionPane.showMessageDialog(this, "Invalid login or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
