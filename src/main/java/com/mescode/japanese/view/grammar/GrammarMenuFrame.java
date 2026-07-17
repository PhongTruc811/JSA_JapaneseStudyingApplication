package com.mescode.japanese.view.grammar;

import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GrammarMenuFrame extends JFrame {
    private final MenuNavigator menuNavigator;
    private final Font titleFont = new Font("Segoe UI Semibold", Font.BOLD, 30);
    private final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font buttonFont = new Font("Segoe UI Semibold", Font.BOLD, 15);

    private final Color bgTop = new Color(0x0F172A);
    private final Color bgBottom = new Color(0x1E293B);
    private final Color cardBg = new Color(0xF8FAFC);
    private final Color textPrimary = new Color(0x0F172A);
    private final Color textSecondary = new Color(0x475569);
    private final Color accent = new Color(0x0EA5E9);
    private final Color accentDark = new Color(0x0284C7);

    public GrammarMenuFrame(MenuNavigator nav) {
        this.menuNavigator = nav;
        setTitle("Grammar");
        setupFrame();
        setupUI();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(560, 420);
        setMinimumSize(new Dimension(520, 380));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        setContentPane(new GradientPanel());
    }

    private void setupUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        add(root, BorderLayout.CENTER);

        JPanel panel = new ShadowPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 30, 28, 30));
        panel.setMaximumSize(new Dimension(460, 300));

        JLabel badge = new JLabel("Grammar Exercise");
        badge.setOpaque(true);
        badge.setBackground(new Color(0x1E293B));
        badge.setForeground(new Color(0xE2E8F0));
        badge.setFont(buttonFont.deriveFont(12f));
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(badge);
        panel.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Practice Grammar");
        title.setFont(titleFont);
        title.setForeground(textPrimary);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));

        JLabel description = new JLabel("Review Japanese patterns with focused multiple-choice questions.");
        description.setFont(bodyFont);
        description.setForeground(textSecondary);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(description);
        panel.add(Box.createVerticalStrut(24));

        JButton start = createButton("Start Quiz", accent, Color.WHITE, accentDark, () -> menuNavigator.navigateTo(MenuOptions.GrammarQuiz));
        JButton back = createButton("Back", new Color(0xE2E8F0), textPrimary, new Color(0xCBD5E1), () -> menuNavigator.navigateTo(MenuOptions.MenuHome));
        panel.add(start);
        panel.add(Box.createVerticalStrut(10));
        panel.add(back);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        root.add(panel, gbc);
    }

    private JButton createButton(String text, Color normal, Color foreground, Color hover, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(buttonFont);
        button.setForeground(foreground);
        button.setBackground(normal);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.addActionListener(e -> action.run());
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hover); button.repaint(); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(normal); button.repaint(); }
        });
        return button;
    }

    private class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, bgTop, 0, h, bgBottom));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fillOval(w - 160, -70, 210, 210);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ShadowPanel extends JPanel {
        ShadowPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(15, 23, 42, 30));
            g2.fillRoundRect(7, 9, w - 14, h - 14, 18, 18);
            g2.setColor(cardBg);
            g2.fillRoundRect(0, 0, w - 12, h - 12, 18, 18);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, w - 13, h - 13, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

