package com.mescode.japanese.view.activate;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ActivateFrame extends JFrame {
    private final AppNavigator navigator;
    private final boolean darkMode;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField licenseField;
    private JLabel statusLabel;
    private JButton activateButton;

    public ActivateFrame(AppNavigator navigator) {
        this.navigator = navigator;
        this.darkMode = navigator.getAppContext().isDarkMode();

        setupFrame();
        setupUI();
    }

    private void setupFrame() {
        setTitle("Activate");
        setSize(1100, 720);
        setMinimumSize(new Dimension(920, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void setupUI() {
        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(42, 54, 42, 54));
        setContentPane(root);

        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setPreferredSize(new Dimension(980, 560));

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 0.44;
        left.weighty = 1.0;
        left.fill = GridBagConstraints.BOTH;
        shell.add(createIntroPanel(), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.weightx = 0.56;
        right.weighty = 1.0;
        right.fill = GridBagConstraints.BOTH;
        right.insets = new Insets(0, 28, 0, 0);
        shell.add(createActivationPanel(), right);

        root.add(shell, new GridBagConstraints());
    }

    private JPanel createIntroPanel() {
        JPanel panel = new RoundedPanel(UITheme.getPanelBackground(darkMode), 28);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(38, 36, 38, 36));

        JLabel logo = new JLabel("あ", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(UITheme.getAccent(darkMode));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Yu Gothic UI", Font.BOLD, 42));
        logo.setPreferredSize(new Dimension(78, 78));
        logo.setMaximumSize(new Dimension(78, 78));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(logo);
        panel.add(Box.createVerticalStrut(30));

        JLabel title = new JLabel("<html>Activate Account</html>");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 34));
        title.setForeground(UITheme.getTitleForeground(darkMode));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(14));

        // dùng html hiển thị text xuống dòng khi thu nhỏ cửa sổ app
        JLabel subtitle = new JLabel("<html>Đăng nhập, nhập license key và kích hoạt quyền sử dụng.</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(UITheme.getTextForeground(darkMode));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(30));

        panel.add(createStep("1", "Đăng nhập tài khoản", "Xác định user trước khi gọi entitlement backend."));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createStep("2", "Nhập license key", "Gắn license vào user hoặc thiết bị hiện tại."));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createStep("3", "Kích hoạt quyền", "Mở khóa app khi backend trả về entitlement hợp lệ."));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createActivationPanel() {
        JPanel panel = new RoundedPanel(UITheme.getPanelBackground(darkMode), 28);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(38, 44, 38, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JLabel title = new JLabel("Activate your learning license");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        title.setForeground(UITheme.getTitleForeground(darkMode));
        gbc.gridy = 0;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Đăng nhập và nhập license key để test backend entitlement.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.getTextForeground(darkMode));
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 26, 0);
        panel.add(subtitle, gbc);

        emailField = createTextField("Email");
        licenseField = createTextField("License key");

        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 2;
        panel.add(createLabeledField("Email", emailField), gbc);
        gbc.gridy = 3;
        gbc.gridy = 4;
        panel.add(createLabeledField("License key", licenseField), gbc);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton loginButton = createSecondaryButton("Login");
        loginButton.addActionListener(e -> statusLabel.setText("Đã nhận thông tin đăng nhập. Sẵn sàng gọi Auth API."));
        actions.add(loginButton);

        activateButton = createPrimaryButton("Activate");
        activateButton.addActionListener(e -> handleActivate());
        actions.add(activateButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 18, 0);
        panel.add(actions, gbc);

        statusLabel = new JLabel("Chưa kích hoạt. Vui lòng đăng nhập và nhập license key.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(UITheme.getTextForeground(darkMode));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 22, 0);
        panel.add(statusLabel, gbc);

        JButton continueButton = createTextButton("Skip for development");
        continueButton.addActionListener(e -> navigator.navigateTo(AppRoute.AppMenu));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(continueButton, gbc);

        gbc.gridy = 8;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createStep(String number, String title, String detail) {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel badge = new JLabel(number, SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(UITheme.getGlow(darkMode));
        badge.setPreferredSize(new Dimension(36, 36));
        panel.add(badge, BorderLayout.WEST);

        JLabel text = new JLabel("<html><b>" + title + "</b><br>" + detail + "</html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(UITheme.getTextForeground(darkMode));
        panel.add(text, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        fieldLabel.setForeground(UITheme.getTitleForeground(darkMode));
        wrapper.add(fieldLabel, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        styleInput(field, placeholder);
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        styleInput(field, placeholder);
        return field;
    }

    private void styleInput(JTextField field, String placeholder) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(UITheme.getTitleForeground(darkMode));
        field.setBackground(darkMode ? new Color(0x0F172A) : new Color(0xF8FBFF));
        field.setCaretColor(UITheme.getAccent(darkMode));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.getBorder(darkMode), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        field.setToolTipText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.getAccent(darkMode), 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.getBorder(darkMode), 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                ));
            }
        });
    }

    private JButton createPrimaryButton(String text) {
        JButton button = createButton(text);
        button.setBackground(UITheme.getAccent(darkMode));
        button.setForeground(Color.WHITE);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = createButton(text);
        button.setBackground(UITheme.getCardBackground(darkMode));
        button.setForeground(UITheme.getTitleForeground(darkMode));
        return button;
    }

    private JButton createTextButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setForeground(UITheme.getGlow(darkMode));
        return button;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 46));
        return button;
    }

    private void handleActivate() {
        String email = emailField.getText().trim();
        String license = licenseField.getText().trim();
        if (email.isEmpty() || license.isEmpty()) {
            statusLabel.setText("Cần email và license key trước khi kích hoạt.");
            return;
        }
        statusLabel.setText("Đang chờ backend entitlement xác minh license cho " + email + ".");
    }

    public JTextField getEmailField() {
        return emailField;
    }


    public JTextField getLicenseField() {
        return licenseField;
    }

    public JButton getActivateButton() {
        return activateButton;
    }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            Color top = darkMode ? new Color(0x111827) : new Color(0xD4E0EC);
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


