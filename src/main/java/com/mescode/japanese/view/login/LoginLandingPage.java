package com.mescode.japanese.view.login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginLandingPage extends JPanel {
    private final Color SKY_BLUE = new Color(0xE0F6FF);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font BUTTON_FONT = new Font("Segoe UI Emoji", Font.BOLD, 16);

    private JButton usernameButton;
    private JButton githubButton;
    private JButton googleButton;

    public LoginLandingPage() {
        setupUI();
    }

    private void setupUI() {
        setBackground(SKY_BLUE);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(SKY_BLUE);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Japanese Alphabet Quiz");
        title.setFont(TITLE_FONT);
        title.setForeground(new Color(0x1F2D3D));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(10));

        // Subtitle
        JLabel subtitle = new JLabel("Choose your login method");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0x555555));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitle);
        centerPanel.add(Box.createVerticalStrut(60));

        // Username/Password Button
        usernameButton = createOptionButton(
                "📧 Username & Password",
                "Traditional login with email and password",
                new Color(0x007AFF)
        );
        centerPanel.add(usernameButton);
        centerPanel.add(Box.createVerticalStrut(20));

        // GitHub Button
        githubButton = createOptionButton(
                "🐙 Login with GitHub",
                "Quick login using your GitHub account",
                new Color(0x24292E)
        );
        centerPanel.add(githubButton);
        centerPanel.add(Box.createVerticalStrut(20));

        googleButton = createOptionButton(
                "🔵 Login with Google",
                "Quick login using your Google account",
                new Color(0x4285F4)
        );
        centerPanel.add(googleButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createOptionButton(String title, String description, Color bgColor) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                Color finalColor = getModel().isArmed() ? 
                        darker(bgColor, 0.8f) : bgColor;
                g2d.setColor(finalColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border
                g2d.setColor(lighter(bgColor, 1.2f));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }

            private Color darker(Color c, float factor) {
                return new Color(
                        (int)(c.getRed() * factor),
                        (int)(c.getGreen() * factor),
                        (int)(c.getBlue() * factor)
                );
            }

            private Color lighter(Color c, float factor) {
                return new Color(
                        Math.min((int)(c.getRed() * factor), 255),
                        Math.min((int)(c.getGreen() * factor), 255),
                        Math.min((int)(c.getBlue() * factor), 255)
                );
            }
        };

        button.setLayout(new BorderLayout());
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(400, 80));
        button.setMaximumSize(new Dimension(400, 80));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setBackground(new Color(0, 0, 0, 0));
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BUTTON_FONT);
        titleLabel.setForeground(Color.WHITE);
        textPanel.add(titleLabel);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(255, 255, 255, 200));
        textPanel.add(descLabel);

        button.add(textPanel, BorderLayout.CENTER);

        return button;
    }

    public JButton getUsernameButton() {
        return usernameButton;
    }

    public JButton getGitHubButton() {
        return githubButton;
    }

    public JButton getGoogleButton() {
        return googleButton;
    }
}
