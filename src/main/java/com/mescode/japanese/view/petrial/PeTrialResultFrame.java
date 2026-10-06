package com.mescode.japanese.view.petrial;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.petrial.PeTrialAnswerResult;
import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.util.theme.UITheme;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

public class PeTrialResultFrame extends JFrame {
    private final AppNavigator navigator;
    private final PeTrialConfig difficulty;
    private final List<PeTrialAnswerResult> results;
    private final boolean timeExpired;

    public PeTrialResultFrame(AppNavigator navigator, PeTrialConfig difficulty,
                              List<PeTrialAnswerResult> results, boolean timeExpired) {
        this.navigator = navigator;
        this.difficulty = difficulty;
        this.results = results;
        this.timeExpired = timeExpired;
        setupFrame();
        setupUI();
    }

    private void setupFrame() {
        setTitle("PE Trial Result");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 600));
        setSize(1000, 720);
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

        int correctCount = (int) results.stream().filter(PeTrialAnswerResult::isCorrect).count();
        int unansweredCount = (int) results.stream().filter(result -> !result.isAnswered()).count();
        int incorrectCount = results.size() - correctCount - unansweredCount;
        int percent = results.isEmpty() ? 0 : (int) Math.round(correctCount * 100.0 / results.size());

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(26, 32, 26, 32));
        setContentPane(root);

        JPanel summary = new JPanel();
        summary.setBackground(panel);
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(20, 24, 20, 24)
        ));
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JLabel badge = label(timeExpired ? "TIME'S UP" : "PE TRIAL COMPLETE", 12, Font.BOLD, accent);
        JLabel heading = label("Your result", 28, Font.BOLD, title);
        JLabel score = label(correctCount + " / " + results.size() + "  (" + percent + "%)", 34, Font.BOLD, accent);
        JLabel meta = label(difficulty.getDisplayName() + " mode  |  "
                + correctCount + " correct  |  " + incorrectCount + " incorrect  |  "
                + unansweredCount + " unanswered", 14, Font.PLAIN, text);
        summary.add(badge);
        summary.add(Box.createVerticalStrut(7));
        summary.add(heading);
        summary.add(Box.createVerticalStrut(8));
        summary.add(score);
        summary.add(Box.createVerticalStrut(6));
        summary.add(meta);
        root.add(summary, BorderLayout.NORTH);

        JPanel review = new JPanel();
        review.setBackground(panel);
        review.setBorder(new EmptyBorder(18, 20, 18, 20));
        review.setLayout(new BoxLayout(review, BoxLayout.Y_AXIS));
        JLabel reviewHeading = new JLabel("Review incorrect and unanswered questions");
        reviewHeading.setFont(new Font("Segoe UI Semibold", Font.BOLD, 17));
        reviewHeading.setForeground(title);
        reviewHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        review.add(reviewHeading);
        review.add(Box.createVerticalStrut(10));

        int reviewItems = 0;
        for (int index = 0; index < results.size(); index++) {
            PeTrialAnswerResult result = results.get(index);
            if (result.isCorrect()) {
                continue;
            }
            reviewItems++;
            JPanel item = buildReviewItem(index + 1, result, card, border, title, text, accent);
            review.add(item);
            review.add(Box.createVerticalStrut(12));
        }
        if (reviewItems == 0) {
            JLabel perfect = new JLabel("Perfect score. Every answer was correct.");
            perfect.setForeground(text);
            perfect.setAlignmentX(Component.LEFT_ALIGNMENT);
            review.add(perfect);
        }

        JScrollPane scrollPane = new JScrollPane(review);
        scrollPane.setBorder(BorderFactory.createLineBorder(border));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(background);
        JButton menu = button("Back to main menu", false, card, border, title, accent);
        JButton changeMode = button("Change mode", false, card, border, title, accent);
        JButton retry = button("Try again", true, card, border, title, accent);
        menu.addActionListener(event -> navigator.navigateTo(AppRoute.AppMenu));
        changeMode.addActionListener(event -> navigator.navigateTo(AppRoute.PETrialSP26));
        retry.addActionListener(event -> navigator.restartPeTrialQuiz(difficulty));
        actions.add(menu);
        actions.add(changeMode);
        actions.add(retry);
        root.add(actions, BorderLayout.SOUTH);
    }

    private JPanel buildReviewItem(int questionNumber, PeTrialAnswerResult result,
                                   Color card, Color border, Color title, Color text, Color accent) {
        JPanel item = new JPanel(new BorderLayout(0, 10));
        item.setBackground(card);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(12, 14, 12, 14)
        ));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 470));
        item.setPreferredSize(new Dimension(900, 440));

        JLabel question = new JLabel("Question " + questionNumber);
        question.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        question.setForeground(title);
        item.add(question, BorderLayout.NORTH);

        ReviewImagePanel imagePanel = new ReviewImagePanel(
                result.question().image(), card, border, text
        );
        item.add(imagePanel, BorderLayout.CENTER);

        JPanel answerSummary = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        answerSummary.setOpaque(false);

        String selectedValue = result.isAnswered() ? result.selectedAnswer() : "Not answered";
        JLabel selectedAnswer = new JLabel("Your answer: " + selectedValue);
        selectedAnswer.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        selectedAnswer.setForeground(result.isAnswered() ? new Color(0xDC2626) : accent);

        JLabel separator = new JLabel("|");
        separator.setForeground(text);

        JLabel correctAnswer = new JLabel("Correct answer: " + result.question().answer());
        correctAnswer.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        correctAnswer.setForeground(new Color(0x16A34A));

        answerSummary.add(selectedAnswer);
        answerSummary.add(separator);
        answerSummary.add(correctAnswer);
        item.add(answerSummary, BorderLayout.SOUTH);
        return item;
    }

    private JLabel label(String value, int size, int style, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton button(String value, boolean primary, Color card, Color border, Color title, Color accent) {
        JButton button = new JButton(value);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setForeground(primary ? Color.WHITE : title);
        button.setBackground(primary ? accent : card);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? accent : border),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return button;
    }

    private static final class ReviewImagePanel extends JPanel {
        private static final int MAX_IMAGE_WIDTH = 1200;
        private final String resourcePath;
        private final Color textColor;
        private BufferedImage image;
        private String message;
        private boolean loadAttempted;

        private ReviewImagePanel(String resourcePath, Color background, Color border, Color textColor) {
            this.resourcePath = resourcePath;
            this.textColor = textColor;
            setBackground(background);
            setBorder(BorderFactory.createLineBorder(border));
            setPreferredSize(new Dimension(900, 338));
            setMinimumSize(new Dimension(420, 158));
        }

        private void loadImage() {
            if (loadAttempted) {
                return;
            }
            loadAttempted = true;
            try {
                URL resource = PeTrialResultFrame.class.getResource(resourcePath);
                if (resource == null) {
                    throw new IllegalStateException("Missing image");
                }
                BufferedImage source = ImageIO.read(resource);
                if (source == null) {
                    throw new IllegalStateException("Unsupported image");
                }
                if (source.getWidth() <= MAX_IMAGE_WIDTH) {
                    image = source;
                    return;
                }

                int scaledHeight = Math.max(1,
                        (int) Math.round(source.getHeight() * MAX_IMAGE_WIDTH / (double) source.getWidth()));
                BufferedImage scaled = new BufferedImage(
                        MAX_IMAGE_WIDTH, scaledHeight, BufferedImage.TYPE_INT_RGB
                );
                Graphics2D graphics = scaled.createGraphics();
                try {
                    graphics.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC
                    );
                    graphics.setRenderingHint(
                            RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY
                    );
                    graphics.drawImage(source, 0, 0, MAX_IMAGE_WIDTH, scaledHeight, null);
                } finally {
                    graphics.dispose();
                    source.flush();
                }
                image = scaled;
            } catch (Exception exception) {
                message = "Unable to display the question image.";
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            loadImage();

            Graphics2D copy = (Graphics2D) graphics.create();
            try {
                copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                copy.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (image != null) {
                    int availableWidth = Math.max(1, getWidth() - 16);
                    int availableHeight = Math.max(1, getHeight() - 16);
                    double scale = Math.min(
                            availableWidth / (double) image.getWidth(),
                            availableHeight / (double) image.getHeight()
                    );
                    int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
                    int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
                    int x = (getWidth() - width) / 2;
                    int y = (getHeight() - height) / 2;
                    copy.drawImage(image, x, y, width, height, null);
                } else if (message != null) {
                    copy.setColor(textColor);
                    copy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    FontMetrics metrics = copy.getFontMetrics();
                    copy.drawString(
                            message,
                            Math.max(12, (getWidth() - metrics.stringWidth(message)) / 2),
                            getHeight() / 2
                    );
                }
            } finally {
                copy.dispose();
            }
        }
    }
}
