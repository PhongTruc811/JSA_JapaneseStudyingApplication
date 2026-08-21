package com.mescode.japanese.view.grammar;

import com.mescode.japanese.model.grammar.GrammarQuestion;
import com.mescode.japanese.view.theme.UITheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

final class GrammarPracticePanel extends JPanel {
    private final List<GrammarQuestion> questions;
    private final Supplier<Boolean> darkSupplier;
    private final BiPredicate<GrammarQuestion, List<String>> checker;
    private final Runnable completedListener;
    private int currentIndex;

    GrammarPracticePanel(
            List<GrammarQuestion> questions,
            Supplier<Boolean> darkSupplier,
            BiPredicate<GrammarQuestion, List<String>> checker,
            Runnable completedListener
    ) {
        this.questions = questions == null ? List.of() : List.copyOf(questions);
        this.darkSupplier = darkSupplier;
        this.checker = checker;
        this.completedListener = completedListener;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        renderCurrentQuestion();
    }

    private void renderCurrentQuestion() {
        removeAll();
        boolean dark = darkSupplier.get();
        if (questions.isEmpty()) {
            JLabel empty = new JLabel("Chưa có câu luyện nhanh cho mẫu ngữ pháp này.");
            empty.setFont(GrammarUi.BODY_FONT);
            empty.setForeground(GrammarUi.danger(dark));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(empty);
            return;
        }

        GrammarQuestion question = questions.get(currentIndex);
        JLabel counter = new JLabel("CÂU " + (currentIndex + 1) + " / " + questions.size());
        counter.setFont(GrammarUi.SMALL_FONT.deriveFont(Font.BOLD));
        counter.setForeground(UITheme.getAccent(dark));
        counter.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(counter);
        add(Box.createVerticalStrut(10));

        GrammarUi.ActionButton next = new GrammarUi.ActionButton(
                "Câu tiếp →",
                darkSupplier,
                true
        );
        next.setVisible(false);
        next.addActionListener(event -> {
            currentIndex++;
            renderCurrentQuestion();
        });

        JLabel status = new JLabel(" ");
        status.setFont(GrammarUi.BODY_FONT);
        status.setForeground(GrammarUi.success(dark));

        GrammarQuestionPanel questionPanel = new GrammarQuestionPanel(
                question,
                darkSupplier,
                true,
                answer -> checker.test(question, answer),
                ignored -> { },
                () -> {
                    if (currentIndex == questions.size() - 1) {
                        status.setText("Hoàn thành " + questions.size()
                                + "/" + questions.size() + " câu luyện nhanh.");
                        completedListener.run();
                    } else {
                        status.setText("Chính xác! Tiếp tục với câu kế tiếp.");
                        next.setVisible(true);
                    }
                    revalidate();
                    repaint();
                }
        );
        questionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        add(questionPanel);
        add(Box.createVerticalStrut(8));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(status);
        footer.add(Box.createHorizontalStrut(14));
        footer.add(next);
        add(footer);

        revalidate();
        repaint();
    }
}
