package com.mescode.japanese.view.grammar;

import com.mescode.japanese.model.grammar.GrammarQuestion;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class GrammarQuestionPanel extends JPanel {
    private final GrammarQuestion question;
    private final Supplier<Boolean> darkSupplier;
    private final Predicate<List<String>> checker;
    private final Consumer<List<String>> answerListener;
    private final Runnable correctListener;
    private final boolean showCheckButton;
    private final List<String> selected = new ArrayList<>();
    private final Map<String, JButton> optionButtons = new LinkedHashMap<>();
    private final JPanel optionsHost = new JPanel(new BorderLayout());
    private final JLabel answerPreview = new JLabel("Chưa chọn đáp án");
    private final JLabel feedback = new JLabel(" ");
    private boolean evaluated;

    GrammarQuestionPanel(
            GrammarQuestion question,
            Supplier<Boolean> darkSupplier,
            boolean showCheckButton,
            Predicate<List<String>> checker,
            Consumer<List<String>> answerListener,
            Runnable correctListener
    ) {
        this.question = question;
        this.darkSupplier = darkSupplier;
        this.showCheckButton = showCheckButton;
        this.checker = checker;
        this.answerListener = answerListener == null ? ignored -> { } : answerListener;
        this.correctListener = correctListener == null ? () -> { } : correctListener;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        build();
    }

    List<String> getAnswer() {
        return List.copyOf(selected);
    }

    void setAnswer(List<String> answer) {
        selected.clear();
        if (answer != null) {
            selected.addAll(answer);
        }
        renderOptions();
        updatePreview();
    }

    void showEvaluation(boolean correct) {
        // A wrong answer must remain editable so learners can correct it
        // without leaving and reopening the grammar point.
        evaluated = correct;
        boolean dark = darkSupplier.get();
        feedback.setForeground(correct ? GrammarUi.success(dark) : GrammarUi.danger(dark));
        String prefix = correct ? "Chính xác. " : "Chưa đúng. ";
        feedback.setText(GrammarUi.html(prefix + safe(question.getExplanation()), 660));
        optionButtons.values().forEach(button ->
                button.setCursor(java.awt.Cursor.getPredefinedCursor(
                        correct ? java.awt.Cursor.DEFAULT_CURSOR : java.awt.Cursor.HAND_CURSOR)));
    }

    private void build() {
        boolean dark = darkSupplier.get();
        JLabel prompt = new JLabel(GrammarUi.html(question.getPrompt(), 680));
        prompt.setFont(GrammarUi.HEADING_FONT);
        prompt.setForeground(UITheme.getTitleForeground(dark));
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(prompt);

        if (question.getReading() != null && !question.getReading().isBlank()) {
            add(Box.createVerticalStrut(7));
            JLabel reading = new JLabel(GrammarUi.html(question.getReading(), 680));
            reading.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.PLAIN, 16f));
            reading.setForeground(UITheme.getTextForeground(dark));
            reading.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(reading);
        }

        add(Box.createVerticalStrut(18));
        answerPreview.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.BOLD, 17f));
        answerPreview.setForeground(UITheme.getAccent(dark));
        answerPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (question.getType() != GrammarQuestion.Type.MULTIPLE_CHOICE) {
            add(answerPreview);
            add(Box.createVerticalStrut(12));
        }

        optionsHost.setOpaque(false);
        optionsHost.setAlignmentX(Component.LEFT_ALIGNMENT);
        renderOptions();
        add(optionsHost);
        add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (question.getType() != GrammarQuestion.Type.MULTIPLE_CHOICE) {
            GrammarUi.ActionButton undo = new GrammarUi.ActionButton("Hoàn tác", darkSupplier, false);
            undo.addActionListener(event -> {
                if (!selected.isEmpty() && !evaluated) {
                    selected.remove(selected.size() - 1);
                    renderOptions();
                    notifyChanged();
                }
            });
            GrammarUi.ActionButton clear = new GrammarUi.ActionButton("Làm lại", darkSupplier, false);
            clear.addActionListener(event -> {
                if (!evaluated) {
                    selected.clear();
                    renderOptions();
                    notifyChanged();
                }
            });
            actions.add(undo);
            actions.add(Box.createHorizontalStrut(8));
            actions.add(clear);
        }
        if (showCheckButton) {
            if (actions.getComponentCount() > 0) {
                actions.add(Box.createHorizontalStrut(8));
            }
            GrammarUi.ActionButton check = new GrammarUi.ActionButton("Kiểm tra", darkSupplier, true);
            check.addActionListener(event -> {
                if (selected.isEmpty()) {
                    feedback.setForeground(GrammarUi.danger(darkSupplier.get()));
                    feedback.setText("Hãy chọn một đáp án trước.");
                    return;
                }
                boolean correct = checker.test(getAnswer());
                showEvaluation(correct);
                if (correct) {
                    correctListener.run();
                }
            });
            actions.add(check);
        }
        if (actions.getComponentCount() > 0) {
            add(actions);
            add(Box.createVerticalStrut(10));
        }

        feedback.setFont(GrammarUi.BODY_FONT);
        feedback.setForeground(UITheme.getTextForeground(dark));
        feedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(feedback);
    }

    private void renderOptions() {
        optionsHost.removeAll();
        optionButtons.clear();
        List<String> displayOptions = new ArrayList<>(question.getOptions());
        if (selected.isEmpty()) {
            Collections.shuffle(displayOptions);
        }

        JPanel options = question.getType() == GrammarQuestion.Type.MULTIPLE_CHOICE
                ? verticalOptions(displayOptions)
                : tokenOptions(displayOptions);
        optionsHost.add(options, BorderLayout.CENTER);
        optionsHost.revalidate();
        optionsHost.repaint();
        updatePreview();
    }

    private JPanel verticalOptions(List<String> displayOptions) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String option : displayOptions) {
            JButton button = createOptionButton(option);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(event -> {
                if (evaluated) {
                    return;
                }
                selected.clear();
                selected.add(option);
                refreshButtonStyles();
                notifyChanged();
            });
            optionButtons.put(option, button);
            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }
        refreshButtonStyles();
        return panel;
    }

    private JPanel tokenOptions(List<String> displayOptions) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setOpaque(false);
        for (String option : displayOptions) {
            JButton button = createOptionButton(option);
            button.setEnabled(!selected.contains(option) && !evaluated);
            button.addActionListener(event -> {
                if (!evaluated && !selected.contains(option)) {
                    selected.add(option);
                    renderOptions();
                    notifyChanged();
                }
            });
            optionButtons.put(option, button);
            panel.add(button);
        }
        return panel;
    }

    private JButton createOptionButton(String option) {
        JButton button = new JButton(option);
        button.setFont(GrammarUi.JAPANESE_FONT.deriveFont(Font.PLAIN, 17f));
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 14, 11, 14));
        button.setOpaque(true);
        refreshButtonStyle(button, selected.contains(option));
        return button;
    }

    private void refreshButtonStyles() {
        optionButtons.forEach((option, button) -> refreshButtonStyle(button, selected.contains(option)));
    }

    private void refreshButtonStyle(JButton button, boolean chosen) {
        boolean dark = darkSupplier.get();
        button.setForeground(chosen ? Color.WHITE : UITheme.getTitleForeground(dark));
        button.setBackground(chosen ? UITheme.getAccent(dark) : GrammarUi.softCard(dark));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        chosen ? UITheme.getAccent(dark) : UITheme.getBorder(dark), 1),
                new EmptyBorder(10, 13, 10, 13)));
    }

    private void updatePreview() {
        if (question.getType() == GrammarQuestion.Type.MULTIPLE_CHOICE) {
            return;
        }
        answerPreview.setText(selected.isEmpty()
                ? "Chưa chọn đáp án"
                : GrammarUi.html(String.join(" ", selected), 650));
    }

    private void notifyChanged() {
        updatePreview();
        answerListener.accept(getAnswer());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
