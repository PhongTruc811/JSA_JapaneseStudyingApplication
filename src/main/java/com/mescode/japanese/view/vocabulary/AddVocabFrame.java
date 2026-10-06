package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.app.navigation.AppNavigator;
import com.mescode.japanese.app.navigation.AppRoute;
import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.repository.VocabRepository;

import javax.swing.*;
import java.awt.*;

public class AddVocabFrame extends JFrame {
    private final AppNavigator navigator;
    private final AppContext appContext;

    private final JTextField kanaField = new JTextField();
    private final JTextField kanjiField = new JTextField();
    private final JTextField romajiField = new JTextField();
    private final JTextField meaningField = new JTextField();
    private final JTextArea exampleArea = new JTextArea(3, 20);

    public AddVocabFrame(AppNavigator navigator) {
        this.navigator = navigator;
        this.appContext = navigator.getAppContext();

        setTitle("Add Vocabulary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(480, 360);
        setLocationRelativeTo(null);

        JPanel form = new JPanel();
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(labeledField("Kana:", kanaField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledField("Kanji:", kanjiField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledField("Romaji:", romajiField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledField("Meaning:", meaningField));
        form.add(Box.createVerticalStrut(8));

        JPanel examplePanel = new JPanel(new BorderLayout());
        examplePanel.setBackground(Color.WHITE);
        JLabel exLabel = new JLabel("Example (optional):");
        examplePanel.add(exLabel, BorderLayout.NORTH);
        exampleArea.setLineWrap(true);
        exampleArea.setWrapStyleWord(true);
        examplePanel.add(new JScrollPane(exampleArea), BorderLayout.CENTER);
        form.add(examplePanel);
        form.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        actions.add(cancelBtn);
        actions.add(saveBtn);
        form.add(actions);

        add(form, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> navigator.navigateTo(AppRoute.Vocab));
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(80, 24));
        p.add(l, BorderLayout.WEST);
        field.setPreferredSize(new Dimension(300, 24));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void onSave() {
        String kana = kanaField.getText().trim();
        String kanji = kanjiField.getText().trim();
        String romaji = romajiField.getText().trim();
        String meaning = meaningField.getText().trim();
        String example = exampleArea.getText().trim();

        if (kana.isEmpty() || romaji.isEmpty() || meaning.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill kana, romaji and meaning.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vocabulary v = new Vocabulary(kana, kanji.isEmpty() ? null : kanji, meaning, romaji, example);
        try {
            VocabRepository repo = new VocabRepository();
            repo.addCustomVocab(v);
            appContext.getVocabs().add(v);
            JOptionPane.showMessageDialog(this, "Vocabulary saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            navigator.navigateTo(AppRoute.Vocab);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save vocabulary: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
