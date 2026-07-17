package com.mescode.japanese.view.login;

import com.mescode.japanese.auth.oauth.GitHubOAuthConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Desktop;
import java.net.URI;

public class GitHubLoginPanel extends JPanel {
    private final Color GITHUB_BLACK = new Color(0x24292E);
    private final Color GITHUB_HOVER = new Color(0x1F1F1F);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font INFO_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public GitHubLoginPanel() {
        setupUI();
    }

    private void setupUI() {
        setBackground(new Color(0xF4F6F8));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 20, 40, 20));

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(0xF4F6F8));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Logo/Title
        JLabel title = new JLabel("Sign in with GitHub");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(GITHUB_BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(30));

        // GitHub Button
        JButton githubButton = createGitHubButton();
        centerPanel.add(githubButton);
        centerPanel.add(Box.createVerticalStrut(20));

        // Info text
        JLabel infoLabel = new JLabel("Click the button above to sign in using your GitHub account");
        infoLabel.setFont(INFO_FONT);
        infoLabel.setForeground(new Color(0x666666));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(infoLabel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createGitHubButton() {
        JButton button = new JButton("Login with GitHub") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2d.setColor(getModel().isArmed() ? GITHUB_HOVER : GITHUB_BLACK);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // Text
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

        button.addActionListener(e -> openGitHubLogin());

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

    private void openGitHubLogin() {
        try {
            String authUrl = GitHubOAuthConfig.getAuthorizationUrl();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
                JOptionPane.showMessageDialog(
                        this,
                        "Your browser has opened. Please authorize the application and copy the authorization code.",
                        "GitHub Login",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // TODO: Implement callback handler to receive auth code
                String authCode = JOptionPane.showInputDialog(
                        this,
                        "Enter the authorization code from GitHub:",
                        "Authorization Code"
                );

                if (authCode != null && !authCode.isEmpty()) {
                    handleGitHubCallback(authCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGitHubCallback(String authCode) {
        JOptionPane.showMessageDialog(
                this,
                "Login successful! Auth Code: " + authCode + "\n\n" +
                "In production, this would exchange for an access token.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
        // TODO: Exchange authCode for access token and get user info
    }
}
