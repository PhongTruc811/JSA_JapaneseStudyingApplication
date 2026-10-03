package com.mescode.japanese.view.vocabulary.quiz;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.vocab.VocabQuizAnswerResult;
import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class VocabQuizResultFrame extends JFrame {
    private final AppNavigator navigator;
    private final VocabQuizConfig config;
    private final List<VocabQuizAnswerResult> results;

    public VocabQuizResultFrame(AppNavigator navigator, VocabQuizConfig config,
                                List<VocabQuizAnswerResult> results) {
        this.navigator = navigator;
        this.config = config;
        this.results = results;
        setupFrame();
        setupUI();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Vocabulary Quiz Result");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 600));
        setSize(980, 690);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        boolean dark = navigator.getAppContext().isDarkMode();
        Color background = UITheme.getBackground(dark);
        Color panel = UITheme.getPanelBackground(dark);
        Color card = UITheme.getCardBackground(dark);
        Color border = UITheme.getBorder(dark);
        Color title = UITheme.getTitleForeground(dark);
        Color text = UITheme.getTextForeground(dark);
        Color accent = UITheme.getAccent(dark);
        int score = (int) results.stream().filter(VocabQuizAnswerResult::isCorrect).count();
        int total = results.size();
        int percent = total == 0 ? 0 : (int) Math.round(score * 100.0 / total);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(28, 34, 28, 34));
        setContentPane(root);

        JPanel summary = new JPanel();
        summary.setBackground(panel);
        summary.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border, 1), new EmptyBorder(22, 24, 22, 24)));
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JLabel badge = new JLabel(config.getQuizTypeLabel().toUpperCase() + " QUIZ COMPLETE");
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        badge.setForeground(accent);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = new JLabel("Your result");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(title);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel scoreLabel = new JLabel(score + " / " + total + "  (" + percent + "%)");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        scoreLabel.setForeground(accent);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel meta = new JLabel(config.getChapterLabel() + "  |  " + config.difficulty().getDisplayName());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        meta.setForeground(text);
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        summary.add(badge); summary.add(Box.createVerticalStrut(8)); summary.add(heading);
        summary.add(Box.createVerticalStrut(8)); summary.add(scoreLabel); summary.add(Box.createVerticalStrut(6)); summary.add(meta);
        root.add(summary, BorderLayout.NORTH);

        JPanel review = new JPanel();
        review.setBackground(panel);
        review.setBorder(new EmptyBorder(18, 20, 18, 20));
        review.setLayout(new BoxLayout(review, BoxLayout.Y_AXIS));
        JLabel reviewHeading = new JLabel("Review incorrect answers");
        reviewHeading.setFont(new Font("Segoe UI Semibold", Font.BOLD, 17));
        reviewHeading.setForeground(title);
        reviewHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        review.add(reviewHeading); review.add(Box.createVerticalStrut(10));
        int wrongIndex = 1;
        for (VocabQuizAnswerResult result : results) {
            if (!result.isCorrect()) {
                JPanel item = new JPanel(new GridLayout(3, 1, 0, 4));
                item.setBackground(card);
                item.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border), new EmptyBorder(10, 14, 10, 14)));
                item.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel meaning = new JLabel(wrongIndex++ + ". " + result.question().meaning());
                meaning.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
                meaning.setForeground(title);
                JLabel selected = new JLabel("Your answer: " + result.selectedAnswer());
                selected.setForeground(new Color(0xDC2626));
                JLabel correct = new JLabel("Correct answer: " + result.question().correctAnswer());
                correct.setForeground(new Color(0x16A34A));
                item.add(meaning); item.add(selected); item.add(correct);
                review.add(item); review.add(Box.createVerticalStrut(8));
            }
        }
        if (wrongIndex == 1) {
            JLabel allCorrect = new JLabel("Perfect score. Every answer was correct.");
            allCorrect.setForeground(text);
            allCorrect.setAlignmentX(Component.LEFT_ALIGNMENT);
            review.add(allCorrect);
        }
        JScrollPane scroll = new JScrollPane(review);
        scroll.setBorder(BorderFactory.createLineBorder(border));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(background);
        JButton menu = button("Back to vocabulary menu", false, card, border, title, accent);
        JButton setup = button("Change setup", false, card, border, title, accent);
        JButton retry = button("Try again", true, card, border, title, accent);
        menu.addActionListener(event -> navigator.navigateTo(AppRoute.Vocab));
        setup.addActionListener(event -> navigator.navigateTo(AppRoute.VocabQuiz));
        retry.addActionListener(event -> navigator.restartVocabQuiz(config));
        actions.add(menu); actions.add(setup); actions.add(retry);
        root.add(actions, BorderLayout.SOUTH);
    }

    private JButton button(String label, boolean primary, Color card, Color border, Color title, Color accent) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setForeground(primary ? Color.WHITE : title);
        button.setBackground(primary ? accent : card);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? accent : border), new EmptyBorder(10, 14, 10, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
