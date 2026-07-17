package com.mescode.japanese.view.login;

import javax.swing.*;
import java.awt.*;

public class Password_LoginPanel extends JPanel {
    JLabel usernameLabel = new JLabel("Username:");
    JTextField usernameInput = new JTextField(15);
    JLabel passwordLabel = new JLabel("Password:");
    JPasswordField passwordInput = new JPasswordField(15);
    JButton btnLogin = new JButton("Login");

    public Password_LoginPanel() {
        setLayout(new GridLayout(3, 2, 10, 10));

        add(usernameLabel);
        add(usernameInput);

        add(passwordLabel);
        add(passwordInput);

        add(new JLabel());
        add(btnLogin);
    }
}
