package com.mescode.japanese.view.splash;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.util.theme.UITheme;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Splash screen shown on app startup.
 * Displays branded loading progress before opening the access key gate.
 */
public class SplashFrame extends JFrame {
    private static final String[] LOADING_MESSAGES = {
            "Loading Data",
            "Welcome to JSA (Japanese Study Application)",
            "Developed by Mescode (Trúc Nguyễn)",
            "Hỗ trợ cho sinh viên trường FPTU",
            "Cùng nhau học tập chăm chỉ nhé",
    };

    private final AppNavigator navigator;
    private final AppContext appContext;
    private final boolean darkMode;

    private LogoMark logo;
    private AnimatedLabel brandLabel;
    private AnimatedLabel appNameLabel;
    private AnimatedLabel metadataLabel;
    private AccentDivider loadingDivider;
    private LoadingStatusPanel loadingStatus;
    private AnimatedLabel footerLabel;
    private RoundedProgressBar progressBar;
    private Timer introTimer;
    private Timer progressTimer;
    private Timer fadeTimer;
    private Timer backgroundTimer;
    private float backgroundPhase;
    private boolean opacitySupported = true;

    public SplashFrame(AppNavigator navigator) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();
        this.darkMode = appContext.isDarkMode();

        configureFrame();
        setupUI();
        startAnimatedBackground();
        startIntro();

