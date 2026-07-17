package com.mescode.japanese.view.settings;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.model.User;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsFrame extends JFrame {
    private final MenuNavigator menuNavigator;
    private final AppContext appContext;

    private Color background;
    private Color panelBackground;
    private Color cardBackground;
    private Color border;
    private Color hover;
    private Color pressed;
    private Color titleForeground;
    private Color textForeground;
    private Color accent;
    private Color glow;

    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 30);
    private final Font headingFont = new Font("Segoe UI Semibold", Font.BOLD, 18);
    private final Font labelFont = new Font("Segoe UI Semibold", Font.BOLD, 14);
    private final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font smallFont = new Font("Segoe UI", Font.PLAIN, 12);

    public SettingsFrame(MenuNavigator menuNavigator, AppContext appContext) {
        this.menuNavigator = menuNavigator;
        this.appContext = appContext;
        setTitle("Settings");
        configureTheme();
        setupFrame();
        setupUI();
        setVisible(true);
    }

    private void configureTheme() {
        boolean darkMode = appContext.isDarkMode();
        background = UITheme.getBackground(darkMode);
        panelBackground = UITheme.getPanelBackground(darkMode);
        cardBackground = darkMode ? new Color(0x253247) : new Color(0xF8FBFF);
        border = UITheme.getBorder(darkMode);
        hover = UITheme.getHover(darkMode);
        pressed = UITheme.getPressed(darkMode);
        titleForeground = UITheme.getTitleForeground(darkMode);
        textForeground = UITheme.getTextForeground(darkMode);
        accent = UITheme.getAccent(darkMode);
        glow = UITheme.getGlow(darkMode);
    }

    private void setupFrame() {
        setSize(960, 720);
        setMinimumSize(new Dimension(760, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
    }

    private void setupUI() {
        GradientPanel content = new GradientPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(content);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));

        stack.add(buildHeader());
        stack.add(Box.createVerticalStrut(18));
        stack.add(createSectionCard("Appearance", "Theme and display", "Tune the app to feel comfortable while studying.", buildAppearancePanel()));
        stack.add(Box.createVerticalStrut(14));
        stack.add(createSectionCard("Account", "Profile", "Your current learning session and login state.", buildProfilePanel()));
        stack.add(Box.createVerticalStrut(14));
        stack.add(createSectionCard("App", "About and actions", "Version info and maintenance actions.", buildAboutPanel()));

        Dimension preferred = stack.getPreferredSize();
        stack.setPreferredSize(new Dimension(760, preferred.height));

        JPanel scrollBody = new JPanel(new GridBagLayout());
        scrollBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        scrollBody.add(stack, gbc);

        JScrollPane scrollPane = new JScrollPane(scrollBody);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        content.add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        JPanel topLine = new JPanel(new BorderLayout(12, 0));
        topLine.setOpaque(false);
        topLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        topLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel badge = new JLabel("Preferences");
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        badge.setForeground(appContext.isDarkMode() ? new Color(0xE2E8F0) : new Color(0x334155));
        badge.setOpaque(true);
        badge.setBackground(appContext.isDarkMode() ? new Color(0x1E293B) : new Color(0xE0F2FE));
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        topLine.add(badge, BorderLayout.WEST);

        RoundedButton back = new RoundedButton("Back to Menu", false, false);
        back.setPreferredSize(new Dimension(126, 36));
        back.addActionListener(e -> menuNavigator.navigateTo(MenuOptions.MenuHome));

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(back);
        topLine.add(backWrap, BorderLayout.EAST);

        header.add(topLine);
        header.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Settings");
        title.setFont(titleFont);
        title.setForeground(titleForeground);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(5));

        JLabel subtitle = new JLabel("Make the interface calmer, clearer, and easier to use.");
        subtitle.setFont(bodyFont);
        subtitle.setForeground(textForeground);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(subtitle);
        return header;
    }

    private JComponent buildAppearancePanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        RoundedButton themeButton = new RoundedButton(themeButtonText(), true, false);
        themeButton.setPreferredSize(new Dimension(180, 42));
        themeButton.addActionListener(e -> {
            appContext.toggleTheme();
            refreshScreen();
        });
        panel.add(createSettingRow("Color theme", currentThemeText(), themeButton));
        panel.add(Box.createVerticalStrut(10));

        JCheckBox soundCheckBox = new JCheckBox("Enabled");
        soundCheckBox.setSelected(appContext.isSoundEnabled());
        soundCheckBox.setFont(labelFont);
        soundCheckBox.setForeground(titleForeground);
        soundCheckBox.setBackground(cardBackground);
        soundCheckBox.setOpaque(false);
        soundCheckBox.setFocusPainted(false);
        soundCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        soundCheckBox.addActionListener(e -> appContext.setSoundEnabled(soundCheckBox.isSelected()));
        panel.add(createSettingRow("Sound effects", "Play feedback sounds during quizzes.", soundCheckBox));
        panel.add(Box.createVerticalStrut(10));

        JComboBox<String> fontSizeCombo = new JComboBox<>(new String[]{"Small", "Medium", "Large"});
        fontSizeCombo.setSelectedIndex(appContext.getFontSize() == 12 ? 0 : appContext.getFontSize() == 16 ? 2 : 1);
        fontSizeCombo.setFont(bodyFont);
        fontSizeCombo.setPreferredSize(new Dimension(150, 34));
        fontSizeCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fontSizeCombo.addActionListener(e -> {
            String selected = (String) fontSizeCombo.getSelectedItem();
            int size = "Small".equals(selected) ? 12 : "Large".equals(selected) ? 16 : 14;
            appContext.setFontSize(size);
        });
        panel.add(createSettingRow("Font size", "Choose a comfortable reading size.", fontSizeCombo));

        return panel;
    }

    private JComponent buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);

        AvatarMark avatar = new AvatarMark();
        panel.add(avatar, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        User user = appContext.getCurrentUser();
        JLabel name = new JLabel(user == null ? "Guest session" : user.getName());
        name.setFont(labelFont);
        name.setForeground(titleForeground);
        text.add(name);
        text.add(Box.createVerticalStrut(4));

        JLabel detail = new JLabel(user == null ? "Login to sync your profile when authentication is ready." : user.getEmail());
        detail.setFont(bodyFont);
        detail.setForeground(textForeground);
        text.add(detail);

        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildAboutPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createActionRow(
                "i",
                "Japanese Alphabet Quiz",
                "Version " + appContext.getAppVersion() + " | Kana, vocabulary, and grammar practice.",
                "About",
                false,
                false,
                this::showAboutDialog));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow(
                "R",
                "Reset preferences",
                "Restore dark mode, sound effects, and medium font size.",
                "Reset",
                false,
                false,
                this::confirmResetPreferences));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createActionRow(
                "P",
                "Reset progress",
                "Clear learned vocabulary progress while keeping saved favorites.",
                "Reset",
                false,
                true,
                this::confirmResetProgress));
        return panel;
    }

    private JComponent createSectionCard(String eyebrow, String title, String description, JComponent content) {
        JPanel card = new SectionCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel eyebrowLabel = new JLabel(eyebrow.toUpperCase());
        eyebrowLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        eyebrowLabel.setForeground(accent);
        eyebrowLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(eyebrowLabel);
        card.add(Box.createVerticalStrut(8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(headingFont);
        titleLabel.setForeground(titleForeground);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(bodyFont);
        descriptionLabel.setForeground(textForeground);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descriptionLabel);
        card.add(Box.createVerticalStrut(16));

        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(content);
        Dimension preferred = card.getPreferredSize();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        return card;
    }

    private JComponent createSettingRow(String title, String description, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 12, 10, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(titleForeground);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(smallFont);
        descriptionLabel.setForeground(textForeground);
        text.add(descriptionLabel);

        JPanel controlWrap = new JPanel(new GridBagLayout());
        controlWrap.setOpaque(false);
        controlWrap.setPreferredSize(new Dimension(190, 44));
        controlWrap.add(control);

        row.add(text, BorderLayout.CENTER);
        row.add(controlWrap, BorderLayout.EAST);
        return row;
    }


    private JComponent createActionRow(String mark, String title, String description, String actionText,
                                       boolean primary, boolean danger, Runnable action) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(new ActionMark(mark, danger), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(titleForeground);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(3));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(smallFont);
        descriptionLabel.setForeground(textForeground);
        text.add(descriptionLabel);
        row.add(text, BorderLayout.CENTER);

        RoundedButton button = new RoundedButton(actionText, primary, danger);
        button.setPreferredSize(new Dimension(96, 36));
        button.addActionListener(e -> action.run());

        JPanel buttonWrap = new JPanel(new GridBagLayout());
        buttonWrap.setOpaque(false);
        buttonWrap.setPreferredSize(new Dimension(110, 40));
        buttonWrap.add(button);
        row.add(buttonWrap, BorderLayout.EAST);
        return row;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Japanese Alphabet Quiz\nVersion " + appContext.getAppVersion()
                        + "\nPractice kana, vocabulary, and grammar in one desktop app.",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmResetPreferences() {
        int result = JOptionPane.showConfirmDialog(this,
                "This will restore theme, sound, and font size defaults. Continue?",
                "Reset Preferences",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            appContext.resetPreferences();
            refreshScreen();
            JOptionPane.showMessageDialog(this,
                    "Preferences have been reset.",
                    "Reset",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void confirmResetProgress() {
        int result = JOptionPane.showConfirmDialog(this,
                "This will clear your learned vocabulary progress. Continue?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            appContext.resetProgress();
            JOptionPane.showMessageDialog(this,
                    "Progress has been reset.",
                    "Reset",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String currentThemeText() {
        return appContext.isDarkMode() ? "Dark mode is active." : "Light mode is active.";
    }

    private String themeButtonText() {
        return appContext.isDarkMode() ? "Switch to Light" : "Switch to Dark";
    }

    private void refreshScreen() {
        configureTheme();
        setupUI();
        revalidate();
        repaint();
    }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color top = appContext.isDarkMode() ? new Color(0x0B1220) : new Color(0xF8FBFF);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, background));
            g2.fillRect(0, 0, w, h);
            g2.setColor(appContext.isDarkMode() ? new Color(56, 189, 248, 18) : new Color(14, 165, 233, 16));
            g2.fillOval(w - 210, -100, 280, 280);
            g2.setColor(appContext.isDarkMode() ? new Color(249, 115, 22, 16) : new Color(234, 88, 12, 12));
            g2.fillOval(-100, h - 180, 240, 240);
            g2.dispose();
        }
    }

    private class SectionCard extends JPanel {
        SectionCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(15, 23, 42, appContext.isDarkMode() ? 42 : 22));
            g2.fillRoundRect(8, 10, w - 16, h - 16, 22, 22);
            g2.setColor(panelBackground);
            g2.fillRoundRect(0, 0, w - 12, h - 12, 22, 22);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 13, h - 13, 22, 22);
            g2.dispose();
        }
    }

    private class AvatarMark extends JComponent {
        AvatarMark() {
            setPreferredSize(new Dimension(54, 54));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, accent, getWidth(), getHeight(), glow));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            String text = "G";
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }


    private class ActionMark extends JComponent {
        private final String mark;
        private final boolean danger;

        ActionMark(String mark, boolean danger) {
            this.mark = mark;
            this.danger = danger;
            setPreferredSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color start = danger ? new Color(0xF97316) : accent;
            Color end = danger ? new Color(0xDC2626) : glow;
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(mark)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(mark, x, y);
            g2.dispose();
        }
    }
    private class RoundedButton extends JButton {
        private final boolean primary;
        private final boolean danger;
        private boolean hovered;

        RoundedButton(String text, boolean primary, boolean danger) {
            super(text);
            this.primary = primary;
            this.danger = danger;
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
            setForeground(primary || danger ? Color.WHITE : titleForeground);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 16, 10, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            if (danger) {
                fill = hovered ? new Color(0xDC2626) : new Color(0xB91C1C);
            } else if (primary) {
                fill = hovered ? pressed : accent;
            } else {
                fill = hovered ? hover : cardBackground;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(primary || danger ? fill : border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
