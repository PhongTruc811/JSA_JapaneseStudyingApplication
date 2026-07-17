package com.mescode.japanese.view.splash;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Splash screen shown on app startup.
 * Displays branded loading progress before opening the access key gate.
 */
public class SplashFrame extends JFrame {
    private static final String[] LOADING_MESSAGES = {
            "Loading kana sets...",
            "Preparing vocabulary...",
            "Warming up grammar practice...",
            "Almost ready..."
    };

    private final MenuNavigator navigator;
    private final AppContext appContext;
    private final boolean darkMode;

    private RoundedProgressBar progressBar;
    private JLabel statusLabel;
    private Timer progressTimer;
    private Timer fadeTimer;
    private boolean opacitySupported = true;

    public SplashFrame(MenuNavigator navigator) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();
        this.darkMode = appContext.isDarkMode();

        configureFrame();
        setupUI();
        startIntro();

        setVisible(true);
    }

    private void configureFrame() {
        setTitle("Japanese Alphabet Quiz");
        setSize(520, 380);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setAlwaysOnTop(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void setupUI() {
        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(new EmptyBorder(34, 38, 34, 38));
        setContentPane(root);

        JPanel card = new SplashCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(34, 42, 34, 42));
        card.setPreferredSize(new Dimension(390, 270));
        card.setOpaque(false);

        LogoMark logo = new LogoMark();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);
        card.add(Box.createVerticalStrut(18));

        JLabel titleLabel = new JLabel("Japanese Alphabet Quiz", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        titleLabel.setForeground(UITheme.getTitleForeground(darkMode));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));

        JLabel subtitleLabel = new JLabel("Learn kana, vocabulary and grammar", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(UITheme.getTextForeground(darkMode));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(26));

        statusLabel = new JLabel(LOADING_MESSAGES[0], SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(UITheme.getTextForeground(darkMode));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(320, 18));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));

        progressBar = new RoundedProgressBar();
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(progressBar);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(card, gbc);
    }

    private void startIntro() {
        if (!setOpacitySafely(0f)) {
            startProgressAndNavigate();
            return;
        }
        Timer introTimer = new Timer(18, null);
        introTimer.addActionListener(e -> {
            float nextOpacity = Math.min(1f, getOpacity() + 0.08f);
            if (!setOpacitySafely(nextOpacity)) {
                introTimer.stop();
                startProgressAndNavigate();
                return;
            }
            if (nextOpacity >= 1f) {
                introTimer.stop();
                startProgressAndNavigate();
            }
        });
        introTimer.start();
    }

    private void startProgressAndNavigate() {
        final int durationMs = 2200;
        final int tickMs = 28;
        final int steps = durationMs / tickMs;
        final int increment = Math.max(1, 100 / steps);

        progressTimer = new Timer(tickMs, null);
        progressTimer.addActionListener(e -> {
            int value = progressBar.getValue();
            if (value >= 100) {
                progressTimer.stop();
                progressBar.setValue(100);
                statusLabel.setText("Ready!");
                Timer dismiss = new Timer(260, evt -> fadeOutAndNavigate());
                dismiss.setRepeats(false);
                dismiss.start();
                return;
            }

            value = Math.min(100, value + increment);
            progressBar.setValue(value);
            statusLabel.setText(LOADING_MESSAGES[Math.min(LOADING_MESSAGES.length - 1, value / 30)]);
        });
        progressTimer.start();
    }

    private void fadeOutAndNavigate() {
        setAlwaysOnTop(false);
        if (!opacitySupported) {
            navigateToMenu();
            return;
        }
        fadeTimer = new Timer(18, null);
        fadeTimer.addActionListener(e -> {
            float nextOpacity = Math.max(0f, getOpacity() - 0.08f);
            if (!setOpacitySafely(nextOpacity)) {
                fadeTimer.stop();
                navigateToMenu();
                return;
            }
            if (nextOpacity <= 0f) {
                fadeTimer.stop();
                navigateToMenu();
            }
        });
        fadeTimer.start();
    }

    private void navigateToMenu() {
        dispose();
        SwingUtilities.invokeLater(() -> navigator.navigateTo(MenuOptions.AccessKey));
    }

    private boolean setOpacitySafely(float opacity) {
        if (!opacitySupported) {
            return false;
        }
        try {
            setOpacity(opacity);
            return true;
        } catch (UnsupportedOperationException | IllegalComponentStateException ignored) {
            opacitySupported = false;
            try {
                setOpacity(1f);
            } catch (RuntimeException ignoredAgain) {
                // Keep startup usable when translucent windows are unavailable.
            }
            return false;
        }
    }

    @Override
    public void dispose() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        super.dispose();
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
            Color bottom = UITheme.getBackground(darkMode);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRect(0, 0, w, h);

            g2.setColor(darkMode ? new Color(56, 189, 248, 28) : new Color(14, 165, 233, 22));
            g2.fillOval(w - 160, -70, 210, 210);
            g2.setColor(darkMode ? new Color(249, 115, 22, 22) : new Color(234, 88, 12, 18));
            g2.fillOval(-70, h - 135, 190, 190);
            g2.dispose();
        }
    }

    private class SplashCard extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(15, 23, 42, darkMode ? 52 : 30));
            g2.fillRoundRect(10, 12, w - 20, h - 20, 28, 28);
            g2.setColor(UITheme.getPanelBackground(darkMode));
            g2.fillRoundRect(0, 0, w - 12, h - 12, 28, 28);
            g2.setColor(darkMode ? new Color(255, 255, 255, 22) : new Color(255, 255, 255, 150));
            g2.drawRoundRect(0, 0, w - 13, h - 13, 28, 28);
            g2.dispose();
        }
    }

    private class LogoMark extends JComponent {
        LogoMark() {
            setPreferredSize(new Dimension(78, 78));
            setMaximumSize(new Dimension(78, 78));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(darkMode ? new Color(249, 115, 22, 36) : new Color(234, 88, 12, 28));
            g2.fillOval(x - 5, y + 5, size, size);
            g2.setColor(UITheme.getAccent(darkMode));
            g2.fillRoundRect(x, y, size, size, 22, 22);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Yu Gothic UI", Font.BOLD, 32));
            FontMetrics fm = g2.getFontMetrics();
            String text = "あ";
            int tx = x + (size - fm.stringWidth(text)) / 2;
            int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent() - 1;
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    private class RoundedProgressBar extends JComponent {
        private int value;

        RoundedProgressBar() {
            setPreferredSize(new Dimension(320, 16));
            setMaximumSize(new Dimension(320, 16));
        }

        int getValue() {
            return value;
        }

        void setValue(int value) {
            this.value = Math.max(0, Math.min(100, value));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = h;
            g2.setColor(darkMode ? new Color(51, 65, 85) : new Color(224, 242, 254));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            int fillWidth = Math.max(h, Math.round(w * (value / 100f)));
            GradientPaint paint = new GradientPaint(0, 0, UITheme.getAccent(darkMode), w, 0, UITheme.getGlow(darkMode));
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, fillWidth, h, arc, arc);

            g2.setColor(darkMode ? new Color(255, 255, 255, 30) : new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.dispose();
        }
    }
}

