package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizQuestion;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class DoVocabQuizFrame extends JFrame implements VocabQuizFrame_Interface {
    private final AppNavigator navigator;
    private final JPanel questionNavigation = new JPanel(new GridLayout(0, 5, 6, 6));
    private final JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 12, 12));
    private final List<JButton> navigationButtons = new ArrayList<>();
    private final JLabel badgeLabel = new JLabel("VOCABULARY QUIZ");
    private final JLabel titleLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel metadataLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel progressLabel = new JLabel("0 / 0", SwingConstants.RIGHT);
    private final JLabel questionLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton nextButton = new JButton("Next");
    private final JButton checkButton = new JButton("Check Answer");
    private final JButton finishButton = new JButton("Submit & Finish");
    private VocabQuizConfig config;
    private int currentIndex;
    private int totalQuestions;
    private Color background, panel, card, border, hover, accent, title, text;
    private Consumer<String> answerHandler = answer -> { };
    private IntConsumer questionHandler = index -> { };
    private Runnable nextHandler = () -> { };
    private Runnable checkHandler = () -> { };
    private Runnable finishHandler = () -> { };

    public DoVocabQuizFrame(AppNavigator navigator, VocabQuizConfig config) {
        this.navigator = navigator;
        this.config = config;
        navigator.getAppContext().addThemeListener(isDark -> SwingUtilities.invokeLater(this::refreshTheme));
        setupFrame();
        refreshTheme();
        buildUI();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Vocabulary Quiz");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 650));
        setSize(1120, 720);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(26, 32, 26, 32));
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        badgeLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        badgeLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        badgeLabel.setOpaque(true);
        copy.add(badgeLabel);
        copy.add(Box.createVerticalStrut(10));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        copy.add(titleLabel);
        copy.add(Box.createVerticalStrut(5));
        metadataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        copy.add(metadataLabel);
        progressLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        header.add(copy, BorderLayout.WEST);
        header.add(progressLabel, BorderLayout.EAST);
        return header;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        JPanel navigationCard = roundedPanel();
        navigationCard.setLayout(new BorderLayout(0, 12));
        navigationCard.setBorder(new EmptyBorder(18, 16, 18, 16));
        JLabel navTitle = new JLabel("QUESTIONS");
        navTitle.setBackground(Color.WHITE);
        navTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        navigationCard.add(navTitle, BorderLayout.NORTH);
        questionNavigation.setOpaque(false);
        JScrollPane scroll = new JScrollPane(questionNavigation);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        navigationCard.add(scroll, BorderLayout.CENTER);
        navigationCard.setPreferredSize(new Dimension(205, 1));

        JPanel quizCard = roundedPanel();
        quizCard.setLayout(new BorderLayout(0, 18));
        quizCard.setBorder(new EmptyBorder(24, 26, 24, 26));
        JLabel prompt = new JLabel();
        prompt.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        JPanel promptPanel = new JPanel(new BorderLayout(0, 10));
        promptPanel.setOpaque(false);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 31));
        questionLabel.setVerticalAlignment(SwingConstants.CENTER);
        promptPanel.add(prompt, BorderLayout.NORTH);
        promptPanel.add(questionLabel, BorderLayout.CENTER);
        quizCard.add(promptPanel, BorderLayout.NORTH);

        optionsPanel.setOpaque(false);
        quizCard.add(optionsPanel, BorderLayout.CENTER);
        feedbackLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        quizCard.add(feedbackLabel, BorderLayout.SOUTH);
        center.add(navigationCard, BorderLayout.WEST);
        center.add(quizCard, BorderLayout.CENTER);
        return center;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JPanel studyActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        studyActions.setOpaque(false);
        configureButton(checkButton, false);
        checkButton.addActionListener(event -> checkHandler.run());
        studyActions.add(checkButton);
        JPanel completionActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        completionActions.setOpaque(false);
        configureButton(nextButton, false);
        configureButton(finishButton, true);
        nextButton.addActionListener(event -> nextHandler.run());
        finishButton.addActionListener(event -> finishHandler.run());
        completionActions.add(nextButton);
        completionActions.add(finishButton);
        footer.add(studyActions, BorderLayout.WEST);
        footer.add(completionActions, BorderLayout.EAST);
        return footer;
    }

    @Override
    public void showQuiz(VocabQuizConfig config, List<VocabQuizQuestion> questions) {
        this.config = config;
        totalQuestions = questions.size();
        titleLabel.setText(config.getQuizTypeLabel() + " Practice");
        metadataLabel.setText(config.getChapterLabel() + "  |  " + config.difficulty().getDisplayName() + " mode");
        badgeLabel.setText(config.getQuizTypeLabel().toUpperCase() + " QUIZ");
        checkButton.setVisible(config.difficulty().isAnswerRevealEnabled());
        questionNavigation.removeAll();
        navigationButtons.clear();
        for (int index = 0; index < questions.size(); index++) {
            final int questionIndex = index;
            JButton button = new JButton(String.valueOf(index + 1));
            button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setBorder(new EmptyBorder(9, 5, 9, 5));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(event -> questionHandler.accept(questionIndex));
            navigationButtons.add(button);
            questionNavigation.add(button);
        }
        questionNavigation.revalidate();
        questionNavigation.repaint();
    }

    @Override
    public void showQuestion(int index, VocabQuizQuestion question, String selectedAnswer) {
        currentIndex = index;
        progressLabel.setText("Question " + (index + 1) + " / " + totalQuestions);
        questionLabel.setText("<html><div style='text-align:center'>" + escape(question.meaning()) + "</div></html>");
        optionsPanel.removeAll();
        for (int optionIndex = 0; optionIndex < question.options().size(); optionIndex++) {
            String answer = question.options().get(optionIndex);
            OptionButton option = new OptionButton((char) ('A' + optionIndex), answer, answer.equals(selectedAnswer));
            option.addActionListener(event -> answerHandler.accept(answer));
            optionsPanel.add(option);
        }
        nextButton.setEnabled(index < totalQuestions - 1);
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    @Override
    public void updateQuestionNavigation(int currentIndex, List<Boolean> answered) {
        finishButton.setEnabled(true);
        for (int index = 0; index < navigationButtons.size(); index++) {
            JButton button = navigationButtons.get(index);
            boolean isCurrent = index == currentIndex;
            boolean isAnswered = answered.get(index);
            Color answeredColor = new Color(0x16A34A);
            button.setForeground(isCurrent || isAnswered ? Color.WHITE : title);
            button.setBackground(isCurrent ? accent : isAnswered ? answeredColor : card);
            button.setBorder(BorderFactory.createLineBorder(isCurrent ? accent : isAnswered ? answeredColor : border, 1, true));
            button.repaint();
        }
    }

    @Override
    public void showFeedback(String message, boolean correct) {
        feedbackLabel.setText(message);
        feedbackLabel.setForeground(correct ? new Color(0x16A34A) : new Color(0xDC2626));
    }

    @Override
    public void showIncompleteWarning(int remainingQuestions) {
        String noun = remainingQuestions == 1 ? "question" : "questions";
        JOptionPane.showMessageDialog(
                this,
                "You still have " + remainingQuestions + " unanswered " + noun + ".\n"
                        + "Answered questions are marked green in the Questions panel.",
                "Quiz is not finished yet",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void clearFeedback() {
        feedbackLabel.setText(" ");
    }

    @Override
    public void showUnavailable(String message) {
        questionLabel.setText(message);
        optionsPanel.removeAll();
        finishButton.setEnabled(false);
    }

    @Override public void setOnAnswerSelected(Consumer<String> action) { answerHandler = action; }
    @Override public void setOnQuestionSelected(IntConsumer action) { questionHandler = action; }
    @Override public void setOnNext(Runnable action) { nextHandler = action; }
    @Override public void setOnCheckAnswer(Runnable action) { checkHandler = action; }
    @Override public void setOnFinish(Runnable action) { finishHandler = action; }

    private void refreshTheme() {
        boolean darkMode = navigator.getAppContext().isDarkMode();
        background = UITheme.getBackground(darkMode);
        panel = UITheme.getPanelBackground(darkMode);
        card = UITheme.getCardBackground(darkMode);
        border = UITheme.getBorder(darkMode);
        hover = UITheme.getHover(darkMode);
        accent = UITheme.getAccent(darkMode);
        title = UITheme.getTitleForeground(darkMode);
        text = UITheme.getTextForeground(darkMode);
        badgeLabel.setBackground(accent);
        badgeLabel.setForeground(Color.WHITE);
        titleLabel.setForeground(title);
        metadataLabel.setForeground(text);
        progressLabel.setForeground(title);
        questionLabel.setForeground(title);
        getContentPane().setBackground(background);
        repaint();
    }

    private JPanel roundedPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setColor(card);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private void configureButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(11, 16, 11, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new QuizButtonUI(button, primary));
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setPaint(new GradientPaint(0, 0, background, 0, getHeight(), panel));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private class OptionButton extends JButton {
        OptionButton(char letter, String answer, boolean selected) {
            super("<html><b>" + letter + "</b>&nbsp;&nbsp;" + escape(answer) + "</html>");
            setFont(new Font("Yu Gothic UI", Font.BOLD, 19));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(selected ? Color.WHITE : title);
            setBackground(selected ? accent : card);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent event) { if (!selected) { setBackground(hover); repaint(); } }
                @Override public void mouseExited(MouseEvent event) { if (!selected) { setBackground(card); repaint(); } }
            });
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private class QuizButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final JButton button;
        private final boolean primary;
        QuizButtonUI(JButton button, boolean primary) { this.button = button; this.primary = primary; }
        @Override public void paint(Graphics graphics, JComponent component) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(primary ? accent : card);
            g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 15, 15);
            g2.setColor(primary ? accent : border);
            g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, 15, 15);
            g2.dispose();
            button.setForeground(primary ? Color.WHITE : title);
            super.paint(graphics, component);
        }
    }
}
