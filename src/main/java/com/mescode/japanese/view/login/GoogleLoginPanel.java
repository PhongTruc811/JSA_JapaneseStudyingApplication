package com.mescode.japanese.view.login;

import com.mescode.japanese.auth.oauth.GoogleOAuthConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class GoogleLoginPanel extends JPanel {
    private final Color GOOGLE_BLUE = new Color(0x4285F4);
    private final Color GOOGLE_HOVER = new Color(0x2F6FDB);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public GoogleLoginPanel() {
        setupUI();
    }

    private void setupUI() {
        setBackground(new Color(0xF4F6F8));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 20, 40, 20));

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(0xF4F6F8));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sign in with Google");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0x202124));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(30));

        JButton googleButton = createGoogleButton();
        centerPanel.add(googleButton);
        centerPanel.add(Box.createVerticalStrut(20));

        JLabel infoLabel = new JLabel("Click the button above to sign in using your Google account");
        infoLabel.setFont(INFO_FONT);
        infoLabel.setForeground(new Color(0x666666));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createGoogleButton() {
        JButton button = new JButton("Login with Google") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getModel().isArmed() ? GOOGLE_HOVER : GOOGLE_BLUE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                g2d.setColor(Color.WHITE);
                g2d.setFont(BUTTON_FONT);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> openGoogleLogin());
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    private void openGoogleLogin() {
        String clientId = GoogleOAuthConfig.getClientId();
        String clientSecret = GoogleOAuthConfig.getClientSecret();
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Google OAuth chưa được cấu hình.\nHãy cập nhật google.client.id và google.client.secret trong oauth-config.properties.",
                    "Missing Google OAuth Config",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            String authUrl = GoogleOAuthConfig.getAuthorizationUrl();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
                JOptionPane.showMessageDialog(
                        this,
                        "Browser đã được mở. Hãy cho phép app và copy authorization code.",
                        "Google Login",
                        JOptionPane.INFORMATION_MESSAGE
                );

                String authCode = JOptionPane.showInputDialog(
                        this,
                        "Enter the authorization code from Google:",
                        "Authorization Code"
                );

                if (authCode != null && !authCode.isEmpty()) {
                    handleGoogleCallback(authCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGoogleCallback(String authCode) {
        JOptionPane.showMessageDialog(
                this,
                "Google login nhận được code: " + authCode + "\n\n"
                        + "Bước tiếp theo: exchange code -> access token -> lấy profile.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
