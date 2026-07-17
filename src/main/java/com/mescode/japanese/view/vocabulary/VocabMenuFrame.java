package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VocabMenuFrame extends JFrame {
    private final MenuNavigator menuNavigator;
    private final AppContext appContext;

    private boolean darkMode;
    private boolean uiBuilt;

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
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font sectionFont = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font itemTitleFont = new Font("Segoe UI Semibold", Font.BOLD, 16);
    private final Font itemDescriptionFont = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font badgeFont = new Font("Segoe UI Semibold", Font.BOLD, 12);

    public VocabMenuFrame(MenuNavigator nav) {
        this.menuNavigator = nav;
        this.appContext = nav.getAppContext();
        this.darkMode = appContext.isDarkMode();

        appContext.addThemeListener(isDark -> SwingUtilities.invokeLater(() -> refreshTheme(isDark)));
        setTitle("Vocabulary Menu");

        setupFrame();
        configureTheme(darkMode);
        setupUI();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(720, 720);
        setMinimumSize(new Dimension(660, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
    }

    private void configureTheme(boolean dark) {
        darkMode = dark;
        background = UITheme.getBackground(dark);
        panelBackground = UITheme.getPanelBackground(dark);
        cardBackground = UITheme.getCardBackground(dark);
        border = UITheme.getBorder(dark);
        hover = UITheme.getHover(dark);
        pressed = UITheme.getPressed(dark);
        titleForeground = UITheme.getTitleForeground(dark);
        textForeground = UITheme.getTextForeground(dark);
        accent = UITheme.getAccent(dark);
        glow = UITheme.getGlow(dark);
    }

    private void refreshTheme(boolean dark) {
        configureTheme(dark);
        if (uiBuilt) {
            setupUI();
            revalidate();
        }
        repaint();
    }

    private void setupUI() {
        GradientPanel contentPanel = new GradientPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(contentPanel);

        JPanel card = new ShadowPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 32, 26, 32));
        card.setPreferredSize(new Dimension(560, 580));
        card.setMaximumSize(new Dimension(560, 600));

        card.add(buildMetaRow());
        card.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Vocabulary");
        title.setFont(titleFont);
        title.setForeground(titleForeground);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Build your word bank, review meanings, and start focused quizzes.");
        subtitle.setFont(subtitleFont);
        subtitle.setForeground(textForeground);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        card.add(createSectionHeader("Study actions"));
        card.add(Box.createVerticalStrut(8));

        card.add(createMenuItem("+", "Add new word", "Create a custom word card for your own study list.", MenuOptions.AddVocab));
        card.add(Box.createVerticalStrut(8));
        card.add(createMenuItem("?", "Practice quiz", "Practice all kanji & hiragana words from chapter 1-3", MenuOptions.VocabQuiz));
        card.add(Box.createVerticalStrut(8));
        card.add(createMenuItem("#", "Word List (Chapter 1-3)", "Browse all vocabulary currently loaded in the app.", MenuOptions.ShowVocab));
        card.add(Box.createVerticalStrut(8));
        card.add(createMenuItem("漢", "Kanji List (Unit 1-3)", "Review Kanji, Hiragana, Hán Việt readings, and meanings.", MenuOptions.ShowKanji));
        card.add(Box.createVerticalStrut(8));
        card.add(createMenuItem("<", "Back", "Return to the main menu.", MenuOptions.MenuHome));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(card, gbc);

        uiBuilt = true;
    }

    private JComponent buildMetaRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        row.add(createPill("Vocabulary Module", false), BorderLayout.WEST);
        row.add(createPill(vocabCountText(), true), BorderLayout.EAST);
        return row;
    }

    private JLabel createPill(String text, boolean accented) {
        JLabel label = new JLabel(text);
        label.setFont(badgeFont);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(7, 12, 7, 12));
        label.setForeground(accented ? Color.WHITE : textForeground);
        label.setBackground(accented ? accent : cardBackground);
        return label;
    }

    private String vocabCountText() {
        int count = appContext.getVocabs() == null ? 0 : appContext.getVocabs().size();
        return count + (count == 1 ? " word" : " words");
    }

    private JComponent createSectionHeader(String text) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        header.setPreferredSize(new Dimension(1, 26));

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

    private JComponent createMenuItem(String mark, String title, String description, MenuOptions option) {
        RoundedActionCard card = new RoundedActionCard(mark, title, description, option);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            Color top = darkMode ? new Color(0x0B1220) : new Color(0xF8FBFF);
            g2.setPaint(new GradientPaint(0, 0, top, 0, height, background));
            g2.fillRect(0, 0, width, height);

            g2.setColor(darkMode ? new Color(56, 189, 248, 18) : new Color(14, 165, 233, 16));
            g2.fillOval(width - 220, -110, 280, 280);
            g2.setColor(darkMode ? new Color(249, 115, 22, 16) : new Color(234, 88, 12, 12));
            g2.fillOval(-100, height - 180, 240, 240);
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

            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(15, 23, 42, darkMode ? 45 : 28));
            g2.fillRoundRect(8, 10, width - 16, height - 16, 24, 24);
            g2.setColor(panelBackground);
            g2.fillRoundRect(0, 0, width - 12, height - 12, 24, 24);
            g2.setColor(darkMode ? new Color(255, 255, 255, 18) : border);
            g2.drawRoundRect(0, 0, width - 13, height - 13, 24, 24);
            g2.dispose();
        }
    }

    private class RoundedActionCard extends JPanel {
        private final MenuOptions option;
        private boolean hovered;
        private boolean pressedState;

        RoundedActionCard(String mark, String title, String description, MenuOptions option) {
            this.option = option;

            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            setPreferredSize(new Dimension(1, 64));

            add(new ActionMark(mark), BorderLayout.WEST);
            add(buildText(title, description), BorderLayout.CENTER);

            JLabel arrow = new JLabel(">", SwingConstants.CENTER);
            arrow.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
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
                    hovered = contains(e.getPoint());
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (menuNavigator != null) {
                        menuNavigator.navigateTo(option);
                    }
                }
            });
        }

        private JComponent buildText(String title, String description) {
            JPanel textWrap = new JPanel();
            textWrap.setOpaque(false);
            textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(itemTitleFont);
            titleLabel.setForeground(titleForeground);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setFont(itemDescriptionFont);
            descriptionLabel.setForeground(textForeground);
            descriptionLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
            descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textWrap.add(titleLabel);
            textWrap.add(descriptionLabel);
            return textWrap;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            Color fill = pressedState ? pressed : hovered ? hover : cardBackground;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, width, height, 18, 18);
            g2.setColor(hovered ? accent : border);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 18, 18);
            g2.dispose();
        }

        private class ActionMark extends JComponent {
            private final String mark;

            ActionMark(String mark) {
                this.mark = mark;
                setPreferredSize(new Dimension(42, 42));
                setMinimumSize(new Dimension(42, 42));
                setMaximumSize(new Dimension(42, 42));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color start = hovered ? glow : accent;
                Color end = hovered ? accent : glow;
                g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("漢".equals(mark) ? "Yu Gothic UI" : "Segoe UI Semibold", Font.BOLD, 18));
                FontMetrics metrics = g2.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(mark)) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.drawString(mark, x, y);
                g2.dispose();
            }
        }
    }
}

