package com.mescode.japanese.view.kana;

import com.mescode.japanese.model.Kana;
import com.mescode.japanese.model.Score;

import javax.swing.*;
import java.awt.*;

public class KanaQuizFrame extends JFrame implements KanaQuizFrame_Interface {
    // Score mặc định là 0
    Score score  = new Score();

    JLabel kanaLabel = new JLabel("", SwingConstants.CENTER);

    JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);

    JLabel scoreLabel = new JLabel("Score: 0");

    JTextField inputField = new JTextField();

    JButton checkButton = new JButton("Check");

    JLabel correctAnswerLabel = new JLabel(" ", SwingConstants.CENTER);

    JPanel bottomPanel = new JPanel(new BorderLayout());

    public KanaQuizFrame(com.mescode.japanese.app.navigation.MenuNavigator navigator) {
        com.mescode.japanese.app.context.AppContext appContext = navigator.getAppContext();
        appContext.addThemeListener(isDark -> javax.swing.SwingUtilities.invokeLater(() -> refreshTheme(isDark)));
        setup_Frame();
        refreshTheme(appContext.isDarkMode());

        setup_Component();

        add_Component_ToFrame();
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void refreshTheme(boolean dark) {
        Color bg = dark ? new Color(0x0F172A) : Color.LIGHT_GRAY;
        Color panelBg = dark ? new Color(0x1E293B) : Color.WHITE;
        this.getContentPane().setBackground(bg);
        // update components that used hard-coded colors
        inputField.setBackground(dark ? new Color(0x1F2D3D) : Color.LIGHT_GRAY);
        resultLabel.setForeground(dark ? new Color(0xF87171) : Color.RED);
        correctAnswerLabel.setForeground(dark ? new Color(0xE5E7EB) : Color.BLACK);
        this.repaint();
    }

    public void setup_Frame(){
        this.setTitle("Japanese Studying Application");
        //this.setSize(400,400);
        this.setBackground(Color.LIGHT_GRAY);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(0, 0));
    }

    public void setup_Component(){
        //hiraganaLabel.setText(currentKana.getKana());
        kanaLabel.setFont(new Font("Mei Ryo", Font.BOLD, 100));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 13));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 15));
        resultLabel.setForeground(Color.RED);

        correctAnswerLabel.setFont(new Font("Mei Ryo", Font.BOLD, 15));
        correctAnswerLabel.setForeground(Color.BLACK);

        inputField.setFont(new Font("Arial", Font.BOLD, 30));
        inputField.setBackground(Color.LIGHT_GRAY);

        checkButton.setPreferredSize(new Dimension(50, 30));
        checkButton.setMaximumSize(new Dimension(50, 30));
        checkButton.setBackground(Color.LIGHT_GRAY);
        checkButton.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        checkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    }
    public void add_Component_ToFrame(){
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(420, 155));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(kanaLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setPreferredSize(new Dimension(420, 155));
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        centerPanel.add(inputField, BorderLayout.CENTER);

        // bottom panl
        bottomPanel.setPreferredSize(new Dimension(420, 155));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); // bố trí các component con bên trong bottomPanel theo trục dọc Y

        // score label
        addComponents_ToPanel(scoreLabel);

        resultLabel.setPreferredSize(new Dimension(400, 30));resultLabel.setMaximumSize(new Dimension(400, 25));
        addComponents_ToPanel(resultLabel);

        correctAnswerLabel.setPreferredSize(new Dimension(400, 30));correctAnswerLabel.setMaximumSize(new Dimension(400, 25));
        addComponents_ToPanel(correctAnswerLabel);

        addComponents_ToPanel(checkButton);


        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
    }
    private void addComponents_ToPanel(JComponent component){
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        component.add(Box.createVerticalStrut(15));
        bottomPanel.add(component);
    }

    // Implements các method của QuizView Interface
    @Override
    public void showKana(Kana kana) {
        kanaLabel.setText(kana.getKana());
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
    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
    @Override
    public String getUserInput() {
        return inputField.getText().trim().toLowerCase();
    }
    @Override
    public void resetInput() {
        inputField.setText("");
    }

    @Override
    public void showCorrectAnswer(String correctHiragana) {
        correctAnswerLabel.setText(correctHiragana);
    }

    @Override
    public void resetCorrectAnswer() {
        correctAnswerLabel.setText("");
    }

    @Override
    public void setOnSubmit(Runnable action) {
        inputField.addActionListener(e -> action.run());
    }
    @Override
    public void setOnShowAnswer(Runnable action) {
        checkButton.addActionListener(e -> action.run());
    }

}
