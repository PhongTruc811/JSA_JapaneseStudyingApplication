package com.mescode.japanese.view.petrial;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.model.petrial.PeTrialCountdown;
import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.model.petrial.PeTrialQuestion;
import com.mescode.japanese.view.theme.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class PeTrialQuizFrame extends JFrame implements PeTrialQuizView {
    private final AppNavigator navigator;
    private final PeTrialConfig difficulty;
    private final JPanel questionNavigation = new JPanel(new GridLayout(0, 5, 7, 7));
    private final JPanel answerPanel = new JPanel(new GridLayout(1, 4, 12, 0));
    private final List<JButton> navigationButtons = new ArrayList<>();
    private final Map<String, JButton> answerButtons = new LinkedHashMap<>();
    private final QuestionImagePanel imagePanel = new QuestionImagePanel();
    private final JLabel titleLabel = new JLabel("PE Trial Spring 2026");
    private final JLabel metadataLabel = new JLabel();
    private final JLabel progressLabel = new JLabel("Question 0 / 30");
    private final JLabel timerLabel = new JLabel();
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton previousButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JButton checkButton = new JButton("Check Answer");
    private final JButton submitButton = new JButton("Submit");

    private Consumer<String> answerHandler = answer -> { };
    private IntConsumer questionHandler = index -> { };
    private Runnable previousHandler = () -> { };
    private Runnable nextHandler = () -> { };
    private Runnable checkHandler = () -> { };
    private Runnable submitHandler = () -> { };
    private Runnable timeExpiredHandler = () -> { };

    private PeTrialCountdown countdown;
    private Timer countdownTimer;
    private int currentIndex;
    private int totalQuestions;
    private String selectedAnswer;
    private List<Boolean> answeredState = List.of();
    private boolean darkMode;
    private Color background;
    private Color panel;
    private Color card;
    private Color border;
    private Color accent;
    private Color title;
    private Color text;

    public PeTrialQuizFrame(AppNavigator navigator, PeTrialConfig difficulty) {
        this.navigator = navigator;
        this.difficulty = difficulty;
        setupFrame();
        setupUI();
        refreshTheme();
        navigator.getAppContext().addThemeListener(dark -> SwingUtilities.invokeLater(this::refreshTheme));
    }

    private void setupFrame() {
        setTitle("PE Trial Spring 2026");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(22, 26, 22, 26));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildQuestionNavigation(), BorderLayout.WEST);
        root.add(buildQuestionCard(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        metadataLabel.setText(difficulty.getDisplayName() + " mode"
                + (difficulty.isTimed() ? "  |  40-minute exam" : "  |  Practice without a time limit"));
        metadataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labels.add(titleLabel);
        labels.add(Box.createVerticalStrut(4));
        labels.add(metadataLabel);

        JPanel status = new JPanel();
        status.setOpaque(false);
        status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
        progressLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timerLabel.setVisible(difficulty.isTimed());
        timerLabel.setOpaque(true);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC2410C)),
                new EmptyBorder(7, 12, 7, 12)
        ));
        status.add(progressLabel);
        if (difficulty.isTimed()) {
            status.add(Box.createHorizontalStrut(18));
            status.add(timerLabel);
        }

        header.add(labels, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JComponent buildQuestionNavigation() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBorder(new EmptyBorder(14, 14, 14, 14));
        wrapper.setPreferredSize(new Dimension(205, 0));

        JLabel heading = new JLabel("Questions");
        heading.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        questionNavigation.setOpaque(false);

        JPanel topAligned = new JPanel(new BorderLayout());
        topAligned.setOpaque(false);
        topAligned.add(questionNavigation, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(topAligned);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(heading, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.putClientProperty("heading", heading);
        return wrapper;
    }

    private JComponent buildQuestionCard() {
        JPanel questionCard = new JPanel(new BorderLayout(0, 14));
        questionCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        imagePanel.setPreferredSize(new Dimension(800, 360));

        for (String option : List.of("A", "B", "C", "D")) {
            JButton button = new JButton(option);
            button.setFont(new Font("Segoe UI", Font.BOLD, 20));
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(event -> answerHandler.accept(option));
            answerButtons.put(option, button);
            answerPanel.add(button);
        }
        answerPanel.setOpaque(false);

        questionCard.add(imagePanel, BorderLayout.CENTER);
        questionCard.add(answerPanel, BorderLayout.SOUTH);
        return questionCard;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);

        JPanel feedback = new JPanel(new BorderLayout());
        feedback.setOpaque(false);
        feedbackLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        feedback.add(feedbackLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        configureActionButton(previousButton, false);
        configureActionButton(nextButton, false);
        configureActionButton(checkButton, false);
        configureActionButton(submitButton, true);
        previousButton.addActionListener(event -> previousHandler.run());
        nextButton.addActionListener(event -> nextHandler.run());
        checkButton.addActionListener(event -> checkHandler.run());
        submitButton.addActionListener(event -> submitHandler.run());
        checkButton.setVisible(difficulty.isAnswerCheckEnabled());
        actions.add(previousButton);
        actions.add(nextButton);
        actions.add(checkButton);
        actions.add(submitButton);

        footer.add(feedback, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void configureActionButton(JButton button, boolean primary) {
        button.putClientProperty("primary", primary);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    @Override
    public void showQuiz(PeTrialConfig difficulty, List<PeTrialQuestion> questions) {
        totalQuestions = questions.size();
        questionNavigation.removeAll();
        navigationButtons.clear();
        for (int index = 0; index < questions.size(); index++) {
            int questionIndex = index;
            JButton button = new JButton(String.valueOf(index + 1));
            button.setFocusPainted(false);
            button.setMargin(new Insets(7, 4, 7, 4));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(event -> questionHandler.accept(questionIndex));
            navigationButtons.add(button);
            questionNavigation.add(button);
        }
        questionNavigation.revalidate();
        questionNavigation.repaint();
        if (difficulty.isTimed()) {
            startCountdown();
        }
    }

    @Override
    public void showQuestion(int index, PeTrialQuestion question, String selectedAnswer) {
        currentIndex = index;
        this.selectedAnswer = selectedAnswer;
        progressLabel.setText("Question " + (index + 1) + " / " + totalQuestions);
        previousButton.setEnabled(index > 0);
        nextButton.setEnabled(index < totalQuestions - 1);
        try {
            imagePanel.setImage(question.image());
        } catch (Exception exception) {
            imagePanel.showMessage("Unable to display " + question.id() + ".");
            answerButtons.values().forEach(button -> button.setEnabled(false));
            return;
        }
        styleAnswerButtons(this.selectedAnswer);
    }

    @Override
    public void updateQuestionNavigation(int currentIndex, List<Boolean> answered) {
        answeredState = List.copyOf(answered);
        for (int index = 0; index < navigationButtons.size(); index++) {
            JButton button = navigationButtons.get(index);
            boolean current = index == currentIndex;
            boolean completed = answered.get(index);
            Color base = completed ? new Color(0x16A34A) : card;
            button.setBackground(current ? accent : base);
            button.setForeground(current || completed ? Color.WHITE : title);
            button.setBorder(BorderFactory.createLineBorder(current ? accent : border, current ? 2 : 1));
        }
    }

    @Override
    public void showFeedback(String message, boolean correct) {
        feedbackLabel.setText(message);
        feedbackLabel.setForeground(correct ? new Color(0x16A34A) : new Color(0xDC2626));
    }

    @Override
    public void clearFeedback() {
        feedbackLabel.setText(" ");
    }

    @Override
    public void showUnavailable(String message) {
        imagePanel.showMessage(message);
        answerButtons.values().forEach(button -> button.setEnabled(false));
        submitButton.setEnabled(false);
    }

    @Override
    public boolean confirmSubmit(int unansweredQuestions) {
        String message = unansweredQuestions == 0
                ? "Submit your PE Trial now?"
                : "You still have " + unansweredQuestions + " unanswered question"
                + (unansweredQuestions == 1 ? "" : "s")
                + ".\nUnanswered questions will be counted as incorrect.\n\nSubmit anyway?";
        return JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm submission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private void startCountdown() {
        stopCountdown();
        countdown = new PeTrialCountdown(difficulty.getDurationSeconds());
        updateTimerDisplay();
        countdownTimer = new Timer(1000, event -> {
            PeTrialCountdown.TickResult tick = countdown.tick();
            updateTimerDisplay();
            if (tick.expiredNow()) {
                stopCountdown();
                timeExpiredHandler.run();
            }
        });
        countdownTimer.setInitialDelay(1000);
        countdownTimer.start();
    }

    private void updateTimerDisplay() {
        if (countdown == null) {
            return;
        }
        boolean warning = countdown.isWarningTime();
        Color timerBackground = warning ? new Color(0xDC2626) : new Color(0xF97316);
        Color timerBorder = warning ? new Color(0x991B1B) : new Color(0xC2410C);
        timerLabel.setText("TIME  " + countdown.getFormattedTime());
        timerLabel.setBackground(timerBackground);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(timerBorder),
                new EmptyBorder(7, 12, 7, 12)
        ));
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void styleAnswerButtons(String selectedAnswer) {
        for (Map.Entry<String, JButton> entry : answerButtons.entrySet()) {
            boolean selected = entry.getKey().equals(selectedAnswer);
            JButton button = entry.getValue();
            button.setEnabled(true);
            button.setBackground(selected ? accent : card);
            button.setForeground(selected ? Color.WHITE : title);
            button.setBorder(BorderFactory.createLineBorder(selected ? accent : border, selected ? 2 : 1));
        }
    }

    private void refreshTheme() {
        darkMode = navigator.getAppContext().isDarkMode();
        background = UITheme.getBackground(darkMode);
        panel = UITheme.getPanelBackground(darkMode);
        card = UITheme.getCardBackground(darkMode);
        border = UITheme.getBorder(darkMode);
        accent = UITheme.getAccent(darkMode);
        title = UITheme.getTitleForeground(darkMode);
        text = UITheme.getTextForeground(darkMode);

        getContentPane().setBackground(background);
        titleLabel.setForeground(title);
        metadataLabel.setForeground(text);
        progressLabel.setForeground(title);
        feedbackLabel.setForeground(text);
        recolorContainers(getContentPane());
        imagePanel.applyTheme(card, border, text);
        styleAnswerButtons(selectedAnswer);
        applyActionTheme(previousButton);
        applyActionTheme(nextButton);
        applyActionTheme(checkButton);
        applyActionTheme(submitButton);
        if (!answeredState.isEmpty()) {
            updateQuestionNavigation(currentIndex, answeredState);
        }
        updateTimerDisplay();
        repaint();
    }

    private void recolorContainers(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel child) {
                child.setBackground(child.isOpaque() ? panel : background);
                Object heading = child.getClientProperty("heading");
                if (heading instanceof JLabel label) {
                    label.setForeground(title);
                }
                recolorContainers(child);
            } else if (component instanceof JScrollPane scrollPane) {
                scrollPane.getViewport().setBackground(panel);
            }
        }
    }

    private void applyActionTheme(JButton button) {
        boolean primary = Boolean.TRUE.equals(button.getClientProperty("primary"));
        button.setBackground(primary ? accent : card);
        button.setForeground(primary ? Color.WHITE : title);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? accent : border),
                new EmptyBorder(10, 15, 10, 15)
        ));
    }

    @Override
    public void dispose() {
        stopCountdown();
        super.dispose();
    }

    @Override public void setOnAnswerSelected(Consumer<String> action) { answerHandler = action; }
    @Override public void setOnQuestionSelected(IntConsumer action) { questionHandler = action; }
    @Override public void setOnPrevious(Runnable action) { previousHandler = action; }
    @Override public void setOnNext(Runnable action) { nextHandler = action; }
    @Override public void setOnCheckAnswer(Runnable action) { checkHandler = action; }
    @Override public void setOnSubmit(Runnable action) { submitHandler = action; }
    @Override public void setOnTimeExpired(Runnable action) { timeExpiredHandler = action; }

    private static final class QuestionImagePanel extends JPanel {
        private BufferedImage image;
        private String message;
        private Color borderColor = Color.GRAY;
        private Color textColor = Color.DARK_GRAY;

        private QuestionImagePanel() {
            setOpaque(true);
        }

        private void setImage(String resourcePath) throws Exception {
            URL resource = PeTrialQuizFrame.class.getResource(resourcePath);
            if (resource == null) {
                throw new IllegalStateException("Missing image: " + resourcePath);
            }
            image = ImageIO.read(resource);
            if (image == null) {
                throw new IllegalStateException("Unsupported image: " + resourcePath);
            }
            message = null;
            repaint();
        }

        private void showMessage(String value) {
            image = null;
            message = value;
            repaint();
        }

        private void applyTheme(Color background, Color border, Color text) {
            setBackground(background);
            borderColor = border;
            textColor = text;
            setBorder(BorderFactory.createLineBorder(border));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D copy = (Graphics2D) graphics.create();
            try {
                copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                copy.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (image != null) {
                    int availableWidth = Math.max(1, getWidth() - 20);
                    int availableHeight = Math.max(1, getHeight() - 20);
                    double scale = Math.min(availableWidth / (double) image.getWidth(),
                            availableHeight / (double) image.getHeight());
                    int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
                    int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
                    int x = (getWidth() - width) / 2;
                    int y = (getHeight() - height) / 2;
                    copy.drawImage(image, x, y, width, height, null);
                } else if (message != null) {
                    copy.setColor(textColor);
                    copy.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    FontMetrics metrics = copy.getFontMetrics();
                    copy.drawString(message, Math.max(12, (getWidth() - metrics.stringWidth(message)) / 2),
                            getHeight() / 2);
                }
            } finally {
                copy.dispose();
            }
        }
    }
}
