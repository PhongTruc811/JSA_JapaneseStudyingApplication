package com.mescode.japanese.view.kana;

import com.mescode.japanese.model.kana.KanaQuizDifficulty;
import com.mescode.japanese.model.kana.KanaQuizCountdown;
import com.mescode.japanese.model.kana.KanaQuizGroup;
import com.mescode.japanese.model.kana.KanaQuizOptions;
import com.mescode.japanese.util.theme.UITheme;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class KanaDifficultyDialog extends JDialog {
    private final boolean darkMode;
    private final Color panel;
    private final Color card;
    private final Color border;
    private final Color hover;
    private final Color pressed;
    private final Color title;
    private final Color text;
    private final Color accent;
    private KanaQuizDifficulty selectedDifficulty;
    private KanaDifficultyDialog(JFrame owner, boolean darkMode) {
        super(owner, true);
        this.darkMode = darkMode;
        panel = UITheme.getPanelBackground(darkMode);
        card = UITheme.getCardBackground(darkMode);
        border = UITheme.getBorder(darkMode);
        hover = UITheme.getHover(darkMode);
        pressed = UITheme.getPressed(darkMode);
        title = UITheme.getTitleForeground(darkMode);
        text = UITheme.getTextForeground(darkMode);
        accent = UITheme.getAccent(darkMode);
        setupDialog();
        setupUI();
    }

    public static KanaQuizDifficulty showDialog(JFrame owner, boolean darkMode) {
        KanaDifficultyDialog dialog = new KanaDifficultyDialog(owner, darkMode);
        dialog.setVisible(true);
        return dialog.selectedDifficulty;
    }

    public static KanaQuizOptions showOptionsDialog(JFrame owner, boolean darkMode) {
        KanaDifficultyDialog dialog = new KanaDifficultyDialog(owner, darkMode);
        dialog.setVisible(true);
        return dialog.selectedDifficulty == null
                ? null
                : new KanaQuizOptions(dialog.selectedDifficulty, KanaQuizGroup.ALL);
    }

    private void setupDialog() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(540, 500);
        setLocationRelativeTo(getOwner());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private void setupUI() {
        DialogCard content = new DialogCard();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 30, 30, 30));
        setContentPane(content);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel badge = new JLabel("KANA QUIZ");
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        badge.setForeground(accent);
        JButton close = new JButton("×");
        close.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        close.setForeground(text);
        close.setBorder(null);
        close.setContentAreaFilled(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dispose());
        header.add(badge, BorderLayout.WEST);
        header.add(close, BorderLayout.EAST);
        content.add(header);
        content.add(Box.createVerticalStrut(8));

        JLabel heading = label("Set up your quiz", 26, Font.BOLD, title);
        JLabel subtitle = label("Choose a difficulty for this quiz.", 14, Font.PLAIN, text);
        content.add(heading);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(18));

        JLabel difficultyHeading = label("DIFFICULTY", 12, Font.BOLD, accent);
        content.add(difficultyHeading);
        content.add(Box.createVerticalStrut(8));

        content.add(new DifficultyOption(KanaQuizDifficulty.EASY,
                "A relaxed pace with answer reveal."));
        content.add(Box.createVerticalStrut(10));
        content.add(new DifficultyOption(KanaQuizDifficulty.MEDIUM,
                "A quicker challenge with answer reveal."));
        content.add(Box.createVerticalStrut(10));
        content.add(new DifficultyOption(KanaQuizDifficulty.HARD,
                "A fast challenge without answer reveal."));
    }

    private JLabel label(String value, int size, int style, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void select(KanaQuizDifficulty difficulty) {
        selectedDifficulty = difficulty;
        dispose();
    }

    private class DialogCard extends JPanel {
        DialogCard() {
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(15, 23, 42, darkMode ? 100 : 55));
            g2.fillRoundRect(9, 11, getWidth() - 18, getHeight() - 18, 30, 30);
            g2.setColor(panel);
            g2.fillRoundRect(0, 0, getWidth() - 11, getHeight() - 11, 30, 30);
            g2.setColor(darkMode ? new Color(255, 255, 255, 22) : border);
            g2.drawRoundRect(0, 0, getWidth() - 12, getHeight() - 12, 30, 30);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class DifficultyOption extends JButton {
        private final KanaQuizDifficulty difficulty;

        DifficultyOption(KanaQuizDifficulty difficulty, String description) {
            this.difficulty = difficulty;
            setLayout(new BorderLayout(16, 0));
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(13, 16, 13, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
            setPreferredSize(new Dimension(1, 82));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            DifficultyMeter meter = new DifficultyMeter(difficulty);
            add(meter, BorderLayout.WEST);

            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
            copy.add(label(difficulty.getDisplayName(), 17, Font.BOLD, title));
            JLabel descriptionLabel = label(description, 12, Font.PLAIN, text);
            descriptionLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
            copy.add(descriptionLabel);
            add(copy, BorderLayout.CENTER);

            String duration = KanaQuizCountdown.formatTime(difficulty.getDurationSeconds());
            JLabel time = new JLabel(duration, SwingConstants.CENTER);
            time.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
            time.setForeground(accent);
            time.setPreferredSize(new Dimension(58, 28));
            add(time, BorderLayout.EAST);
            addActionListener(e -> select(this.difficulty));
            forwardClicks(meter);
            forwardClicks(copy);
            forwardClicks(time);
        }

        private void forwardClicks(Component component) {
            component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            component.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    getModel().setRollover(true);
                    repaint();
                }

                @Override public void mouseExited(MouseEvent e) {
                    Point point = SwingUtilities.convertPoint(component, e.getPoint(), DifficultyOption.this);
                    if (!contains(point)) {
                        getModel().setRollover(false);
                        repaint();
                    }
                }

                @Override public void mouseClicked(MouseEvent e) {
                    doClick();
                }
            });
            if (component instanceof Container container) {
                for (Component child : container.getComponents()) {
                    forwardClicks(child);
                }
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isPressed() ? pressed : getModel().isRollover() ? hover : card;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(getModel().isRollover() ? accent : border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }

        private class DifficultyMeter extends JComponent {
            private final int activeBars;
            private final Color activeColor;

            DifficultyMeter(KanaQuizDifficulty difficulty) {
                activeBars = switch (difficulty) {
                    case EASY -> 1;
                    case MEDIUM -> 2;
                    case HARD -> 3;
                };
                activeColor = switch (difficulty) {
                    case EASY -> new Color(0x22C55E);
                    case MEDIUM -> new Color(0xF59E0B);
                    case HARD -> new Color(0xEF4444);
                };
                setPreferredSize(new Dimension(46, 46));
                setMinimumSize(new Dimension(46, 46));
                setMaximumSize(new Dimension(46, 46));
                setOpaque(false);
            }

            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(darkMode ? new Color(255, 255, 255, 16) : new Color(15, 23, 42, 14));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 15, 15);

                int[] heights = {12, 20, 28};
                int barWidth = 7;
                int gap = 4;
                int totalWidth = (barWidth * 3) + (gap * 2);
                int startX = (getWidth() - totalWidth) / 2;
                int baseline = 36;
                boolean highlighted = getModel().isRollover();

                for (int i = 0; i < heights.length; i++) {
                    int x = startX + i * (barWidth + gap);
                    int y = baseline - heights[i];
                    if (i < activeBars) {
                        g2.setColor(highlighted ? activeColor.brighter() : activeColor);
                    } else {
                        g2.setColor(darkMode
                                ? new Color(148, 163, 184, 70)
                                : new Color(100, 116, 139, 65));
                    }
                    g2.fillRoundRect(x, y, barWidth, heights[i], 5, 5);
                }
                g2.dispose();
            }
        }
    }

}
