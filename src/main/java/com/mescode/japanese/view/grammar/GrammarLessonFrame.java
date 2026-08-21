package com.mescode.japanese.view.grammar;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarExample;
import com.mescode.japanese.model.grammar.GrammarPoint;
import com.mescode.japanese.service.GrammarService;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;

public class GrammarLessonFrame extends JFrame {
    private final AppNavigator navigator;
    private final GrammarService service;
    private final String chapterId;
    private final Consumer<Boolean> themeListener;
    private boolean darkMode;
    private GrammarChapter chapter;
    private int currentIndex;
    private JPanel navigationHost;
    private JPanel lessonHost;
    private JScrollPane lessonScrollPane;
    private JLabel progressLabel;
    private GrammarUi.ActionButton quizButton;

    public GrammarLessonFrame(AppNavigator navigator, String chapterId) {
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
        setTitle("Grammar Lesson");
        setSize(1120, 760);
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
            root.add(buildMissingState(), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        List<String> validationErrors = service.validateChapter(chapter);
        if (!validationErrors.isEmpty()) {
            root.add(buildInvalidState(validationErrors), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 28, 28, 28));
        body.add(buildNavigation(), BorderLayout.WEST);
        body.add(buildLessonArea(), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        refreshNavigation();
        showPoint(Math.min(currentIndex, chapter.getGrammarPoints().size() - 1));
        updateProgress();
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
        JLabel eyebrow = new JLabel("CHAPTER " + chapter.getNumber());
        eyebrow.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        eyebrow.setForeground(UITheme.getAccent(darkMode));
        titleBox.add(eyebrow);
        titleBox.add(Box.createVerticalStrut(6));
        JLabel title = new JLabel(chapter.getTitle());
        title.setFont(GrammarUi.TITLE_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        titleBox.add(title);
        header.add(titleBox, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        progressLabel = new JLabel();
        progressLabel.setFont(GrammarUi.BODY_FONT.deriveFont(Font.BOLD));
        progressLabel.setForeground(UITheme.getTextForeground(darkMode));
        actions.add(progressLabel);

        GrammarUi.ActionButton chapters = new GrammarUi.ActionButton("Các chapter", () -> darkMode, false);
        chapters.addActionListener(event -> navigator.openGrammarMenu());
        actions.add(chapters);

        quizButton = new GrammarUi.ActionButton("Quiz tổng hợp", () -> darkMode, true);
        quizButton.addActionListener(event -> navigator.openGrammarQuiz(chapterId));
        actions.add(quizButton);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private Component buildNavigation() {
        GrammarUi.SurfacePanel navigation = new GrammarUi.SurfacePanel(() -> darkMode);
        navigation.setLayout(new BorderLayout());
        navigation.setBorder(new EmptyBorder(18, 18, 20, 24));
        navigation.setPreferredSize(new Dimension(325, 500));

        JLabel label = new JLabel("NỘI DUNG BÀI HỌC");
        label.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        label.setForeground(UITheme.getAccent(darkMode));
        navigation.add(label, BorderLayout.NORTH);

        navigationHost = new JPanel();
        navigationHost.setOpaque(false);
        navigationHost.setLayout(new BoxLayout(navigationHost, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(navigationHost);
        GrammarUi.stripScrollPane(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(14, 0, 0, 0));
        navigation.add(scrollPane, BorderLayout.CENTER);
        return navigation;
    }

    private Component buildLessonArea() {
        lessonHost = new JPanel();
        lessonHost.setOpaque(false);
        lessonHost.setLayout(new BoxLayout(lessonHost, BoxLayout.Y_AXIS));

        JPanel centering = new JPanel(new GridBagLayout());
        centering.setOpaque(false);
        centering.setBorder(new EmptyBorder(0, 0, 20, 0));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        centering.add(lessonHost, constraints);

        lessonScrollPane = new JScrollPane(centering);
        GrammarUi.stripScrollPane(lessonScrollPane);
        lessonScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return lessonScrollPane;
    }

    private void refreshNavigation() {
        if (navigationHost == null) {
            return;
        }
        navigationHost.removeAll();
        List<GrammarPoint> points = chapter.getGrammarPoints();
        for (int index = 0; index < points.size(); index++) {
            GrammarPoint point = points.get(index);
            boolean completed = service.isPointCompleted(chapterId, point.getId());
            String prefix = completed ? "✓ " : (index + 1) + ". ";
            GrammarUi.ActionButton button = new GrammarUi.ActionButton(
                    prefix + point.getPattern(),
                    () -> darkMode,
                    index == currentIndex);
            button.setHorizontalAlignment(GrammarUi.ActionButton.LEFT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            int target = index;
            button.addActionListener(event -> showPoint(target));
            navigationHost.add(button);
            navigationHost.add(Box.createVerticalStrut(8));
        }
        navigationHost.revalidate();
        navigationHost.repaint();
    }

    private void showPoint(int index) {
        if (chapter == null || index < 0 || index >= chapter.getGrammarPoints().size()) {
            return;
        }
        currentIndex = index;
        refreshNavigation();
        lessonHost.removeAll();
        GrammarPoint point = chapter.getGrammarPoints().get(index);
        lessonHost.add(buildPointCard(point));
        if (index == chapter.getGrammarPoints().size() - 1 && !chapter.getApplications().isEmpty()) {
            lessonHost.add(Box.createVerticalStrut(16));
            lessonHost.add(buildApplicationsCard());
        }
        lessonHost.add(Box.createVerticalStrut(14));
        lessonHost.add(buildPager());
        lessonHost.revalidate();
        lessonHost.repaint();
        SwingUtilities.invokeLater(() -> {
            if (lessonScrollPane != null) {
                lessonScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
            }
        });
    }

    private Component buildPointCard(GrammarPoint point) {
        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 30, 30, 38));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel number = new JLabel("MẪU " + (currentIndex + 1) + " / " + chapter.getGrammarPoints().size());
        number.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        number.setForeground(UITheme.getAccent(darkMode));
        number.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(number);
        card.add(Box.createVerticalStrut(8));

        JLabel pattern = new JLabel(point.getPattern());
        pattern.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.BOLD, 29f));
        pattern.setForeground(UITheme.getTitleForeground(darkMode));
        pattern.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pattern);
        card.add(Box.createVerticalStrut(6));

        JLabel meaning = new JLabel(GrammarUi.html(point.getMeaning(), 720));
        meaning.setFont(GrammarUi.HEADING_FONT.deriveFont(18f));
        meaning.setForeground(UITheme.getAccent(darkMode));
        meaning.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(meaning);
        card.add(Box.createVerticalStrut(14));

        JLabel explanation = new JLabel(GrammarUi.html(point.getExplanation(), 720));
        explanation.setFont(GrammarUi.BODY_FONT);
        explanation.setForeground(UITheme.getTextForeground(darkMode));
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(explanation);

        if (point.getCommonMistake() != null && !point.getCommonMistake().isBlank()) {
            card.add(Box.createVerticalStrut(12));
            JLabel mistake = new JLabel(GrammarUi.html(
                    "Lưu ý: " + point.getCommonMistake(), 720));
            mistake.setFont(GrammarUi.BODY_FONT.deriveFont(Font.ITALIC));
            mistake.setForeground(GrammarUi.danger(darkMode));
            mistake.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(mistake);
        }

        card.add(Box.createVerticalStrut(22));
        card.add(sectionSeparator("VÍ DỤ"));
        card.add(Box.createVerticalStrut(10));
        for (GrammarExample example : point.getExamples()) {
            card.add(buildExample(example));
            card.add(Box.createVerticalStrut(10));
        }

        card.add(Box.createVerticalStrut(12));
        card.add(sectionSeparator("LUYỆN NHANH"));
        card.add(Box.createVerticalStrut(14));
        GrammarPracticePanel practice = new GrammarPracticePanel(
                point.getPractice(),
                () -> darkMode,
                service::checkAnswer,
                () -> {
                    service.markPointCompleted(chapterId, point.getId());
                    refreshNavigation();
                    updateProgress();
                });
        practice.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(practice);
        return card;
    }

    private Component buildExample(GrammarExample example) {
        JPanel examplePanel = new JPanel();
        examplePanel.setOpaque(true);
        examplePanel.setBackground(GrammarUi.softCard(darkMode));
        examplePanel.setBorder(new EmptyBorder(13, 16, 13, 16));
        examplePanel.setLayout(new BoxLayout(examplePanel, BoxLayout.Y_AXIS));
        examplePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        examplePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel japanese = new JLabel(GrammarUi.html(example.getJapanese(), 670));
        japanese.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.BOLD, 20f));
        japanese.setForeground(UITheme.getTitleForeground(darkMode));
        japanese.setAlignmentX(Component.LEFT_ALIGNMENT);
        examplePanel.add(japanese);

        if (example.getReading() != null && !example.getReading().isBlank()) {
            examplePanel.add(Box.createVerticalStrut(4));
            JLabel reading = new JLabel(GrammarUi.html(example.getReading(), 670));
            reading.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.PLAIN, 14f));
            reading.setForeground(UITheme.getTextForeground(darkMode));
            reading.setAlignmentX(Component.LEFT_ALIGNMENT);
            examplePanel.add(reading);
        }
        examplePanel.add(Box.createVerticalStrut(5));
        JLabel vietnamese = new JLabel(GrammarUi.html(example.getVietnamese(), 670));
        vietnamese.setFont(GrammarUi.BODY_FONT);
        vietnamese.setForeground(UITheme.getTextForeground(darkMode));
        vietnamese.setAlignmentX(Component.LEFT_ALIGNMENT);
        examplePanel.add(vietnamese);
        return examplePanel;
    }

    private Component buildApplicationsCard() {
        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 30, 30, 38));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel eyebrow = new JLabel("ỨNG DỤNG CUỐI CHAPTER");
        eyebrow.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        eyebrow.setForeground(UITheme.getAccent(darkMode));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(eyebrow);
        card.add(Box.createVerticalStrut(8));
        JLabel title = new JLabel("Tự giới thiệu và hội thoại");
        title.setFont(GrammarUi.HEADING_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        for (GrammarExample application : chapter.getApplications()) {
            if (application.getLabel() != null && !application.getLabel().isBlank()) {
                JLabel speaker = new JLabel(application.getLabel());
                speaker.setFont(GrammarUi.BODY_FONT.deriveFont(Font.BOLD));
                speaker.setForeground(UITheme.getAccent(darkMode));
                speaker.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(speaker);
                card.add(Box.createVerticalStrut(4));
            }
            card.add(buildExample(application));
            card.add(Box.createVerticalStrut(10));
        }
        return card;
    }

    private Component buildPager() {
        JPanel pager = new JPanel(new BorderLayout());
        pager.setOpaque(false);
        pager.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        pager.setAlignmentX(Component.LEFT_ALIGNMENT);

        GrammarUi.ActionButton previous = new GrammarUi.ActionButton("← Mẫu trước", () -> darkMode, false);
        previous.setEnabled(currentIndex > 0);
        previous.addActionListener(event -> showPoint(currentIndex - 1));
        pager.add(previous, BorderLayout.WEST);

        GrammarUi.ActionButton next = new GrammarUi.ActionButton(
                currentIndex == chapter.getGrammarPoints().size() - 1 ? "Đến quiz →" : "Mẫu tiếp →",
                () -> darkMode,
                true);
        if (currentIndex == chapter.getGrammarPoints().size() - 1) {
            next.setEnabled(service.isQuizUnlocked(chapter));
            next.addActionListener(event -> navigator.openGrammarQuiz(chapterId));
        } else {
            next.addActionListener(event -> showPoint(currentIndex + 1));
        }
        pager.add(next, BorderLayout.EAST);
        return pager;
    }

    private Component sectionSeparator(String text) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(text);
        label.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        label.setForeground(UITheme.getAccent(darkMode));
        panel.add(label, BorderLayout.WEST);
        JSeparator separator = new JSeparator();
        separator.setForeground(UITheme.getBorder(darkMode));
        panel.add(separator, BorderLayout.CENTER);
        return panel;
    }

    private void updateProgress() {
        if (progressLabel == null || chapter == null) {
            return;
        }
        int completed = service.getChapterProgress(chapterId).getCompletedPointIds().size();
        progressLabel.setText(completed + " / " + chapter.getGrammarPoints().size() + " mẫu đã học");
        quizButton.setEnabled(service.isQuizUnlocked(chapter));
        quizButton.setToolTipText(quizButton.isEnabled()
                ? null
                : "Hoàn thành tất cả bài luyện nhanh để mở quiz.");
    }

    private Component buildMissingState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("Không thể tải chapter được yêu cầu.");
        label.setFont(GrammarUi.HEADING_FONT);
        label.setForeground(GrammarUi.danger(darkMode));
        panel.add(label);
        return panel;
    }

    private Component buildInvalidState(List<String> errors) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel(GrammarUi.html(
                "Dữ liệu chapter chưa hợp lệ:\n• " + String.join("\n• ", errors), 680));
        label.setFont(GrammarUi.BODY_FONT);
        label.setForeground(GrammarUi.danger(darkMode));
        panel.add(label);
        return panel;
    }
}
