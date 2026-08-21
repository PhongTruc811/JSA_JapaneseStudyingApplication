package com.mescode.japanese.view.petrial;

import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class PeTrialDifficultyDialog {
    private PeTrialDifficultyDialog() {
    }

    public static PeTrialConfig showDialog(Component parent, boolean darkMode) {
        Color background = UITheme.getPanelBackground(darkMode);
        Color title = UITheme.getTitleForeground(darkMode);
        Color text = UITheme.getTextForeground(darkMode);
        Color accent = UITheme.getAccent(darkMode);

        JPanel content = new JPanel();
        content.setBackground(background);
        content.setBorder(new EmptyBorder(12, 14, 8, 14));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Choose PE Trial mode");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(title);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("30 questions in the original exam order.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(text);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel easy = modeLabel("Easy", "Check each answer while practising. No time limit.", accent, title);
        JLabel hard = modeLabel("Hard", "40-minute countdown. No answer checking.", accent, title);

        content.add(heading);
        content.add(Box.createVerticalStrut(6));
        content.add(description);
        content.add(Box.createVerticalStrut(18));
        content.add(easy);
        content.add(Box.createVerticalStrut(10));
        content.add(hard);

        Object[] options = {"Start Easy", "Start Hard", "Cancel"};
        int result = JOptionPane.showOptionDialog(
                parent,
                content,
                "PE Trial setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        return switch (result) {
            case 0 -> PeTrialConfig.EASY;
            case 1 -> PeTrialConfig.HARD;
            default -> null;
        };
    }

    private static JLabel modeLabel(String mode, String detail, Color accent, Color title) {
        JLabel label = new JLabel("<html><b><font color='#"
                + hex(accent) + "'>" + mode + "</font></b><br>" + detail + "</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(title);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static String hex(Color color) {
        return "%02x%02x%02x".formatted(color.getRed(), color.getGreen(), color.getBlue());
    }
}
