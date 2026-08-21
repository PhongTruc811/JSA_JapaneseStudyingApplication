package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizDifficulty;
import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class VocabQuizTypeDialog extends JDialog {
    private static final int DIALOG_WIDTH = 580;
    private static final int CONTENT_WIDTH = 520;
    private final boolean darkMode;
    private final List<Vocabulary> vocabularies;
    private final Color panel;
    private final Color card;
    private final Color border;
    private final Color hover;
    private final Color title;
    private final Color text;
    private final Color accent;
    private final DialogCard content = new DialogCard();
    private boolean showKanji;
    private Set<Integer> chapters = new LinkedHashSet<>();
    private VocabQuizDifficulty difficulty = VocabQuizDifficulty.EASY;
    private VocabQuizConfig config;

    private VocabQuizTypeDialog(JFrame owner, boolean darkMode, List<Vocabulary> vocabularies) {
        super(owner, true);
        this.darkMode = darkMode;
        this.vocabularies = vocabularies == null ? List.of() : List.copyOf(vocabularies);
        panel = UITheme.getPanelBackground(darkMode);
        card = UITheme.getCardBackground(darkMode);
        border = UITheme.getBorder(darkMode);
        hover = UITheme.getHover(darkMode);
        title = UITheme.getTitleForeground(darkMode);
        text = UITheme.getTextForeground(darkMode);
        accent = UITheme.getAccent(darkMode);
        setupDialog();
        showTypeStep();
    }

    public static VocabQuizConfig showDialog(JFrame owner, boolean darkMode, List<Vocabulary> vocabularies) {
        VocabQuizTypeDialog dialog = new VocabQuizTypeDialog(owner, darkMode, vocabularies);
        dialog.setVisible(true);
        return dialog.config;
    }

    private void setupDialog() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(DIALOG_WIDTH, 400);
        setLocationRelativeTo(getOwner());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                dispose();
            }
        });
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(24, 30, 30, 30));
        setContentPane(content);
    }

    private void showTypeStep() {
        JPanel body = body("Step 1 of 3", "Select quiz type", "Chọn loại nội dung mà bạn muốn làm quiz");
        body.add(choiceButton("語", "Vocabulary", "Từ vựng (chapter 1-3)", () -> {
            showKanji = false;
            showChapterStep();
        }));
        body.add(Box.createVerticalStrut(10));
        body.add(choiceButton("漢", "Kanji", "Kanji Hán Tự (chapter 1-3)", () -> {
            showKanji = true;
            showChapterStep();
        }));
        setBody(body);
    }

    private void showChapterStep() {
        JPanel body = body("Step 2 of 3", "Select chapters", "Có thể chọn nhiều hơn 1 chapter");
        JPanel chaptersPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        chaptersPanel.setOpaque(false);
        chaptersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chaptersPanel.setPreferredSize(new Dimension(CONTENT_WIDTH, 76));
        chaptersPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 76));
        for (int chapter = 1; chapter <= 3; chapter++) {
            final int chapterNumber = chapter;
            JToggleButton button = new JToggleButton("Chapter " + chapter);
            button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
            button.setForeground(title);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setBorder(new EmptyBorder(14, 10, 14, 10));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setSelected(chapters.contains(chapter));
            button.addActionListener(event -> {
                if (button.isSelected()) chapters.add(chapterNumber); else chapters.remove(chapterNumber);
                button.repaint();
            });
            button.setUI(new ChapterToggleUI(button));
            chaptersPanel.add(button);
        }
        body.add(chaptersPanel);
        body.add(Box.createVerticalStrut(24));
        body.add(actions(() -> showTypeStep(), () -> {
            if (chapters.isEmpty()) {
                showError("Bạn chưa chọn chapter để làm quiz");
                return;
            }
            showDifficultyStep();
        }, "Next"));
        setBody(body);
    }

    private void showDifficultyStep() {
        JPanel body = body("Step 3 of 3", "Select Quiz Mode", "Chọn mức độ Khó hoặc Dễ cho bài quiz của bạn");
        ButtonGroup group = new ButtonGroup();
        body.add(difficultyButton(group, VocabQuizDifficulty.EASY,
                "Easy", "Có nút bấm check đáp án trong lúc làm, giúp hỗ trợ ôn tập"));
        body.add(Box.createVerticalStrut(10));
        body.add(difficultyButton(group, VocabQuizDifficulty.HARD,
                "Hard", "Không hỗ trợ check đáp án, trải nghiệm giống thi thực tế"));
        body.add(Box.createVerticalStrut(24));
        body.add(actions(this::showChapterStep, this::startQuiz, "Start quiz"));
        setBody(body);
    }

    private JComponent difficultyButton(ButtonGroup group, VocabQuizDifficulty value, String heading, String description) {
        JRadioButton button = new JRadioButton("<html><b>" + heading + "</b><br><span style='font-size:10px'>"
                + description + "</span></html>");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(title);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(14, 16, 14, 16));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(CONTENT_WIDTH, 74));
        button.setMaximumSize(new Dimension(CONTENT_WIDTH, 74));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setSelected(difficulty == value);
        button.addActionListener(event -> difficulty = value);
        button.setUI(new ChapterToggleUI(button));
        group.add(button);
        return button;
    }

    private void startQuiz() {
        VocabQuizConfig candidate = new VocabQuizConfig(showKanji, chapters, difficulty);
        String error = VocabService.validateQuizData(vocabularies, candidate);
        if (error != null) {
            showError(error);
            return;
        }
        config = candidate;
        dispose();
    }

    private JPanel body(String step, String heading, String subtitle) {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel stepLabel = centeredLabel(step.toUpperCase(), 12, Font.BOLD, accent);
        body.add(stepLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(centeredLabel(heading, 26, Font.BOLD, title));
        body.add(Box.createVerticalStrut(5));
        body.add(centeredLabel(subtitle, 14, Font.PLAIN, text));
        body.add(Box.createVerticalStrut(24));
        return body;
    }

    private JButton choiceButton(String mark, String heading, String description, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(16, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(14, 16, 14, 16));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(CONTENT_WIDTH, 86));
        button.setMaximumSize(new Dimension(CONTENT_WIDTH, 86));
        JLabel icon = new JLabel(mark, SwingConstants.CENTER);
        icon.setFont(new Font("Yu Mincho", Font.BOLD, 28));
        icon.setForeground(accent);
        icon.setPreferredSize(new Dimension(52, 52));
        button.add(icon, BorderLayout.WEST);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(label(heading, 17, Font.BOLD, title));
        copy.add(label(description, 12, Font.PLAIN, text));
        button.add(copy, BorderLayout.CENTER);
        JLabel arrow = new JLabel(">", SwingConstants.CENTER);
        button.add(arrow, BorderLayout.EAST);
        button.addActionListener(event -> action.run());
        button.setUI(new ChoiceButtonUI(button));
        forwardClick(icon, button);
        forwardClick(copy, button);
        forwardClick(arrow, button);
        return button;
    }

    private void forwardClick(Component component, JButton target) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                target.getModel().setRollover(true);
                target.repaint();
            }

            @Override public void mouseExited(MouseEvent event) {
                Point point = SwingUtilities.convertPoint(component, event.getPoint(), target);
                if (!target.contains(point)) {
                    target.getModel().setRollover(false);
                    target.repaint();
                }
            }

            @Override public void mouseClicked(MouseEvent event) {
                target.doClick();
            }
        });
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                forwardClick(child, target);
            }
        }
    }

    private JPanel actions(Runnable backAction, Runnable nextAction, String nextText) {
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Keep BoxLayout from stretching the navigation buttons to fill the
        // remaining vertical space in steps 2 and 3.
        actions.setPreferredSize(new Dimension(CONTENT_WIDTH, 52));
        actions.setMaximumSize(new Dimension(CONTENT_WIDTH, 52));
        JButton back = actionButton("Back", false);
        JButton next = actionButton(nextText, true);
        back.addActionListener(event -> backAction.run());
        next.addActionListener(event -> nextAction.run());
        actions.add(back, BorderLayout.WEST);
        actions.add(next, BorderLayout.EAST);
        return actions;
    }

    private JButton actionButton(String value, boolean primary) {
        JButton button = new JButton(value);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        button.setForeground(primary ? Color.WHITE : title);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new ActionButtonUI(button, primary));
        return button;
    }

    private void setBody(JPanel body) {
        content.removeAll();
        content.add(header(), BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
        // Each step has a different amount of content. Repack vertically so
        // shorter steps do not leave a large empty area below the controls.
        pack();
        setSize(DIALOG_WIDTH, getHeight());
        setLocationRelativeTo(getOwner());
    }

    private JComponent header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("VOCABULARY QUIZ", 12, Font.BOLD, accent), BorderLayout.WEST);
        JButton close = new JButton("x");
        close.setForeground(text);
        close.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        close.setBorder(null);
        close.setContentAreaFilled(false);
        close.setFocusPainted(false);
        close.addActionListener(event -> dispose());
        header.add(close, BorderLayout.EAST);
        return header;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Cannot start quiz", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel label(String value, int size, int style, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel centeredLabel(String value, int size, int style, Color color) {
        JLabel label = label(value, size, style, color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(CONTENT_WIDTH, label.getPreferredSize().height));
        label.setMaximumSize(new Dimension(CONTENT_WIDTH, label.getPreferredSize().height));
        return label;
    }

    private class DialogCard extends JPanel {
        DialogCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(15, 23, 42, darkMode ? 100 : 55));
            g2.fillRoundRect(9, 11, getWidth() - 18, getHeight() - 18, 30, 30);
            g2.setColor(panel);
            g2.fillRoundRect(0, 0, getWidth() - 11, getHeight() - 11, 30, 30);
            g2.setColor(darkMode ? new Color(255, 255, 255, 22) : border);
            g2.drawRoundRect(0, 0, getWidth() - 12, getHeight() - 12, 30, 30);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private class ChoiceButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final JButton button;
        ChoiceButtonUI(JButton button) { this.button = button; }
        @Override public void paint(Graphics graphics, JComponent component) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(button.getModel().isRollover() ? hover : card);
            g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 18, 18);
            g2.setColor(button.getModel().isRollover() ? accent : border);
            g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, 18, 18);
            g2.dispose();
            super.paint(graphics, component);
        }
    }

    private class ChapterToggleUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final AbstractButton button;
        ChapterToggleUI(AbstractButton button) { this.button = button; }
        @Override public void paint(Graphics graphics, JComponent component) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            boolean active = button.isSelected();
            g2.setColor(active ? accent : button.getModel().isRollover() ? hover : card);
            g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 15, 15);
            g2.setColor(active ? accent : border);
            g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, 15, 15);
            g2.dispose();
            button.setForeground(active ? Color.WHITE : title);
            super.paint(graphics, component);
        }
    }

    private class ActionButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final JButton button;
        private final boolean primary;
        ActionButtonUI(JButton button, boolean primary) { this.button = button; this.primary = primary; }
        @Override public void paint(Graphics graphics, JComponent component) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(primary ? accent : card);
            g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 15, 15);
            g2.setColor(primary ? accent : border);
            g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, 15, 15);
            g2.dispose();
            super.paint(graphics, component);
        }
    }
}
