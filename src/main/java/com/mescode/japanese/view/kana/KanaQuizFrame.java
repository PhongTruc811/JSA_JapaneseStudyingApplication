package com.mescode.japanese.view.kana;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaQuizCountdown;
import com.mescode.japanese.model.kana.KanaQuizDifficulty;
import com.mescode.japanese.model.kana.KanaQuizOptions;
import com.mescode.japanese.model.kana.KanaQuizSessionStats;
import com.mescode.japanese.model.kana.KanaQuizGroup;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class KanaQuizFrame extends JFrame implements KanaQuizFrame_Interface {
    private final AppNavigator navigator;
    private final AppRoute quizOption;
    private final KanaQuizOptions options;
    private final KanaQuizDifficulty difficulty;
    private final AppContext appContext;

    private final JLabel typeLabel = new JLabel();
    private final JLabel kanaLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel difficultyLabel = new JLabel();
    private final JLabel streakLabel = new JLabel("Streak: 0");
    private final JLabel timerLabel = new JLabel();
    private final JLabel correctAnswerLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JTextField inputField = new JTextField();
    private final JButton correctButton = new JButton("Correct answer");
    private final JButton endButton = new JButton("End quiz");
    private final JPanel rootPanel = new JPanel(new GridBagLayout());
    private final JPanel quizCard = new RoundedPanel();
    private final JPanel characterPanel = new RoundedPanel();
    private final JPanel inputPanel = new JPanel(new BorderLayout(0, 8));
    private final JPanel feedbackPanel = new JPanel();

    private boolean darkMode;
    private int currentScore;
    private KanaQuizCountdown countdown;
    private Timer countdownTimer;
    private boolean quizStarted;
    private boolean quizFinished;
    private KanaQuizSessionStats.Snapshot stats = KanaQuizSessionStats.Snapshot.empty();
    private Color background, panel, card, border, title, text, accent;

    public KanaQuizFrame(AppNavigator navigator) {
        this(navigator, AppRoute.HiraQuiz, KanaQuizOptions.easyAll());
    }

    public KanaQuizFrame(AppNavigator navigator, AppRoute quizOption) {
        this(navigator, quizOption, KanaQuizOptions.easyAll());
    }

    public KanaQuizFrame(AppNavigator navigator, AppRoute quizOption, KanaQuizDifficulty difficulty) {
        this(navigator, quizOption, new KanaQuizOptions(
                difficulty == null ? KanaQuizDifficulty.EASY : difficulty,
                KanaQuizGroup.ALL));
    }

    public KanaQuizFrame(AppNavigator navigator, AppRoute quizOption, KanaQuizOptions options) {
        this.navigator = navigator;
        this.quizOption = quizOption;
        this.options = options == null ? KanaQuizOptions.easyAll() : options;
        this.difficulty = this.options.difficulty();
        this.appContext = navigator.getAppContext();
        this.countdown = new KanaQuizCountdown(this.difficulty.getDurationSeconds());
        configureTheme(appContext.isDarkMode());
        setupFrame();
        setupUI();
        applyTheme();
        appContext.addThemeListener(dark -> SwingUtilities.invokeLater(() -> {
            configureTheme(dark);
            applyTheme();
        }));
        setVisible(true);
        SwingUtilities.invokeLater(inputField::requestFocusInWindow);
    }

    private void setupFrame() {
        setTitle(quizOption == AppRoute.KataQuiz ? "Katakana Quiz" : "Hiragana Quiz");
        setSize(760, 760);
        setMinimumSize(new Dimension(680, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void configureTheme(boolean dark) {
        darkMode = dark;
        background = UITheme.getBackground(dark);
        panel = UITheme.getPanelBackground(dark);
        card = UITheme.getCardBackground(dark);
        border = UITheme.getBorder(dark);
        title = UITheme.getTitleForeground(dark);
        text = UITheme.getTextForeground(dark);
        accent = UITheme.getAccent(dark);
    }

    private void setupUI() {
        rootPanel.setBorder(new EmptyBorder(28, 32, 28, 32));
        setContentPane(rootPanel);

        quizCard.setLayout(new BoxLayout(quizCard, BoxLayout.Y_AXIS));
        quizCard.setBorder(new EmptyBorder(26, 32, 28, 32));
        quizCard.setPreferredSize(new Dimension(590, 610));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        String script = quizOption == AppRoute.KataQuiz ? "KATAKANA" : "HIRAGANA";
        typeLabel.setText(script);
        typeLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        typeLabel.setOpaque(true);
        typeLabel.setBorder(new EmptyBorder(7, 12, 7, 12));
        scoreLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        difficultyLabel.setText(difficulty.getDisplayName().toUpperCase());
        difficultyLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        streakLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        timerLabel.setText(countdown.getFormattedTime());
        timerLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));

        JPanel status = new JPanel();
        status.setOpaque(false);
        status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
        status.add(difficultyLabel);
        status.add(Box.createHorizontalStrut(10));
        status.add(scoreLabel);
        status.add(Box.createHorizontalStrut(10));
        status.add(streakLabel);
        status.add(Box.createHorizontalStrut(10));
        status.add(timerLabel);
        header.add(typeLabel, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        quizCard.add(header);
        quizCard.add(Box.createVerticalStrut(18));

        JLabel instruction = new JLabel("Type the romaji for this character", SwingConstants.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        instruction.setName("instruction");
        quizCard.add(instruction);
        quizCard.add(Box.createVerticalStrut(12));

        characterPanel.setLayout(new BorderLayout());
        characterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        characterPanel.setPreferredSize(new Dimension(1, 190));
        kanaLabel.setFont(new Font("Yu Gothic UI", Font.BOLD, 112));
        characterPanel.add(kanaLabel, BorderLayout.CENTER);
        quizCard.add(characterPanel);
        quizCard.add(Box.createVerticalStrut(18));

        JLabel inputLabel = new JLabel("Your answer");
        inputLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        inputField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setBorder(new EmptyBorder(12, 14, 12, 14));
        inputField.setToolTipText("Enter romaji, then press Enter");
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);
        quizCard.add(inputPanel);
        quizCard.add(Box.createVerticalStrut(12));

        feedbackPanel.setOpaque(false);
        feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));
        feedbackPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        resultLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        correctAnswerLabel.setFont(new Font("Yu Gothic UI", Font.PLAIN, 14));
        correctAnswerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        feedbackPanel.add(resultLabel);
        feedbackPanel.add(Box.createVerticalStrut(5));
        feedbackPanel.add(correctAnswerLabel);
        quizCard.add(feedbackPanel);
        quizCard.add(Box.createVerticalGlue());

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        styleButton(correctButton, false);
        styleButton(endButton, true);
        correctButton.setEnabled(difficulty.isAnswerRevealEnabled());
        correctButton.setToolTipText(difficulty.isAnswerRevealEnabled()
                ? "Reveal the current kana answer"
                : "Answer reveal is unavailable in Hard mode");
        endButton.addActionListener(e -> finishQuiz());
        actions.add(correctButton);
        actions.add(endButton);
        quizCard.add(actions);

        rootPanel.add(quizCard);
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.putClientProperty("primary", primary);
    }

    private void applyTheme() {
        rootPanel.setBackground(background);
        quizCard.setBackground(panel);
        characterPanel.setBackground(card);
        typeLabel.setBackground(accent);
        typeLabel.setForeground(Color.WHITE);
        scoreLabel.setForeground(title);
        kanaLabel.setForeground(title);
        inputField.setBackground(darkMode ? new Color(0x111827) : Color.WHITE);
        inputField.setForeground(title);
        inputField.setCaretColor(accent);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        resultLabel.setForeground(darkMode ? new Color(0xFB7185) : new Color(0xBE123C));
        correctAnswerLabel.setForeground(text);
        colorLabels(quizCard);
        difficultyLabel.setForeground(accent);
        updateStreakDisplay();
        updateTimerDisplay();

        correctButton.setBackground(card);
        correctButton.setForeground(title);
        correctButton.setBorder(BorderFactory.createLineBorder(border, 1, true));
        endButton.setBackground(accent);
        endButton.setForeground(Color.WHITE);
        endButton.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        revalidate();
        repaint();
    }

    private void startCountdownIfNeeded() {
        if (quizStarted || quizFinished) {
            return;
        }
        quizStarted = true;
        updateTimerDisplay();
        countdownTimer = new Timer(1000, e -> {
            KanaQuizCountdown.TickResult tick = countdown.tick();
            updateTimerDisplay();
            if (tick.expiredNow()) {
                finishQuiz();
            }
        });
        countdownTimer.setInitialDelay(1000);
        countdownTimer.start();
    }

    private void updateTimerDisplay() {
        timerLabel.setText(countdown.getFormattedTime());
        timerLabel.setForeground(countdown.isWarningTime()
                ? (darkMode ? new Color(0xFB7185) : new Color(0xBE123C))
                : title);
    }

    private void updateStreakDisplay() {
        streakLabel.setText("Streak: " + stats.currentStreak());
        streakLabel.setForeground(stats.currentStreak() > 0 ? accent : text);
    }

    private void finishQuiz() {
        if (quizFinished) {
            return;
        }
        quizFinished = true;
        stopCountdown();
        inputField.setEnabled(false);
        correctButton.setEnabled(false);
        endButton.setEnabled(false);
        showEndQuizDialog();
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void colorLabels(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && label != typeLabel
                    && label != resultLabel && label != correctAnswerLabel) {
                label.setForeground(label == scoreLabel || label == kanaLabel ? title : text);
            }
            if (component instanceof Container child) {
                colorLabels(child);
            }
        }
    }

    private void showEndQuizDialog() {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        ResultCard content = new ResultCard();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(24, 30, 28, 30));
        dialog.setContentPane(content);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel badge = new JLabel("QUIZ RESULT");
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        badge.setForeground(accent);
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        closeButton.setForeground(text);
        closeButton.setBorder(null);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> returnToKanaMenu(dialog));
        header.add(badge, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        JPanel summary = new JPanel();
        summary.setOpaque(false);
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        String headingText = countdown.getRemainingSeconds() == 0 ? "Time's up!" : "Quiz completed :3";
        JLabel heading = dialogLabel(headingText, 26, Font.BOLD, title);
        JLabel score = dialogLabel(String.valueOf(currentScore), 58, Font.BOLD, accent);
        JLabel points = dialogLabel("POINTS", 12, Font.BOLD, text);
        JLabel encouragement = dialogLabel(scoreMessage(), 15, Font.PLAIN, text);
        summary.add(Box.createVerticalStrut(8));
        summary.add(heading);
        summary.add(Box.createVerticalStrut(16));
        summary.add(score);
        summary.add(points);
        summary.add(Box.createVerticalStrut(13));
        summary.add(encouragement);
        summary.add(Box.createVerticalStrut(18));
        summary.add(buildStatsSummary());
        content.add(summary, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        DialogButton retryButton = new DialogButton("Try again", true);
        DialogButton menuButton = new DialogButton("Menu", false);
        retryButton.addActionListener(e -> {
            dialog.dispose();
            navigator.restartKanaQuiz(quizOption, options);
        });
        menuButton.addActionListener(e -> returnToKanaMenu(dialog));
        actions.add(retryButton);
        actions.add(menuButton);
        content.add(actions, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(retryButton);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        dialog.getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                returnToKanaMenu(dialog);
            }
        });
        dialog.setSize(590, 470);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JComponent buildStatsSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(510, 70));
        panel.setPreferredSize(new Dimension(510, 70));
        panel.add(new MetricCard("Correct", stats.correctAnswers(), new Color(0x22C55E)));
        panel.add(new MetricCard("Wrong", stats.wrongAnswers(), new Color(0xEF4444)));
        panel.add(new MetricCard("Revealed", stats.answerReveals(), accent));
        panel.add(new MetricCard("Best streak", stats.bestStreak(), new Color(0xF59E0B)));
        return panel;
    }

    private void returnToKanaMenu(JDialog dialog) {
        dialog.dispose();
        navigator.navigateTo(AppRoute.Kana);
    }

    private JLabel dialogLabel(String value, int size, int style, Color color) {
        JLabel label = new JLabel(value, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Hàm xử lý logic thong bao message dựa trên score
    private String scoreMessage() {
        if (currentScore >= 10) {
            return "Excellent work — keep the momentum going!";
        }
        if (currentScore > 0) {
            return "Nice progress — another round will make it stick.";
        }
        return "Keep practicing — every attempt builds confidence.";
    }

    @Override public void showKana(Kana kana) {
        kanaLabel.setText(kana == null ? "?" : kana.getKana());
        startCountdownIfNeeded();
    }
    @Override public void showResult(String result) { resultLabel.setText(result); }
    @Override public void resetResult() { resultLabel.setText(" "); }
    @Override public void updateScore(int score) {
        currentScore = score;
        scoreLabel.setText("Score: " + score);
    }
    @Override public void updateStats(KanaQuizSessionStats.Snapshot stats) {
        this.stats = stats == null ? KanaQuizSessionStats.Snapshot.empty() : stats;
        updateStreakDisplay();
    }
    @Override public String getUserInput() { return inputField.getText().trim().toLowerCase(); }
    @Override public void resetInput() {
        inputField.setText("");
        inputField.requestFocusInWindow();
    }
    @Override public void showCorrectAnswer(String answer) { correctAnswerLabel.setText(answer); }
    @Override public void resetCorrectAnswer() { correctAnswerLabel.setText(" "); }
    @Override public void setOnSubmit(Runnable action) { inputField.addActionListener(e -> action.run()); }
    @Override public void setOnShowAnswer(Runnable action) { correctButton.addActionListener(e -> action.run()); }

    @Override public void dispose() {
        stopCountdown();
        super.dispose();
    }

    private class RoundedPanel extends JPanel {
        RoundedPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = this == characterPanel ? card : panel;
            if (this == quizCard) {
                g2.setColor(new Color(15, 23, 42, darkMode ? 45 : 25));
                g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 16, 28, 28);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 26, 26);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 11, getHeight() - 11, 26, 26);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ResultCard extends JPanel {
        ResultCard() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(15, 23, 42, darkMode ? 95 : 55));
            g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 16, 28, 28);
            g2.setColor(panel);
            g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 28, 28);
            g2.setColor(darkMode ? new Color(255, 255, 255, 22) : border);
            g2.drawRoundRect(0, 0, getWidth() - 11, getHeight() - 11, 28, 28);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class MetricCard extends JPanel {
        private final Color metricColor;

        MetricCard(String name, int value, Color metricColor) {
            this.metricColor = metricColor;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(9, 8, 9, 8));

            JLabel valueLabel = dialogLabel(String.valueOf(value), 22, Font.BOLD, metricColor);
            JLabel nameLabel = dialogLabel(name, 11, Font.PLAIN, text);
            add(valueLabel);
            add(Box.createVerticalStrut(2));
            add(nameLabel);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(card);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(new Color(metricColor.getRed(), metricColor.getGreen(), metricColor.getBlue(), 150));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class DialogButton extends JButton {
        private final boolean primary;

        DialogButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
            setForeground(primary ? Color.WHITE : title);
            setBorder(new EmptyBorder(12, 18, 12, 18));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = primary ? accent : card;
            if (getModel().isPressed()) {
                fill = UITheme.getPressed(darkMode);
            } else if (getModel().isRollover()) {
                fill = primary ? UITheme.getPressed(darkMode) : UITheme.getHover(darkMode);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(primary ? accent : border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
