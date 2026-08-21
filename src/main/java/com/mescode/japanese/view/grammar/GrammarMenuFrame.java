package com.mescode.japanese.view.grammar;

import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarChapterProgress;
import com.mescode.japanese.service.GrammarService;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GrammarMenuFrame extends JFrame {
    private final AppNavigator navigator;
    private final GrammarService service;
    private final Consumer<Boolean> themeListener;
    private boolean darkMode;

    public GrammarMenuFrame(AppNavigator navigator) {
        this.navigator = navigator;
        this.service = navigator.getAppContext().getGrammarService();
        this.darkMode = navigator.getAppContext().isDarkMode();
        this.themeListener = dark -> SwingUtilities.invokeLater(() -> {
            darkMode = dark;
            buildUi();
        });
        navigator.getAppContext().addThemeListener(themeListener);
        setTitle("Grammar Chapters");
        setSize(980, 720);
        setMinimumSize(new Dimension(700, 520));
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

        JPanel viewport = new JPanel(new GridBagLayout());
        viewport.setOpaque(false);
        viewport.setBorder(new EmptyBorder(28, 34, 32, 34));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setPreferredSize(new Dimension(820, 620));
        content.setMaximumSize(new Dimension(880, Integer.MAX_VALUE));
        content.add(buildHeader());
        content.add(Box.createVerticalStrut(20));

        List<GrammarChapter> chapters = service.getChapters();
        if (chapters.isEmpty()) {
            content.add(buildEmptyState());
        } else {
            for (GrammarChapter metadata : chapters) {
                content.add(buildChapterCard(metadata));
                content.add(Box.createVerticalStrut(14));
            }
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTH;
        viewport.add(content, constraints);

        JScrollPane scrollPane = new JScrollPane(viewport);
        GrammarUi.stripScrollPane(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private Component buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("GRAMMAR ROADMAP");
        eyebrow.setFont(GrammarUi.SMALL_FONT.deriveFont(java.awt.Font.BOLD));
        eyebrow.setForeground(UITheme.getAccent(darkMode));
        text.add(eyebrow);
        text.add(Box.createVerticalStrut(8));

        JLabel title = new JLabel("Học ngữ pháp theo chapter");
        title.setFont(GrammarUi.TITLE_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        text.add(title);
        text.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Học từng mẫu câu, hoàn thành bài luyện và chinh phục quiz tổng hợp.");
        subtitle.setFont(GrammarUi.BODY_FONT);
        subtitle.setForeground(UITheme.getTextForeground(darkMode));
        text.add(subtitle);
        header.add(text, BorderLayout.CENTER);

        GrammarUi.ActionButton back = new GrammarUi.ActionButton("Về Menu", () -> darkMode, false);
        back.addActionListener(event -> navigator.navigateTo(AppRoute.AppMenu));
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(back);
        header.add(backWrap, BorderLayout.EAST);
        return header;
    }

    private Component buildChapterCard(GrammarChapter metadata) {
        GrammarUi.SurfacePanel card = new GrammarUi.SurfacePanel(() -> darkMode);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(22, 24, 24, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        card.setPreferredSize(new Dimension(820, 176));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel number = new JLabel(String.format("%02d", metadata.getNumber()));
        number.setFont(GrammarUi.TITLE_FONT.deriveFont(38f));
        number.setForeground(metadata.isAvailable()
                ? UITheme.getAccent(darkMode)
                : UITheme.getBorder(darkMode));
        number.setPreferredSize(new Dimension(72, 70));
        card.add(number, BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Chapter " + metadata.getNumber() + " · " + metadata.getTitle());
        title.setFont(GrammarUi.HEADING_FONT);
        title.setForeground(UITheme.getTitleForeground(darkMode));
        body.add(title);
        body.add(Box.createVerticalStrut(5));

        JLabel description = new JLabel(GrammarUi.html(metadata.getDescription(), 490));
        description.setFont(GrammarUi.BODY_FONT);
        description.setForeground(UITheme.getTextForeground(darkMode));
        body.add(description);
        body.add(Box.createVerticalStrut(12));

        JLabel progressLabel = new JLabel(progressText(metadata));
        progressLabel.setFont(GrammarUi.SMALL_FONT.deriveFont(java.awt.Font.BOLD));
        progressLabel.setForeground(metadata.isAvailable()
                ? UITheme.getAccent(darkMode)
                : UITheme.getTextForeground(darkMode));
        body.add(progressLabel);
        card.add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setPreferredSize(new Dimension(170, 120));
        if (metadata.isAvailable()) {
            Optional<GrammarChapter> chapter = service.getChapter(metadata.getId());
            GrammarUi.ActionButton lesson = new GrammarUi.ActionButton(
                    completedCount(metadata) > 0 ? "Tiếp tục học" : "Bắt đầu học",
                    () -> darkMode,
                    true);
            lesson.setMaximumSize(new Dimension(170, 44));
            lesson.addActionListener(event -> navigator.openGrammarLesson(metadata.getId()));
            actions.add(lesson);
            actions.add(Box.createVerticalStrut(8));

            GrammarUi.ActionButton quiz = new GrammarUi.ActionButton("Quiz tổng hợp", () -> darkMode, false);
            quiz.setMaximumSize(new Dimension(170, 44));
            quiz.setEnabled(chapter.filter(service::isQuizUnlocked).isPresent());
            quiz.setToolTipText(quiz.isEnabled() ? null : "Hoàn thành 6 grammar point để mở quiz.");
            quiz.addActionListener(event -> navigator.openGrammarQuiz(metadata.getId()));
            actions.add(quiz);
        } else {
            GrammarUi.ActionButton comingSoon = new GrammarUi.ActionButton("Sắp ra mắt", () -> darkMode, false);
            comingSoon.setEnabled(false);
            comingSoon.setMaximumSize(new Dimension(170, 44));
            actions.add(comingSoon);
        }
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private Component buildEmptyState() {
        GrammarUi.SurfacePanel panel = new GrammarUi.SurfacePanel(() -> darkMode);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 30, 28, 30));
        JLabel label = new JLabel("Không thể tải dữ liệu Grammar. Hãy kiểm tra resources/data/grammar.");
        label.setFont(GrammarUi.BODY_FONT);
        label.setForeground(GrammarUi.danger(darkMode));
        panel.add(label);
        return panel;
    }

    private String progressText(GrammarChapter metadata) {
        if (!metadata.isAvailable()) {
            return "CHƯA CÓ NỘI DUNG";
        }
        Optional<GrammarChapter> chapter = service.getChapter(metadata.getId());
        if (chapter.isEmpty()) {
            return "KHÔNG THỂ TẢI CHAPTER";
        }
        GrammarChapter loaded = chapter.get();
        GrammarChapterProgress progress = service.getChapterProgress(metadata.getId());
        if (service.isChapterCompleted(loaded)) {
            return "HOÀN THÀNH · BEST " + progress.getBestCorrect() + "/" + progress.getBestTotal();
        }
        String best = progress.getBestTotal() > 0
                ? " · BEST " + progress.getBestCorrect() + "/" + progress.getBestTotal()
                : "";
        return progress.getCompletedPointIds().size() + "/" + loaded.getGrammarPoints().size()
                + " MẪU ĐÃ HỌC" + best;
    }

    private int completedCount(GrammarChapter metadata) {
        return metadata.isAvailable()
                ? service.getChapterProgress(metadata.getId()).getCompletedPointIds().size()
                : 0;
    }
}
