package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.Vocabulary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/*
    1. hiển thị data
    2. nhận input
    3. phát sự kiện (event)
 */
public class VocabQuizFrame extends JFrame implements VocabQuizFrame_Interface {
    private final com.mescode.japanese.app.context.AppContext appContext;

    private Color background;
    private Color panelBackground;
    private Color border;
    private final boolean showKanji;

    private final JLabel wordLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("Score: 0");
    private final JLabel correctAnswerLabel = new JLabel(" ", SwingConstants.CENTER);

    private final JTextField romajiField = new JTextField();
    private final JTextField meaningField = new JTextField();

    private final JButton showAnswerButton = new JButton("Show Answer");

    private final JPanel bottomPanel = new JPanel();

    public VocabQuizFrame(com.mescode.japanese.app.navigation.MenuNavigator navigator) {
        this(navigator, false);
    }

    public VocabQuizFrame(com.mescode.japanese.app.navigation.MenuNavigator navigator, boolean showKanji) {
        this.appContext = navigator.getAppContext();
        this.showKanji = showKanji;
        // register for theme changes
        appContext.addThemeListener(isDark -> SwingUtilities.invokeLater(this::refreshTheme));
        setupFrame();
        refreshTheme();
        setupComponents();
        addComponentsToFrame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refreshTheme() {
        boolean dark = appContext.isDarkMode();
        background = dark ? new Color(0x0F172A) : new Color(0xE0F6FF);
        panelBackground = dark ? new Color(0x1E293B) : Color.WHITE;
        border = dark ? new Color(0x334155) : new Color(0xB3D9E6);
        if (getContentPane() != null) getContentPane().setBackground(background);
        repaint();
    }

    private void setupFrame() {
        setTitle(showKanji ? "Kanji Quiz" : "Vocabulary Quiz");
        getContentPane().setBackground(background);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
    }

    private void setupComponents() {
        wordLabel.setFont(new Font("Yu Mincho", Font.BOLD, 44));
        wordLabel.setForeground(new Color(0x111827));

        scoreLabel.setFont(new Font("Arial", Font.BOLD, 13));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 15));
        resultLabel.setForeground(new Color(0xB91C1C));

        correctAnswerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        correctAnswerLabel.setForeground(new Color(0x111827));

        romajiField.setFont(new Font("Arial", Font.BOLD, 20));
        romajiField.setBackground(new Color(0xF1F5F9));
        // cho phép xuống field khi nhấn enter
        romajiField.addActionListener(e -> meaningField.requestFocus());

        meaningField.setFont(new Font("Arial", Font.BOLD, 20));
        meaningField.setBackground(new Color(0xF1F5F9));

        showAnswerButton.setPreferredSize(new Dimension(120, 34));
        showAnswerButton.setBackground(new Color(0xD4EAFF));
        showAnswerButton.setBorder(BorderFactory.createLineBorder(border, 1));
        showAnswerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void addComponentsToFrame() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(420, 130));
        topPanel.setBackground(panelBackground);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        topPanel.add(wordLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(panelBackground);
        centerPanel.setBorder(BorderFactory.createLineBorder(border, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel romajiLabel = new JLabel("Romaji");
        romajiLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(romajiLabel, gbc);

        gbc.gridy = 1;
        centerPanel.add(romajiField, gbc);

        JLabel meaningLabel = new JLabel("Meaning");
        meaningLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridy = 2;
        centerPanel.add(meaningLabel, gbc);

        gbc.gridy = 3;
        centerPanel.add(meaningField, gbc);

        bottomPanel.setPreferredSize(new Dimension(420, 160));
        bottomPanel.setBackground(panelBackground);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        addComponentToBottom(scoreLabel);

        resultLabel.setPreferredSize(new Dimension(400, 24));
        resultLabel.setMaximumSize(new Dimension(400, 24));
        addComponentToBottom(resultLabel);

        correctAnswerLabel.setPreferredSize(new Dimension(400, 24));
        correctAnswerLabel.setMaximumSize(new Dimension(400, 24));
        addComponentToBottom(correctAnswerLabel);

        addComponentToBottom(showAnswerButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addComponentToBottom(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(component);
    }

    @Override
    public void showVocab(Vocabulary vocab) {
        String displayText = showKanji && hasText(vocab.getKanji()) ? vocab.getKanji() : vocab.getKana();
        wordLabel.setText(displayText);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public String getRomajiInput() {
        return romajiField.getText().trim().toLowerCase();
    }

    @Override
    public String getMeaningInput() {
        return meaningField.getText().trim().toLowerCase();
    }

    @Override
    public void resetInput() {
        romajiField.setText("");
        meaningField.setText("");
        romajiField.requestFocusInWindow();
    }

    @Override
    public void showResult(String result) {
        resultLabel.setText(result);
    }

    @Override
    public void resetResult() {
        resultLabel.setText("");
    }

    @Override
    public void showCorrectAnswer(String answer) {
        correctAnswerLabel.setText(answer);
    }

    @Override
    public void resetCorrectAnswer() {
        correctAnswerLabel.setText("");
    }

    @Override
    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    @Override
    public void setOnSubmit(Runnable action) {
        meaningField.addActionListener(e -> action.run());
    }

    @Override
    public void setOnShowAnswer(Runnable action) {
        showAnswerButton.addActionListener(e -> action.run());
    }
}



