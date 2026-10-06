package com.mescode.japanese.view.kana;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.view.components.BackButton;
import com.mescode.japanese.util.theme.UITheme;
import com.mescode.japanese.view.components.MenuActionCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class KanaMenuFrame extends JFrame {
    private static final int CONTENT_MAX_WIDTH = 820;
    private static final int CONTENT_MIN_WIDTH = 560;
    private static final int MENU_ITEM_HEIGHT = 64;

    private final AppNavigator navigator;
    private final AppContext appContext;
    private boolean darkMode;

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
    private Timer backgroundTimer;
    private float backgroundPhase;

    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 32);
    private final Font subtitleFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font sectionFont = new Font("Segoe UI Semibold", Font.BOLD, 12);
    private final Font itemFont = new Font("Segoe UI Semibold", Font.BOLD, 16);
    private final Font badgeFont = new Font("Segoe UI Semibold", Font.BOLD, 12);
    private final Font iconFont = new Font("Yu Gothic UI", Font.BOLD, 18);

    public KanaMenuFrame(AppNavigator navigator) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();
        configureTheme(appContext.isDarkMode());
        appContext.addThemeListener(dark -> SwingUtilities.invokeLater(() -> refreshTheme(dark)));
        setTitle("Kana Practice");
        setupFrame();
        setupUI();
        startAnimatedBackground();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(900, 680);
        setMinimumSize(new Dimension(580, 480));
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
        cardBackground = dark ? new Color(0x243249) : UITheme.getCardBackground(false);
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
        setupUI();
        revalidate();
        repaint();
    }

    private void setupUI() {
        GradientPanel contentPanel = new GradientPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(background);
        setContentPane(contentPanel);

        JPanel viewport = new JPanel(new GridBagLayout());
        viewport.setOpaque(false);
        viewport.setBorder(new EmptyBorder(28, 32, 28, 32));

        JScrollPane scrollPane = new JScrollPane(viewport);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel menuCard = new MenuCardPanel();
        menuCard.setLayout(new BoxLayout(menuCard, BoxLayout.Y_AXIS));
        menuCard.setBorder(new EmptyBorder(28, 32, 30, 32));
        menuCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuCard.add(createHeroHeader());
        menuCard.add(Box.createVerticalStrut(18));

        List<MenuSection> sections = List.of(
                new MenuSection("MENU", List.of(
                        new MenuEntry("Hiragana", "Luyện chữ mềm dùng cho từ thuần Nhật.", "あ", AppRoute.HiraQuiz),
                        new MenuEntry("Katakana", "Luyện chữ góc cạnh dùng cho từ vay mượn.", "ア", AppRoute.KataQuiz)
                ))
        );

        for (int i = 0; i < sections.size(); i++) {
            menuCard.add(createMenuSection(sections.get(i)));
            if (i < sections.size() - 1) {
                menuCard.add(Box.createVerticalStrut(14));
            }
        }

        menuCard.add(Box.createVerticalStrut(14));
        // BackButton
        menuCard.add(createBackButton());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.CENTER;
        viewport.add(menuCard, constraints);
    }

    private JComponent createHeroHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("JSA - Japanese Studying Application");
        badge.setFont(badgeFont);
        badge.setForeground(darkMode ? new Color(0xE2E8F0) : new Color(0x334155));
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        badge.setOpaque(true);
        badge.setBackground(darkMode ? new Color(0x1E293B) : new Color(0xE0F2FE));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(badge);
        textPanel.add(Box.createVerticalStrut(14));

        JLabel title = label("Kana", titleFont, titleForeground);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(label(
                "Chọn hệ chữ để luyện nhận diện kana và ghi nhớ cách đọc romaji.",
                subtitleFont,
                textForeground));

        header.add(textPanel, BorderLayout.CENTER);
        header.add(new KanaMark(), BorderLayout.EAST);
        return header;
    }

    private JComponent createMenuSection(MenuSection section) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setOpaque(false);
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        sectionPanel.add(createSectionHeader(section.title()));
        sectionPanel.add(Box.createVerticalStrut(6));
        for (int i = 0; i < section.entries().size(); i++) {
            sectionPanel.add(createMenuItem(section.entries().get(i)));
            if (i < section.entries().size() - 1) {
                sectionPanel.add(Box.createVerticalStrut(8));
            }
        }
        return sectionPanel;
    }

    private JComponent createSectionHeader(String value) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        header.setPreferredSize(new Dimension(1, 26));

        JLabel label = label(value.toUpperCase(), sectionFont, accent);
        label.setBorder(new EmptyBorder(0, 2, 5, 0));
        header.add(label);

        JSeparator separator = new JSeparator();
        separator.setForeground(border);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(separator);
        return header;
    }

    private JComponent createMenuItem(MenuEntry entry) {
        MenuActionCard actionCard = new MenuActionCard(
                entry.title(), entry.description(), new MenuMark(entry.mark()),
                () -> darkMode,
                () -> navigator.navigateTo(entry.option())
        );
        actionCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        return actionCard;
    }

    private JLabel label(String value, Font font, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void startAnimatedBackground() {
        if (backgroundTimer != null && backgroundTimer.isRunning()) {
            return;
        }
        backgroundTimer = new Timer(40, e -> {
            backgroundPhase += 0.018f;
            getContentPane().repaint();
        });
        backgroundTimer.start();
    }

    @Override
    public void dispose() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }
        super.dispose();
    }

    private JComponent createBackButton() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        row.add(new BackButton(
                darkMode,
                () -> navigator.navigateTo(AppRoute.AppMenu)
        ));

        return row;
    }

    private record MenuSection(String title, List<MenuEntry> entries) { }

    private record MenuEntry(String title, String description, String mark, AppRoute option) { }

    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            float wave = (float) ((Math.sin(backgroundPhase) + 1) / 2.0);
            Color top = darkMode ? new Color(0x0B1220) : new Color(0xD4E0EC);
            Color mid = blend(glow, background, darkMode ? 0.10f : 0.08f);
            int driftY = Math.round((wave - 0.5f) * height * 0.18f);

            g2.setPaint(new GradientPaint(0, -height / 5 + driftY, top, width, height + driftY, background));
            g2.fillRect(0, 0, width, height);

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.16f : 0.12f));
            g2.setStroke(new BasicStroke(76f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int sweep = Math.round((backgroundPhase * 120) % Math.max(1, width + 360));
            g2.setColor(mid);
            g2.drawLine(-260 + sweep, height + 120, 280 + sweep, -120);

            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.10f : 0.08f));
            g2.setStroke(new BasicStroke(48f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int reverseSweep = Math.round((backgroundPhase * 82) % Math.max(1, width + 420));
            g2.setColor(blend(accent, background, darkMode ? 0.11f : 0.08f));
            g2.drawLine(width + 220 - reverseSweep, -90, width - 320 - reverseSweep, height + 110);

            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.07f : 0.05f));
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(darkMode ? new Color(255, 255, 255, 120) : new Color(14, 165, 233, 120));
            int lineOffset = Math.round((backgroundPhase * 18) % 44);
            for (int x = -80 + lineOffset; x < width + 120; x += 44) {
                g2.drawLine(x, 0, x - 160, height);
            }
            g2.setComposite(originalComposite);
            g2.dispose();
        }
    }

    private class MenuCardPanel extends JPanel {
        MenuCardPanel() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferred = super.getPreferredSize();
            int availableWidth = getParent() == null ? CONTENT_MAX_WIDTH : getParent().getWidth() - 72;
            int width = Math.min(CONTENT_MAX_WIDTH, Math.max(CONTENT_MIN_WIDTH, availableWidth));
            return new Dimension(width, preferred.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(15, 23, 42, darkMode ? 46 : 28));
            g2.fillRoundRect(8, 10, width - 16, height - 16, 24, 24);
            g2.setColor(panelBackground);
            g2.fillRoundRect(0, 0, width - 12, height - 12, 24, 24);
            g2.setColor(darkMode ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 130));
            g2.drawRoundRect(0, 0, width - 13, height - 13, 24, 24);
            g2.dispose();
        }
    }

    private class KanaMark extends JComponent {
        KanaMark() {
            setPreferredSize(new Dimension(92, 92));
            setMinimumSize(new Dimension(92, 92));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setColor(darkMode ? new Color(56, 189, 248, 22) : new Color(14, 165, 233, 22));
            g2.fillOval(x - 8, y + 8, size, size);
            g2.setPaint(new GradientPaint(x, y, accent, x + size, y + size, glow));
            g2.fillRoundRect(x, y, size, size, 24, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Yu Gothic UI", Font.BOLD, 27));
            drawCenteredText(g2, "かな", x, y, size, size, -2);
            g2.dispose();
        }
    }

    private class MenuMark extends JComponent {
        private final String mark;

        MenuMark(String mark) {
            this.mark = mark;
            setPreferredSize(new Dimension(38, 38));
            setMinimumSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            Color base = darkMode ? new Color(0x1E293B) : UITheme.getCardBackground(false);
            g2.setColor(base);
            g2.fillRoundRect(0, 0, size, size, 13, 13);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), darkMode ? 70 : 95));
            g2.drawRoundRect(0, 0, size - 1, size - 1, 13, 13);
            g2.setColor(accent);
            g2.setFont(iconFont);
            drawCenteredText(g2, mark, 0, 0, size, size, -1);
            g2.dispose();
        }
    }

    private void drawCenteredText(Graphics2D g2, String value, int x, int y, int width, int height, int yOffset) {
        FontMetrics metrics = g2.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(value)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent() + yOffset;
        g2.drawString(value, textX, textY);
    }

    private Color blend(Color top, Color bottom, float amount) {
        float keep = 1f - amount;
        int red = Math.round(top.getRed() * amount + bottom.getRed() * keep);
        int green = Math.round(top.getGreen() * amount + bottom.getGreen() * keep);
        int blue = Math.round(top.getBlue() * amount + bottom.getBlue() * keep);
        return new Color(red, green, blue);
    }
}

