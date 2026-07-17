package com.mescode.japanese.view.login;

import javax.swing.*;
import java.awt.*;

public class OTP_LoginPanel extends JPanel {
    JTextField txtPhone = new JTextField(15);
    JTextField txtOTP = new JTextField(6);

    JButton btnSendOTP = new JButton("Send OTP");
    JButton btnVerify = new JButton("Verify");

    public OTP_LoginPanel() {
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Phone"));
        add(txtPhone);

        add(new JLabel());
        add(btnSendOTP);

        add(new JLabel("OTP"));
        add(txtOTP);

        add(new JLabel());
        add(btnVerify);
    }
}
