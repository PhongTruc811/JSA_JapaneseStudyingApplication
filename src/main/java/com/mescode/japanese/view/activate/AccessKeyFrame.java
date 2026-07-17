package com.mescode.japanese.view.activate;

import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AccessKeyFrame extends JFrame {
    private static final String DEFAULT_ACCESS_KEY = "8112005";

    private final MenuNavigator navigator;
    private final boolean darkMode;

    private JTextField keyField;
    private JLabel statusLabel;

    public AccessKeyFrame(MenuNavigator navigator) {
        this.navigator = navigator;
        this.darkMode = navigator.getAppContext().isDarkMode();

        setupFrame();
        setupUI();
    }

    private void setupFrame() {
        setTitle("Access Key");
        setSize(760, 500);
        setMinimumSize(new Dimension(720, 460));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void setupUI() {
        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(36, 42, 36, 42));
        setContentPane(root);

        JPanel card = new RoundedPanel(UITheme.getPanelBackground(darkMode), 28);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(42, 48, 42, 48));
        card.setPreferredSize(new Dimension(520, 360));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel logo = new JLabel("あ", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(UITheme.getAccent(darkMode));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Yu Gothic UI", Font.BOLD, 34));
        logo.setPreferredSize(new Dimension(66, 66));
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        card.add(logo, gbc);

        JLabel title = new JLabel("Enter access key", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        title.setForeground(UITheme.getTitleForeground(darkMode));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(24, 0, 8, 0);
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Xác thực key trước khi Login / Activate.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.getTextForeground(darkMode));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 26, 0);
        card.add(subtitle, gbc);

        keyField = createKeyField();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(keyField, gbc);

        JButton continueButton = createPrimaryButton("Tiếp tục");
        continueButton.addActionListener(e -> validateKey());
        getRootPane().setDefaultButton(continueButton);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(continueButton, gbc);

        statusLabel = new JLabel("Nhập access key để tiếp tục.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(UITheme.getTextForeground(darkMode));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(statusLabel, gbc);

        root.add(card, new GridBagConstraints());
    }

    private JTextField createKeyField() {
        JTextField field = new JTextField();
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        field.setForeground(UITheme.getTitleForeground(darkMode));
        field.setBackground(darkMode ? new Color(0x0F172A) : new Color(0xF8FBFF));
        field.setCaretColor(UITheme.getAccent(darkMode));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.getBorder(darkMode), 1, true),
                new EmptyBorder(13, 16, 13, 16)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.selectAll();
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.getAccent(darkMode), 1, true),
                        new EmptyBorder(13, 16, 13, 16)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.getBorder(darkMode), 1, true),
                        new EmptyBorder(13, 16, 13, 16)
                ));
            }
        });
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        button.setBackground(UITheme.getAccent(darkMode));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(180, 48));
        return button;
    }

    private void validateKey() {
        String value = keyField.getText().trim();
        if (DEFAULT_ACCESS_KEY.equals(value)) {
            navigator.navigateTo(MenuOptions.Activate);
            return;
        }

        statusLabel.setText("Key không đúng. Vui lòng nhập lại.");
        statusLabel.setForeground(new Color(0xF87171));
        keyField.requestFocusInWindow();
        keyField.selectAll();
    }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            Color top = darkMode ? new Color(0x111827) : new Color(0xF8FBFF);
            Color bottom = UITheme.getBackground(darkMode);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRect(0, 0, getWidth(), h);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color background;
        private final int arc;

        RoundedPanel(Color background, int arc) {
            this.background = background;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 36));
            g2.fillRoundRect(10, 12, getWidth() - 16, getHeight() - 16, arc, arc);
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

