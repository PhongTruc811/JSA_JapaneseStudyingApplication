package com.mescode.japanese.view.menu;

import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuFrame extends JFrame {
    private final MenuNavigator menuNavigator;
    private boolean darkMode;

    private Color background;
    private Color panelBackground;
    private Color border;
    private Color hover;
    private Color pressed;
    private Color titleForeground;
    private Color textForeground;
    private Color accent;

    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 30);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font sectionFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font itemFont = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font badgeFont = new Font("Segoe UI Semibold", Font.BOLD, 12);

    public MenuFrame(MenuNavigator nav) {
        this.menuNavigator = nav;
        this.darkMode = nav != null && nav.getAppContext().isDarkMode();
        if (nav != null) {
            nav.getAppContext().addThemeListener(isDark -> SwingUtilities.invokeLater(() -> refreshTheme(isDark)));
        }
        setTitle("Menu");
        setupFrame();
        refreshTheme(darkMode);
        setupUI();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(620, 520);
        setMinimumSize(new Dimension(580, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
    }

    private void refreshTheme(boolean dark) {
        this.darkMode = dark;
        background = UITheme.getBackground(dark);
        panelBackground = UITheme.getPanelBackground(dark);
        border = UITheme.getBorder(dark);
        hover = UITheme.getHover(dark);
        pressed = UITheme.getPressed(dark);
        titleForeground = UITheme.getTitleForeground(dark);
        textForeground = UITheme.getTextForeground(dark);
        accent = UITheme.getAccent(dark);
        getContentPane().setBackground(background);
        repaint();
    }

    private void setupUI() {
        GradientPanel contentPanel = new GradientPanel();
        contentPanel.setLayout(new BorderLayout());
        setContentPane(contentPanel);

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        add(root, BorderLayout.CENTER);

        JPanel card = new ShadowPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 30, 28, 30));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(540, 420));

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLabel = new JLabel("Japanese Alphabet Quiz");
        badgeLabel.setFont(badgeFont);
        badgeLabel.setForeground(darkMode ? new Color(0xE2E8F0) : new Color(0x334155));
        badgeLabel.setBorder(new EmptyBorder(6, 12, 6, 12));
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(darkMode ? new Color(0x1E293B) : new Color(0xE0F2FE));
        badgeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.add(badgeLabel);
        card.add(badge);
        card.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Menu");
        title.setFont(titleFont);
        title.setForeground(titleForeground);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Chọn module để bắt đầu học, luyện tập và theo dõi tiến độ.");
        subtitle.setFont(subtitleFont);
        subtitle.setForeground(textForeground);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        card.add(createSectionHeader                                                                          ("Learning modules"));
        card.add(Box.createVerticalStrut(6));
        card.add(createMenuItem("Vocabulary", "Từ vựng", MenuOptions.Vocab));
        card.add(Box.createVerticalStrut(10));
        card.add(createMenuItem("Grammar Exercise", "Ngữ pháp", MenuOptions.Grammar));
        card.add(Box.createVerticalStrut(10));
        card.add(createMenuItem("Kana", "Hiragana / Katakana", MenuOptions.Kana));
        card.add(Box.createVerticalStrut(14));

        card.add(createSectionHeader("Settings"));
        card.add(Box.createVerticalStrut(6));
        card.add(createMenuItem("Settings", "Đổi Light/Dark theme và cấu hình app", MenuOptions.Settings));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(card, gbc);
    }

    private JComponent createSectionHeader(String text) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        header.setPreferredSize(new Dimension(1, 28));

        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(sectionFont);
        label.setForeground(accent);
        label.setBorder(new EmptyBorder(0, 2, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(label);

        JSeparator separator = new JSeparator();
        separator.setForeground(border);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(separator);
        return header;
    }

    private JComponent createMenuItem(String title, String description, MenuOptions option) {
        RoundedActionCard card = new RoundedActionCard(title, description, option, menuNavigator);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color top = darkMode ? new Color(0x0B1220) : new Color(0xF8FBFF);
            Color bottom = background;
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRect(0, 0, w, h);
            g2.setColor(darkMode ? new Color(255, 255, 255, 12) : new Color(14, 165, 233, 14));
            g2.fillOval(w - 180, -90, 230, 230);
            g2.fillOval(-80, h - 120, 180, 180);
            g2.dispose();
        }
    }

    private class ShadowPanel extends JPanel {
        ShadowPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(15, 23, 42, darkMode ? 45 : 28));
            g2.fillRoundRect(8, 10, w - 16, h - 16, 24, 24);
            g2.setColor(panelBackground);
            g2.fillRoundRect(0, 0, w - 12, h - 12, 24, 24);
            g2.setColor(new Color(255, 255, 255, darkMode ? 18 : 120));
            g2.drawRoundRect(0, 0, w - 13, h - 13, 24, 24);
            g2.dispose();
        }
    }

    private class RoundedActionCard extends JPanel {
        private final MenuOptions option;
        private final MenuNavigator navigator;
        private boolean hovered;
        private boolean pressedState;

        RoundedActionCard(String title, String description, MenuOptions option, MenuNavigator navigator) {
            this.option = option;
            this.navigator = navigator;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(14, 16, 14, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 68));

            JPanel textWrap = new JPanel();
            textWrap.setOpaque(false);
            textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(itemFont);
            titleLabel.setForeground(titleForeground);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel descLabel = new JLabel(description);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLabel.setForeground(textForeground);
            descLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textWrap.add(titleLabel);
            textWrap.add(descLabel);
            add(textWrap, BorderLayout.CENTER);

            JLabel arrow = new JLabel("›", SwingConstants.CENTER);
            arrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
            arrow.setForeground(darkMode ? new Color(0x94A3B8) : new Color(0x64748B));
            arrow.setPreferredSize(new Dimension(18, 28));
            add(arrow, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressedState = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressedState = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressedState = false;
                    hovered = getBounds().contains(e.getPoint());
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (navigator != null) {
                        navigator.navigateTo(option);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            Color fill = pressedState ? pressed : hovered ? hover : panelBackground;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, 18, 18);
            g2.setColor(hovered ? accent : border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.dispose();
        }
    }
}
