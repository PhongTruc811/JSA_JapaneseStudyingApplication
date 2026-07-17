package com.mescode.japanese.view.grammar;

import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.model.GrammarQuestion;
import com.mescode.japanese.repo.GrammarRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarQuizFrame extends JFrame {
    private final MenuNavigator navigator;
    private final GrammarRepository repo = new GrammarRepository();
    private List<GrammarQuestion> questions = Collections.emptyList();

    private boolean darkTheme = true;
    private boolean submitted = false;
    private int lastScore = 0;
    private int lastAttempted = 0;

    private Color bgTop, bgBottom, cardBg, cardBorder, textPrimary, textSecondary;
    private Color accent, accentDark, success, danger, neutral;
    private Color optionBg, optionHover, optionSelectedBg, optionCorrectBg, optionWrongBg, optionMutedBg;
    private Color badgeBg, badgeFg;

    private final Font latinFont = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font latinBoldFont = new Font("Segoe UI Semibold", Font.BOLD, 15);
    private final Font japaneseFont = resolveJapaneseFont();
    private final Font mixedTextFont = resolveMixedTextFont();
    private final Font japaneseBoldFont = japaneseFont.deriveFont(Font.BOLD);

    private final JLabel badgeLabel = new JLabel("Grammar Exercise");
    private final JLabel titleLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel subtitleLabel = new JLabel("Choose the correct answer to review essential Japanese grammar patterns.");
    private final JLabel progressLabel = new JLabel("0 / 0");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea questionArea = new JTextArea();
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JPanel resultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

    private final JPanel optionsPanel = new JPanel();
    private final JPanel answerCard = new JPanel(new BorderLayout());
    private final AnimatedCardPanel animatedCardPanel = new AnimatedCardPanel();

    private JButton previousButton;
    private JButton nextButton;
    private JButton submitButton;
    private JButton themeToggleButton;

    private final Map<String, String> answersByQuestionId = new HashMap<>();
    private final List<OptionButton> optionButtons = new ArrayList<>();
    private final List<ResultChip> resultChips = new ArrayList<>();

    private GrammarQuestion current;
    private int currentIndex = 0;
    private OptionButton selectedOption;

    public GrammarQuizFrame(MenuNavigator navigator) {
        this.navigator = navigator;
        applyTheme(darkTheme);
        setTitle("Grammar Quiz");
        setupFrame();
        loadQuestions();
        setupUI();
        setVisible(true);
    }

    private void loadQuestions() {
        questions = repo.loadQuestions();
        if (questions == null) questions = new ArrayList<>();
    }

    private void setupFrame() {
        setSize(980, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(860, 560));
    }

    private void setupUI() {
        setContentPane(new GradientPanel());
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, 22, 16, 22));
        add(root, BorderLayout.CENTER);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        refreshTheme();
        if (questions.isEmpty()) showEmptyState(); else showQuestion(0, false);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 2, 10, 2));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        badgeLabel.setOpaque(true);
        badgeLabel.setBorder(new EmptyBorder(6, 12, 6, 12));
        badgeLabel.setFont(latinBoldFont.deriveFont(12f));
        left.add(badgeLabel);
        left.add(Box.createVerticalStrut(10));
        titleLabel.setFont(japaneseBoldFont.deriveFont(26f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(titleLabel);
        left.add(Box.createVerticalStrut(6));
        subtitleLabel.setFont(latinFont.deriveFont(14f));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(subtitleLabel);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel card = createSurfaceCard();
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel questionBlock = new JPanel(new BorderLayout(0, 12));
        questionBlock.setOpaque(false);
        JLabel questionTag = new JLabel("Question", SwingConstants.LEFT);
        questionTag.setFont(latinBoldFont.deriveFont(12f));
        questionTag.setForeground(accentDark);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setOpaque(false);
        questionArea.setBorder(null);
        questionArea.setFocusable(false);
        questionArea.setFont(mixedTextFont.deriveFont(19f));
        questionArea.setForeground(textPrimary);
        questionBlock.add(questionTag, BorderLayout.NORTH);
        questionBlock.add(questionArea, BorderLayout.CENTER);
        JPanel answerShell = new JPanel(new BorderLayout());
        answerShell.setOpaque(false);
        JLabel answerTag = new JLabel("Select one answer", SwingConstants.LEFT);
        answerTag.setFont(latinBoldFont.deriveFont(12f));
        answerTag.setForeground(textSecondary);
        answerShell.add(answerTag, BorderLayout.NORTH);
        answerCard.setOpaque(true);
        answerCard.setBackground(cardBg);
        answerCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cardBorder, 1), new EmptyBorder(12, 12, 12, 12)));
        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        answerCard.add(optionsPanel, BorderLayout.CENTER);
        answerShell.add(answerCard, BorderLayout.CENTER);
        JPanel statusBlock = new JPanel();
        statusBlock.setOpaque(false);
        statusBlock.setLayout(new BoxLayout(statusBlock, BoxLayout.Y_AXIS));
        feedbackLabel.setFont(latinBoldFont.deriveFont(14f));
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackLabel.setForeground(textSecondary);
        resultPanel.setOpaque(false);
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setVisible(false);
        statusBlock.add(feedbackLabel);
        statusBlock.add(Box.createVerticalStrut(8));
        statusBlock.add(resultPanel);
        card.add(questionBlock, BorderLayout.NORTH);
        card.add(answerShell, BorderLayout.CENTER);
        card.add(statusBlock, BorderLayout.SOUTH);
        animatedCardPanel.setLayout(new BorderLayout());
        animatedCardPanel.setOpaque(false);
        animatedCardPanel.add(card, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        center.add(animatedCardPanel, gbc);
        return center;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 2, 0, 2));
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        previousButton = createGhostButton("Previous");
        nextButton = createGhostButton("Next");
        leftActions.add(previousButton);
        leftActions.add(nextButton);
        JPanel centerActions = new JPanel(new BorderLayout(10, 0));
        centerActions.setOpaque(false);
        progressLabel.setFont(latinBoldFont.deriveFont(13f));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(150, 10));
        centerActions.add(progressLabel, BorderLayout.WEST);
        centerActions.add(progressBar, BorderLayout.CENTER);
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        themeToggleButton = createGhostButton(darkTheme ? "Light Mode" : "Dark Mode");
        submitButton = createPrimaryButton("Submit");
        rightActions.add(themeToggleButton);
        rightActions.add(submitButton);
        footer.add(leftActions, BorderLayout.WEST);
        footer.add(centerActions, BorderLayout.CENTER);
        footer.add(rightActions, BorderLayout.EAST);
        previousButton.addActionListener(e -> goPrevious());
        nextButton.addActionListener(e -> goNext());
        submitButton.addActionListener(e -> submitQuiz());
        themeToggleButton.addActionListener(e -> toggleTheme());
        return footer;
    }

    private void applyTheme(boolean dark) {
        if (dark) {
            bgTop = new Color(0x0F172A); bgBottom = new Color(0x1E293B); cardBg = new Color(0xF8FAFC); cardBorder = new Color(0xD6E4F0);
            textPrimary = new Color(0x0F172A); textSecondary = new Color(0x475569); accent = new Color(0x0EA5E9); accentDark = new Color(0x0284C7);
            success = new Color(0x16A34A); danger = new Color(0xDC2626); neutral = new Color(0x94A3B8);
            optionBg = new Color(0xEEF4FB); optionHover = new Color(0xDBEAFE); optionSelectedBg = accent; optionCorrectBg = new Color(0xDCFCE7);
            optionWrongBg = new Color(0xFEE2E2); optionMutedBg = new Color(0xE2E8F0); badgeBg = new Color(0x1E293B); badgeFg = new Color(0xE2E8F0);
        } else {
            bgTop = new Color(0xF8FBFF); bgBottom = new Color(0xDBEEFF); cardBg = Color.WHITE; cardBorder = new Color(0xC7D7E6);
            textPrimary = new Color(0x0F172A); textSecondary = new Color(0x334155); accent = new Color(0x0369A1); accentDark = new Color(0x0C4A6E);
            success = new Color(0x166534); danger = new Color(0xB91C1C); neutral = new Color(0x64748B);
            optionBg = new Color(0xF4F8FC); optionHover = new Color(0xE0F2FE); optionSelectedBg = accent; optionCorrectBg = new Color(0xDCFCE7);
            optionWrongBg = new Color(0xFEE2E2); optionMutedBg = new Color(0xE2E8F0); badgeBg = new Color(0x0F172A); badgeFg = Color.WHITE;
        }
    }
    private void toggleTheme() { darkTheme = !darkTheme; applyTheme(darkTheme); refreshTheme(); }

    private void refreshTheme() {
        badgeLabel.setBackground(badgeBg); badgeLabel.setForeground(badgeFg);
        titleLabel.setForeground(darkTheme ? Color.WHITE : textPrimary);
        subtitleLabel.setForeground(darkTheme ? new Color(0xCBD5E1) : textSecondary);
        questionArea.setForeground(textPrimary);
        feedbackLabel.setForeground(submitted ? success : textSecondary);
        progressLabel.setForeground(darkTheme ? new Color(0xE2E8F0) : textPrimary);
        progressBar.setForeground(accent); progressBar.setBackground(darkTheme ? new Color(0x334155) : new Color(0xBFDBFE));
        answerCard.setBackground(cardBg); answerCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(cardBorder, 1), new EmptyBorder(12, 12, 12, 12)));
        if (themeToggleButton instanceof PillButton pb) { pb.applyTheme(darkTheme ? new Color(0xE2E8F0) : new Color(0xDBEAFE), textPrimary, darkTheme ? new Color(0xCBD5E1) : new Color(0xBFDBFE)); pb.setText(darkTheme ? "Light Mode" : "Dark Mode"); }
        if (previousButton instanceof PillButton pb) pb.applyTheme(new Color(0xE2E8F0), textPrimary, new Color(0xCBD5E1));
        if (nextButton instanceof PillButton pb) pb.applyTheme(new Color(0xE2E8F0), textPrimary, new Color(0xCBD5E1));
        if (submitButton instanceof PillButton pb) pb.applyTheme(accent, Color.WHITE, accentDark);
        for (OptionButton ob : optionButtons) ob.applyTheme();
        refreshResultPanel();
        repaint(); revalidate();
    }

    private void showEmptyState() {
        titleLabel.setText("No questions available");
        subtitleLabel.setText("Add grammar questions to `src/main/resources/data/grammar.json`.");
        questionArea.setText("No grammar questions found.");
        optionsPanel.removeAll();
        optionsPanel.add(makeEmptyStateLabel("Nothing to practice yet."));
        updateProgress(0, 0);
        previousButton.setEnabled(false); nextButton.setEnabled(false); submitButton.setEnabled(false);
        feedbackLabel.setText(" "); resultPanel.setVisible(false); animatedCardPanel.setAnimationProgress(1f);
        repaint();
    }

    private void showQuestion(int index, boolean animate) {
        if (index < 0 || index >= questions.size()) return;
        current = questions.get(index); currentIndex = index;
        titleLabel.setText("Grammar Exercise");
        subtitleLabel.setText("Choose the best answer, move back and forth, then submit when ready.");
        questionArea.setText(current.getQuestion());
        optionsPanel.removeAll(); optionButtons.clear(); selectedOption = null;
        if (current.getType() == GrammarQuestion.Type.MCQ) buildMcqOptions(current); else optionsPanel.add(makeEmptyStateLabel("This question type is not supported in MCQ mode."));
        restoreSelection();
        applyQuestionState(current);
        updateProgress(index + 1, questions.size());
        updateNavigationState();
        optionsPanel.revalidate(); optionsPanel.repaint();
        answerCard.revalidate(); answerCard.repaint();
        if (animate) animatedCardPanel.playEntrance(); else animatedCardPanel.setAnimationProgress(1f);
    }

    private void buildMcqOptions(GrammarQuestion question) {
        List<String> options = question.getOptions() == null ? Collections.emptyList() : question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            String opt = options.get(i);
            OptionButton b = new OptionButton(opt);
            b.setFont(japaneseFont.deriveFont(18f));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            b.addActionListener(e -> selectOption(b));
            optionsPanel.add(b); if (i < options.size() - 1) optionsPanel.add(Box.createVerticalStrut(10));
            optionButtons.add(b);
        }
    }

    private void selectOption(OptionButton optionButton) {
        if (submitted) clearSubmissionState();
        if (selectedOption != null) selectedOption.setVisualState(OptionState.DEFAULT);
        selectedOption = optionButton; selectedOption.setVisualState(OptionState.SELECTED);
        saveCurrentAnswer();
        if (submitted) applyQuestionState(current);
    }

    private void restoreSelection() {
        String saved = getSavedAnswer(current); if (saved == null || saved.isEmpty()) return;
        for (OptionButton b : optionButtons) if (saved.equals(b.getActionCommand())) { selectedOption = b; b.setVisualState(OptionState.SELECTED); break; }
    }

    private void updateProgress(int current, int total) {
        if (total <= 0) { progressLabel.setText("0 / 0"); progressBar.setMinimum(0); progressBar.setMaximum(1); progressBar.setValue(0); return; }
        progressLabel.setText(current + " / " + total); progressBar.setMinimum(0); progressBar.setMaximum(total); progressBar.setValue(current);
    }

    private void updateNavigationState() { previousButton.setEnabled(currentIndex > 0); nextButton.setEnabled(currentIndex < questions.size() - 1); }
    private void goPrevious() { saveCurrentAnswer(); if (!questions.isEmpty() && currentIndex > 0) showQuestion(currentIndex - 1, true); }
    private void goNext() { saveCurrentAnswer(); if (!questions.isEmpty() && currentIndex < questions.size() - 1) showQuestion(currentIndex + 1, true); }

    private void saveCurrentAnswer() {
        if (current == null || current.getId() == null) return;
        String answer = selectedOption == null ? "" : selectedOption.getActionCommand();
        if (answer == null || answer.isBlank()) answersByQuestionId.remove(current.getId()); else answersByQuestionId.put(current.getId(), answer);
    }

    private String getSavedAnswer(GrammarQuestion question) {
        if (question == null || question.getId() == null) return "";
        String saved = answersByQuestionId.get(question.getId()); return saved == null ? "" : saved;
    }

    private void submitQuiz() {
        saveCurrentAnswer(); if (questions.isEmpty()) return;
        int total = 0, attempted = 0;
        for (GrammarQuestion q : questions) { String chosen = getSavedAnswer(q); if (chosen != null && !chosen.isBlank()) attempted++; if (isCorrect(q, chosen)) total++; }
        submitted = true; lastScore = total; lastAttempted = attempted;
        feedbackLabel.setForeground(success);
        feedbackLabel.setText("Final score: " + total + " / " + questions.size() + "   |   Answered: " + attempted + " / " + questions.size());
        repo.saveLastScore(total);
        refreshResultPanel();
        applyQuestionState(current);
    }

    private void clearSubmissionState() { submitted = false; lastScore = 0; lastAttempted = 0; feedbackLabel.setForeground(textSecondary); feedbackLabel.setText("Answers changed. Submit again to refresh results."); refreshResultPanel(); }
    private boolean isCorrect(GrammarQuestion q, String chosen) { return q != null && chosen != null && !chosen.isBlank() && chosen.trim().equalsIgnoreCase(q.getAnswer() == null ? "" : q.getAnswer().trim()); }

    private void applyQuestionState(GrammarQuestion q) {
        if (q == null) return;
        if (!submitted) { for (OptionButton b : optionButtons) b.setVisualState(b == selectedOption ? OptionState.SELECTED : OptionState.DEFAULT); feedbackLabel.setForeground(textSecondary); refreshResultPanel(); return; }
        String correct = q.getAnswer() == null ? "" : q.getAnswer().trim(); String chosen = getSavedAnswer(q); boolean answered = chosen != null && !chosen.isBlank(); boolean ok = answered && chosen.trim().equalsIgnoreCase(correct);
        for (OptionButton b : optionButtons) { String opt = b.getActionCommand(); if (opt.equalsIgnoreCase(correct)) b.setVisualState(OptionState.CORRECT); else if (opt.equalsIgnoreCase(chosen) && !ok) b.setVisualState(OptionState.WRONG); else b.setVisualState(OptionState.MUTED); }
        feedbackLabel.setForeground(ok ? success : danger);
        feedbackLabel.setText(ok ? "Correct answer selected." : answered ? "Wrong answer selected." : "No answer selected for this question.");
        refreshResultPanel();
    }

    private void refreshResultPanel() {
        resultPanel.removeAll(); resultChips.clear();
        if (!submitted) { resultPanel.setVisible(false); resultPanel.revalidate(); resultPanel.repaint(); return; }
        for (int i = 0; i < questions.size(); i++) { GrammarQuestion q = questions.get(i); String chosen = getSavedAnswer(q); boolean answered = chosen != null && !chosen.isBlank(); boolean ok = isCorrect(q, chosen); ResultChip chip = new ResultChip(i + 1, answered, ok); resultChips.add(chip); resultPanel.add(chip); }
        resultPanel.setVisible(true); resultPanel.revalidate(); resultPanel.repaint();
    }

    private JButton createPrimaryButton(String text) { return new PillButton(text, accent, Color.WHITE, accentDark, true); }
    private JButton createGhostButton(String text) { return new PillButton(text, new Color(0xE2E8F0), textPrimary, new Color(0xCBD5E1), false); }
    private JLabel makeEmptyStateLabel(String text) { JLabel l = new JLabel(text, SwingConstants.CENTER); l.setFont(latinFont.deriveFont(14f)); l.setForeground(textSecondary); l.setBorder(new EmptyBorder(24, 12, 24, 12)); return l; }
    private Font resolveJapaneseFont() {
        String[] c = {"Yu Gothic UI", "Meiryo", "MS Gothic", "Noto Sans CJK JP", "Dialog"};
        String[] a = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String x : c) for (String f : a) if (f.equalsIgnoreCase(x)) return new Font(f, Font.PLAIN, 16);
        return new Font("Dialog", Font.PLAIN, 16);
    }

    private Font resolveMixedTextFont() {
        String[] c = {"Segoe UI", "Noto Sans", "Noto Sans UI", "Arial Unicode MS", "Dialog"};
        String[] a = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String x : c) for (String f : a) if (f.equalsIgnoreCase(x)) return new Font(f, Font.PLAIN, 16);
        return new Font("Dialog", Font.PLAIN, 16);
    }
    private Color mix(Color a, Color b, double t) { t = Math.max(0, Math.min(1, t)); return new Color((int)Math.round(a.getRed()*(1-t)+b.getRed()*t), (int)Math.round(a.getGreen()*(1-t)+b.getGreen()*t), (int)Math.round(a.getBlue()*(1-t)+b.getBlue()*t)); }
    private class GradientPanel extends JPanel {
        GradientPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, bgTop, 0, h, bgBottom));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, darkTheme ? 14 : 30));
            g2.fillOval(w - 220, -80, 260, 260);
            g2.fillOval(-110, h - 180, 260, 260);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private JPanel createSurfaceCard() { return new ShadowCard(); }

    private class ShadowCard extends JPanel {
        ShadowCard() { setOpaque(false); setBackground(cardBg); setBorder(BorderFactory.createLineBorder(cardBorder, 1, true)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(15, 23, 42, darkTheme ? 24 : 12)); g2.fillRoundRect(6, 8, w - 12, h - 12, 28, 28);
            g2.setColor(cardBg); g2.fillRoundRect(0, 0, w - 12, h - 12, 28, 28);
            g2.setColor(new Color(255, 255, 255, darkTheme ? 120 : 180)); g2.drawRoundRect(0, 0, w - 13, h - 13, 28, 28);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private class PillButton extends JButton {
        private Color normalBg, normalFg, hoverBg; private final boolean primary; private boolean selectedState;
        PillButton(String text, Color background, Color foreground, Color hover, boolean primary) {
            super(text); this.normalBg = background; this.normalFg = foreground; this.hoverBg = hover; this.primary = primary;
            setActionCommand(text); setFont(latinBoldFont.deriveFont(14f)); setForeground(foreground); setBackground(background);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(14, 20, 14, 20)); setHorizontalAlignment(SwingConstants.CENTER); setMargin(new Insets(12, 18, 12, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (!selectedState) { setBackground(hoverBg); repaint(); } }
                @Override public void mouseExited(MouseEvent e) { if (!selectedState) { setBackground(normalBg); repaint(); } }
            });
        }
        void applyTheme(Color background, Color foreground, Color hover) { normalBg = background; normalFg = foreground; hoverBg = hover; if (!selectedState) { setBackground(background); setForeground(foreground); } repaint(); }
        void setSelectedState(boolean selected) { selectedState = selected; if (selected) { setBackground(primary ? accentDark : mix(normalBg, accent, 0.35)); setForeground(Color.WHITE); } else { setBackground(normalBg); setForeground(normalFg); } repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(); Color fill = getBackground();
            if (isEnabled()) { g2.setColor(new Color(15, 23, 42, selectedState ? 42 : 18)); g2.fillRoundRect(4, 5, w - 8, h - 8, 22, 22); }
            g2.setColor(fill); g2.fillRoundRect(0, 0, w - 8, h - 8, 22, 22);
            g2.setColor(selectedState ? new Color(255, 255, 255, 90) : new Color(255, 255, 255, 120)); g2.drawRoundRect(0, 0, w - 9, h - 9, 22, 22);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private enum OptionState { DEFAULT, SELECTED, CORRECT, WRONG, MUTED }

    private class OptionButton extends JButton {
        private OptionState state = OptionState.DEFAULT; private final Color baseFg = textPrimary; private Color baseBg = optionBg; private Color hoverBg = optionHover;
        OptionButton(String text) {
            super(text); setActionCommand(text); setFont(japaneseFont.deriveFont(18f)); setForeground(baseFg); setBackground(baseBg);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setMargin(new Insets(12, 16, 12, 16)); setBorder(new EmptyBorder(12, 16, 12, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (state == OptionState.DEFAULT) { setBackground(hoverBg); repaint(); } }
                @Override public void mouseExited(MouseEvent e) { if (state == OptionState.DEFAULT) { setBackground(baseBg); repaint(); } }
            });
        }
        void applyTheme() { baseBg = optionBg; hoverBg = optionHover; if (state == OptionState.DEFAULT) { setBackground(baseBg); setForeground(baseFg); } else { setVisualState(state); } }
        void setVisualState(OptionState s) {
            state = s;
            switch (s) {
                case DEFAULT -> { setBackground(baseBg); setForeground(baseFg); }
                case SELECTED -> { setBackground(optionSelectedBg); setForeground(Color.WHITE); }
                case CORRECT -> { setBackground(optionCorrectBg); setForeground(new Color(0x166534)); }
                case WRONG -> { setBackground(optionWrongBg); setForeground(new Color(0xB91C1C)); }
                case MUTED -> { setBackground(optionMutedBg); setForeground(neutral); }
            }
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(); Color fill = getBackground();
            g2.setColor(new Color(15, 23, 42, state == OptionState.SELECTED ? 32 : 14)); g2.fillRoundRect(6, 6, w - 12, h - 12, 22, 22);
            g2.setColor(fill); g2.fillRoundRect(0, 0, w - 12, h - 12, 22, 22);
            g2.setColor(state == OptionState.SELECTED ? new Color(255, 255, 255, 100) : new Color(255, 255, 255, 125)); g2.drawRoundRect(0, 0, w - 13, h - 13, 22, 22);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private class ResultChip extends JLabel {
        ResultChip(int index, boolean answered, boolean correct) {
            super("Q" + index + " " + (answered ? (correct ? "✓" : "✕") : "•"), SwingConstants.CENTER);
            setOpaque(true); setBorder(new EmptyBorder(4, 8, 4, 8)); setFont(latinBoldFont.deriveFont(11f));
            if (!answered) { setBackground(optionMutedBg); setForeground(textSecondary); }
            else if (correct) { setBackground(optionCorrectBg); setForeground(new Color(0x166534)); }
            else { setBackground(optionWrongBg); setForeground(new Color(0xB91C1C)); }
        }
    }

    private class AnimatedCardPanel extends JPanel {
        private float animationProgress = 1f; private Timer animationTimer;
        AnimatedCardPanel() { setOpaque(false); }
        void setAnimationProgress(float progress) { animationProgress = Math.max(0f, Math.min(1f, progress)); repaint(); }
        void playEntrance() { if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop(); animationProgress = 0f; animationTimer = new Timer(16, e -> { animationProgress += 0.08f; if (animationProgress >= 1f) { animationProgress = 1f; animationTimer.stop(); } repaint(); }); animationTimer.start(); }
        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animationProgress)); g2.translate(0, (1f - animationProgress) * 18f); super.paintChildren(g2); g2.dispose();
        }
    }
}











