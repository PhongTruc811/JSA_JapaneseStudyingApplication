package com.mescode.japanese.view.grammar;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.model.grammar.GrammarAnswerResult;
import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarQuestion;
import com.mescode.japanese.model.grammar.GrammarQuizResult;
import com.mescode.japanese.service.GrammarService;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GrammarQuizFrame extends JFrame {
    private final AppNavigator navigator;
    private final GrammarService service;
    private final String chapterId;
    private final Consumer<Boolean> themeListener;
    private final Map<String, List<String>> answers = new LinkedHashMap<>();
    private boolean darkMode;
    private GrammarChapter chapter;
    private int currentIndex;
    private JPanel contentHost;
    private JLabel progressLabel;
    private GrammarQuizResult result;

    public GrammarQuizFrame(AppNavigator navigator) {
        this(navigator, "chapter-1");
    }

    public GrammarQuizFrame(AppNavigator navigator, String chapterId) {
        this.navigator = navigator;
        this.service = navigator.getAppContext().getGrammarService();
        this.chapterId = chapterId;
        this.darkMode = navigator.getAppContext().isDarkMode();
        this.chapter = service.getChapter(chapterId).orElse(null);
        this.themeListener = dark -> SwingUtilities.invokeLater(() -> {
            darkMode = dark;
            buildUi();
        });
        navigator.getAppContext().addThemeListener(themeListener);
        setTitle("Grammar Quiz");
        setSize(1100, 760);
        setMinimumSize(new Dimension(780, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUi();
    }

    @Override
    public void dispose() {
        navigator.getAppContext().removeThemeListener(themeListener);
        super.dispose();
    }

    private void buildUi() {
        GrammarUi.GradientPanel root = new GrammarUi.GradientPanel(() -> darkMode);
        root.setLayout(new BorderLayout());
        setContentPane(root);

        if (chapter == null) {
            root.add(buildBlockedState("Không thể tải Chapter này."), BorderLayout.CENTER);
        } else if (!service.validateChapter(chapter).isEmpty()) {
            root.add(buildBlockedState("Dữ liệu quiz chưa hợp lệ."), BorderLayout.CENTER);
        } else if (!service.isQuizUnlocked(chapter) && result == null) {
            root.add(buildBlockedState(
                    "Quiz chưa mở. Hãy hoàn thành bài luyện của cả 6 grammar point trước."),
                    BorderLayout.CENTER);
        } else {
            root.add(buildHeader(), BorderLayout.NORTH);
            contentHost = new JPanel(new BorderLayout());
            contentHost.setOpaque(false);
            contentHost.setBorder(new EmptyBorder(0, 28, 28, 28));
            root.add(contentHost, BorderLayout.CENTER);
            if (result == null) {
                showQuestion(Math.min(currentIndex, chapter.getQuiz().size() - 1));
            } else {
                showResult();
            }
        }
        revalidate();
        repaint();
    }

    private Component buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(24, 30, 20, 30));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("CHAPTER " + chapter.getNumber() + " · QUIZ TỔNG HỢP");
        eyebrow.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        eyebrow.setForeground(UITheme.getAccent(darkMode));
        titleBox.add(eyebrow);
        titleBox.add(Box.createVerticalStrut(6));
        JLabel title = new JLabel(result == null ? chapter.getTitle() : "Kết quả Chapter " + chapter.getNumber());
        title.setFont(GrammarUi.TITLE_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        titleBox.add(title);
        header.add(titleBox, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        progressLabel = new JLabel(result == null
                ? progressText()
                : result.correct() + " / " + result.total());
        progressLabel.setFont(GrammarUi.BODY_FONT.deriveFont(Font.BOLD));
        progressLabel.setForeground(UITheme.getTextForeground(darkMode));
        actions.add(progressLabel);
        GrammarUi.ActionButton lesson = new GrammarUi.ActionButton("Về bài học", () -> darkMode, false);
        lesson.addActionListener(event -> navigator.openGrammarLesson(chapterId));
        actions.add(lesson);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void showQuestion(int index) {
        if (chapter == null || index < 0 || index >= chapter.getQuiz().size()) {
            return;
        }
        currentIndex = index;
        contentHost.removeAll();

        JPanel centering = new JPanel(new GridBagLayout());
        centering.setOpaque(false);
        centering.setBorder(new EmptyBorder(0, 0, 12, 0));

        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 30, 40));
        card.setPreferredSize(new Dimension(780, 480));
        card.setMaximumSize(new Dimension(860, Integer.MAX_VALUE));

        GrammarQuestion question = chapter.getQuiz().get(index);
        JLabel number = new JLabel("CÂU " + (index + 1) + " / " + chapter.getQuiz().size()
                + " · " + typeLabel(question.getType()));
        number.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        number.setForeground(UITheme.getAccent(darkMode));
        number.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(number);
        card.add(Box.createVerticalStrut(12));

        GrammarQuestionPanel questionPanel = new GrammarQuestionPanel(
                question,
                () -> darkMode,
                false,
                answer -> service.checkAnswer(question, answer),
                answer -> {
                    if (answer.isEmpty()) {
                        answers.remove(question.getId());
                    } else {
                        answers.put(question.getId(), List.copyOf(answer));
                    }
                    updateProgressLabel();
                },
                null);
        questionPanel.setAnswer(answers.get(question.getId()));
        questionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(questionPanel);
        card.add(Box.createVerticalGlue());
        card.add(buildQuestionFooter());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        centering.add(card, constraints);

        JScrollPane scrollPane = new JScrollPane(centering);
        GrammarUi.stripScrollPane(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentHost.add(scrollPane, BorderLayout.CENTER);
        updateProgressLabel();
        contentHost.revalidate();
        contentHost.repaint();
    }

    private Component buildQuestionFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        GrammarUi.ActionButton previous = new GrammarUi.ActionButton("← Câu trước", () -> darkMode, false);
        previous.setEnabled(currentIndex > 0);
        previous.addActionListener(event -> showQuestion(currentIndex - 1));
        footer.add(previous, BorderLayout.WEST);

        JPanel markers = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        markers.setOpaque(false);
        for (int index = 0; index < chapter.getQuiz().size(); index++) {
            int target = index;
            String marker = answers.containsKey(chapter.getQuiz().get(index).getId()) ? "●" : "○";
            javax.swing.JButton button = new javax.swing.JButton(marker);
            button.setFont(GrammarUi.SMALL_FONT);
            button.setForeground(index == currentIndex
                    ? UITheme.getAccent(darkMode)
                    : UITheme.getTextForeground(darkMode));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setToolTipText("Câu " + (index + 1));
            button.addActionListener(event -> showQuestion(target));
            markers.add(button);
        }
        footer.add(markers, BorderLayout.CENTER);

        GrammarUi.ActionButton next = new GrammarUi.ActionButton(
                currentIndex == chapter.getQuiz().size() - 1 ? "Nộp bài" : "Câu tiếp →",
                () -> darkMode,
                true);
        if (currentIndex == chapter.getQuiz().size() - 1) {
            next.addActionListener(event -> submitQuiz());
        } else {
            next.addActionListener(event -> showQuestion(currentIndex + 1));
        }
        footer.add(next, BorderLayout.EAST);
        return footer;
    }

    private void submitQuiz() {
        int unanswered = chapter.getQuiz().size() - answers.size();
        if (unanswered > 0) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn còn " + unanswered + " câu chưa trả lời. Vẫn nộp bài?",
                    "Nộp bài",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        result = service.submitQuiz(chapter, answers);
        buildUi();
    }

    private void showResult() {
        contentHost.removeAll();

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 0, 24, 0));

        GrammarUi.SurfacePanel summary = new GrammarUi.SurfacePanel(() -> darkMode);
        summary.setLayout(new BorderLayout(24, 0));
        summary.setBorder(new EmptyBorder(24, 28, 26, 36));
        summary.setMaximumSize(new Dimension(900, 150));
        summary.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel score = new JLabel(result.correct() + " / " + result.total());
        score.setFont(GrammarUi.TITLE_FONT.deriveFont(44f));
        score.setForeground(result.passed() ? GrammarUi.success(darkMode) : GrammarUi.danger(darkMode));
        summary.add(score, BorderLayout.WEST);

        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(result.passed() ? "Đã hoàn thành Chapter 1" : "Chưa đạt mốc 70%");
        title.setFont(GrammarUi.HEADING_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        message.add(title);
        message.add(Box.createVerticalStrut(5));
        JLabel detail = new JLabel(result.passed()
                ? "Bạn đã đạt yêu cầu. Hãy xem lại giải thích để ghi nhớ lâu hơn."
                : "Cần ít nhất 9/12 câu đúng. Xem lại các câu sai rồi thử lại.");
        detail.setFont(GrammarUi.BODY_FONT);
        detail.setForeground(UITheme.getTextForeground(darkMode));
        message.add(detail);
        summary.add(message, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        GrammarUi.ActionButton retry = new GrammarUi.ActionButton("Làm lại", () -> darkMode, true);
        retry.addActionListener(event -> {
            answers.clear();
            result = null;
            currentIndex = 0;
            buildUi();
        });
        GrammarUi.ActionButton chapters = new GrammarUi.ActionButton("Các chapter", () -> darkMode, false);
        chapters.addActionListener(event -> navigator.openGrammarMenu());
        actions.add(retry);
        actions.add(chapters);
        summary.add(actions, BorderLayout.EAST);
        list.add(summary);
        list.add(Box.createVerticalStrut(16));

        int index = 1;
        for (GrammarAnswerResult answerResult : result.answers()) {
            list.add(buildReviewCard(index++, answerResult));
            list.add(Box.createVerticalStrut(10));
        }

        JPanel centering = new JPanel(new GridBagLayout());
        centering.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centering.add(list, constraints);

        JScrollPane scrollPane = new JScrollPane(centering);
        GrammarUi.stripScrollPane(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentHost.add(scrollPane, BorderLayout.CENTER);
        contentHost.revalidate();
        contentHost.repaint();
    }

    private Component buildReviewCard(int index, GrammarAnswerResult answerResult) {
        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode, 18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 22, 22, 30));
        card.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel((answerResult.correct() ? "✓ " : "✕ ") + "Câu " + index);
        status.setFont(GrammarUi.BODY_FONT.deriveFont(Font.BOLD));
        status.setForeground(answerResult.correct()
                ? GrammarUi.success(darkMode)
                : GrammarUi.danger(darkMode));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(status);
        card.add(Box.createVerticalStrut(6));

        JLabel prompt = new JLabel(GrammarUi.html(answerResult.question().getPrompt(), 760));
        prompt.setFont(GrammarUi.BODY_FONT.deriveFont(Font.BOLD));
        prompt.setForeground(UITheme.getTitleForeground(darkMode));
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(prompt);
        card.add(Box.createVerticalStrut(7));

        JLabel submitted = new JLabel(GrammarUi.html(
                "Bạn chọn: " + answerText(answerResult.submittedAnswer()), 760));
        submitted.setFont(GrammarUi.JAPANESE_FONT.deriveFont(15f));
        submitted.setForeground(UITheme.getTextForeground(darkMode));
        submitted.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(submitted);

        if (!answerResult.correct()) {
            card.add(Box.createVerticalStrut(4));
            JLabel expected = new JLabel(GrammarUi.html(
                    "Đáp án: " + answerText(answerResult.question().getAnswer()), 760));
            expected.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.BOLD, 15f));
            expected.setForeground(GrammarUi.success(darkMode));
            expected.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(expected);
        }
        card.add(Box.createVerticalStrut(7));
        JLabel explanation = new JLabel(GrammarUi.html(
                answerResult.question().getExplanation(), 760));
        explanation.setFont(GrammarUi.BODY_FONT);
        explanation.setForeground(UITheme.getTextForeground(darkMode));
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(explanation);
        return card;
    }

    private Component buildBlockedState(String message) {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 34, 34, 42));
        card.setPreferredSize(new Dimension(560, 240));

        JLabel title = new JLabel("Chưa thể bắt đầu quiz");
        title.setFont(GrammarUi.HEADING_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        JLabel detail = new JLabel(GrammarUi.html(message, 480));
        detail.setFont(GrammarUi.BODY_FONT);
        detail.setForeground(UITheme.getTextForeground(darkMode));
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(detail);
        card.add(Box.createVerticalStrut(20));
        GrammarUi.ActionButton lesson = new GrammarUi.ActionButton("Về bài học", () -> darkMode, true);
        lesson.setAlignmentX(Component.LEFT_ALIGNMENT);
        lesson.addActionListener(event -> navigator.openGrammarLesson(chapterId));
        card.add(lesson);
        center.add(card);
        return center;
    }

    private void updateProgressLabel() {
        if (progressLabel != null) {
            progressLabel.setText(progressText());
        }
    }

    private String progressText() {
        return answers.size() + " / " + (chapter == null ? 0 : chapter.getQuiz().size()) + " câu đã trả lời";
    }

    private String typeLabel(GrammarQuestion.Type type) {
        return switch (type) {
            case MULTIPLE_CHOICE -> "CHỌN ĐÁP ÁN";
            case WORD_BANK -> "ĐIỀN TỪ";
            case SENTENCE_ORDER -> "XẾP CÂU";
        };
    }

    private String answerText(List<String> values) {
        return values == null || values.isEmpty() ? "Chưa trả lời" : String.join(" ", values);
    }
}