        setVisible(true);
    }

    private void configureFrame() {
        setTitle("JSA");
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
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(34, 42, 18, 42));
        card.setPreferredSize(new Dimension(430, 370));
        card.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        card.add(content, BorderLayout.CENTER);

        logo = new LogoMark();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(10));

        brandLabel = new AnimatedLabel("JSA");
        brandLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 34));
        brandLabel.setForeground(UITheme.getAccent(darkMode));
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandLabel.setMaximumSize(new Dimension(340, 42));
        content.add(brandLabel);
        content.add(Box.createVerticalStrut(5));

        appNameLabel = new AnimatedLabel("Japanese Study Application");
        appNameLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 17));
        appNameLabel.setForeground(UITheme.getTextForeground(darkMode));
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        appNameLabel.setMaximumSize(new Dimension(340, 26));
        content.add(appNameLabel);

        content.add(Box.createVerticalStrut(20));
        metadataLabel = new AnimatedLabel("JPD113 · JPD123");
        metadataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metadataLabel.setForeground(UITheme.getTextForeground(darkMode));
        metadataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metadataLabel.setMaximumSize(new Dimension(340, 18));
        content.add(metadataLabel);
        content.add(Box.createVerticalStrut(10));

        loadingDivider = new AccentDivider();
        loadingDivider.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(loadingDivider);
        content.add(Box.createVerticalStrut(10));

        loadingStatus = new LoadingStatusPanel();
        loadingStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(loadingStatus);
        content.add(Box.createVerticalStrut(8));

        progressBar = new RoundedProgressBar();
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(progressBar);

        footerLabel = new AnimatedLabel(appContext.appVersion +"- by MesCode (Trúc Nguyễn)");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(UITheme.getTextForeground(darkMode));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerLabel.setMaximumSize(new Dimension(320, 16));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        footer.add(footerLabel);
        card.add(footer, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        root.add(card, gbc);
    }

    private void startIntro() {
        setRevealProgress(0f, 0f, 0f, 0f, 0f);
        final long startedAt = System.nanoTime();
        introTimer = new Timer(33, null);
        introTimer.addActionListener(e -> {
            int elapsedMs = (int) ((System.nanoTime() - startedAt) / 1_000_000L);
            setRevealProgress(
                    revealProgress(elapsedMs, 0, 500),
                    revealProgress(elapsedMs, 250, 800),
                    revealProgress(elapsedMs, 450, 950),
                    revealProgress(elapsedMs, 650, 1100),
                    revealProgress(elapsedMs, 850, 1350)
            );
            if (elapsedMs >= 1350) {
                introTimer.stop();
                startProgressAndNavigate();
            }
        });
        introTimer.start();
    }

    private void setRevealProgress(float logoProgress, float brandProgress, float appNameProgress,
                                    float metadataProgress, float loadingProgress) {
        logo.setRevealProgress(logoProgress);
        brandLabel.setRevealProgress(brandProgress);
        appNameLabel.setRevealProgress(appNameProgress);
        metadataLabel.setRevealProgress(metadataProgress);
        loadingDivider.setRevealProgress(loadingProgress);
        loadingStatus.setRevealProgress(loadingProgress);
        progressBar.setRevealProgress(loadingProgress);
        footerLabel.setRevealProgress(loadingProgress);
    }

    private float revealProgress(int elapsedMs, int startMs, int endMs) {
        if (elapsedMs <= startMs) {
            return 0f;
        }
        if (elapsedMs >= endMs) {
            return 1f;
        }
        float linear = (elapsedMs - startMs) / (float) (endMs - startMs);
        return linear * linear * (3f - 2f * linear);
    }

    private void startProgressAndNavigate() {
        // thời gian chạy ProgressBar
        final int durationMs = 8000;
        final int tickMs = 50;
        final int steps = durationMs / tickMs;
        final int increment = Math.max(1, 100 / steps);

        progressTimer = new Timer(tickMs, null);
        progressTimer.addActionListener(e -> {
            int value = progressBar.getValue();
            if (value >= 100) {
                progressTimer.stop();
                progressBar.setValue(100);
                loadingStatus.setMessage("Ready to learn");
                loadingStatus.setProgress(100);
                // thời gian dừng lại delay sau khi đã load xong progress bar, rồi mới chuyển sang trang AccessKey
                Timer dismiss = new Timer(500, evt -> fadeOutAndNavigateToAccessKey()); // 260ms (0.5 giây)
                dismiss.setRepeats(false);
                dismiss.start();
                return;
            }

            value = Math.min(100, value + increment);
            progressBar.setValue(value);
            loadingStatus.setProgress(value);
            int messageIndex = Math.min(
                    LOADING_MESSAGES.length - 1,
                    value * LOADING_MESSAGES.length / 100
            );
            loadingStatus.setMessage(LOADING_MESSAGES[messageIndex]);
        });
        progressTimer.start();
    }

    private void fadeOutAndNavigateToAccessKey() {
        setAlwaysOnTop(false);
        if (!opacitySupported) {
            navigateToAccessKey();
            return;
        }
        fadeTimer = new Timer(18, null);
        fadeTimer.addActionListener(e -> {
            float nextOpacity = Math.max(0f, getOpacity() - 0.08f);
            if (!setOpacitySafely(nextOpacity)) {
                fadeTimer.stop();
                navigateToAccessKey();
                return;
            }
            if (nextOpacity <= 0f) {
                fadeTimer.stop();
                navigateToAccessKey();
            }
        });
        fadeTimer.start();
    }

    private void navigateToAccessKey() {
        this.dispose();
        navigator.navigateTo(AppRoute.AppMenu); // AccessKey
        // SwingUtilities.invokeLater(() -> navigator.navigateTo(AppRoute.AccessKey));
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

    private void startAnimatedBackground() {
        backgroundTimer = new Timer(40, e -> {
            backgroundPhase += 0.018f;
            getContentPane().repaint();
        });
        backgroundTimer.start();
    }

    private Color blend(Color first, Color second, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        int red = Math.round(first.getRed() * (1f - clamped) + second.getRed() * clamped);
        int green = Math.round(first.getGreen() * (1f - clamped) + second.getGreen() * clamped);
        int blue = Math.round(first.getBlue() * (1f - clamped) + second.getBlue() * clamped);
        return new Color(red, green, blue);
    }

    @Override
    public void dispose() {
        if (introTimer != null) {
            introTimer.stop();
        }
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        if (backgroundTimer != null) {
            backgroundTimer.stop();
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
            float wave = (float) ((Math.sin(backgroundPhase) + 1) / 2.0);
            Color top = darkMode ? new Color(0x0B1220) : new Color(0xD4E0EC);
            Color bottom = UITheme.getBackground(darkMode);
            Color mid = blend(UITheme.getGlow(darkMode), bottom, darkMode ? 0.10f : 0.08f);
            int driftY = Math.round((wave - 0.5f) * h * 0.18f);

            g2.setPaint(new GradientPaint(0, -h / 5 + driftY, top, w, h + driftY, bottom));
            g2.fillRect(0, 0, w, h);

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.16f : 0.12f));
            g2.setStroke(new BasicStroke(76f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int sweep = Math.round((backgroundPhase * 120) % Math.max(1, w + 360));
            g2.setColor(mid);
            g2.drawLine(-260 + sweep, h + 120, 280 + sweep, -120);

            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.10f : 0.08f));
            g2.setStroke(new BasicStroke(48f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int reverseSweep = Math.round((backgroundPhase * 82) % Math.max(1, w + 420));
            g2.setColor(blend(UITheme.getAccent(darkMode), bottom, darkMode ? 0.11f : 0.08f));
            g2.drawLine(w + 220 - reverseSweep, -90, w - 320 - reverseSweep, h + 110);

            g2.setComposite(AlphaComposite.SrcOver.derive(darkMode ? 0.07f : 0.05f));
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(darkMode ? new Color(255, 255, 255, 120) : new Color(14, 165, 233, 120));
            int lineOffset = Math.round((backgroundPhase * 18) % 44);
            for (int x = -80 + lineOffset; x < w + 120; x += 44) {
                g2.drawLine(x, 0, x - 160, h);
            }
            g2.setComposite(originalComposite);
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

    private class AccentDivider extends JComponent {
        private float revealProgress;

        AccentDivider() {
            setPreferredSize(new Dimension(320, 12));
            setMaximumSize(new Dimension(320, 12));
        }

        void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offset = Math.round((1f - revealProgress) * 8f);
            g2.translate(0, offset);
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));

            int centerY = getHeight() / 2;
            int centerX = getWidth() / 2;
            g2.setColor(darkMode ? new Color(255, 255, 255, 34) : new Color(15, 23, 42, 26));
            g2.drawLine(0, centerY, centerX - 12, centerY);
            g2.drawLine(centerX + 12, centerY, getWidth(), centerY);
            g2.setColor(UITheme.getAccent(darkMode));
            g2.fillOval(centerX - 4, centerY - 4, 8, 8);
            g2.dispose();
        }
    }

    private class LoadingStatusPanel extends JPanel {
        private final JLabel message = new JLabel(LOADING_MESSAGES[0], SwingConstants.LEFT);
        private final JLabel percentage = new JLabel("0%", SwingConstants.RIGHT);
        private float revealProgress;

        LoadingStatusPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(320, 18));
            setMaximumSize(new Dimension(320, 18));

            message.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            message.setForeground(UITheme.getTextForeground(darkMode));
            percentage.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            percentage.setForeground(UITheme.getAccent(darkMode));
            add(message, BorderLayout.CENTER);
            add(percentage, BorderLayout.EAST);
        }

        void setMessage(String text) {
            message.setText(text);
        }

        void setProgress(int value) {
            percentage.setText(Math.max(0, Math.min(100, value)) + "%");
        }

        void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int offset = Math.round((1f - revealProgress) * 8f);
            g2.translate(0, offset);
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));
            super.paint(g2);
            g2.dispose();
        }
    }

    private class AnimatedLabel extends JLabel {
        private float revealProgress;

        AnimatedLabel(String text) {
            super(text, SwingConstants.CENTER);
        }

        void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int offset = Math.round((1f - revealProgress) * 8f);
            g2.translate(0, offset);
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private class LogoMark extends JComponent {
        private float revealProgress;

        LogoMark() {
            setPreferredSize(new Dimension(78, 78));
            setMaximumSize(new Dimension(78, 78));
        }

        void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offset = Math.round((1f - revealProgress) * 8f);
            g2.translate(0, offset);
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));

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
        private float revealProgress;

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

        void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offset = Math.round((1f - revealProgress) * 8f);
            g2.translate(0, offset);
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));

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

