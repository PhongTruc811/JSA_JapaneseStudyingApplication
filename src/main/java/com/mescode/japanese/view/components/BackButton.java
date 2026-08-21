package com.mescode.japanese.view.components;

import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BackButton extends JPanel {
    private static final int WIDTH = 118;
    private static final int HEIGHT = 38;

    private final JButton button;
    private final Color panelBackground;
    private final Color cardBackground;
    private final Color border;
    private final Color hover;
    private final Color pressed;
    private final Color textForeground;
    private final Color accent;

    private boolean hovered;
    private boolean pressedState;


    public BackButton(boolean darkMode, Runnable action) {
        this("‹  Back", darkMode, action);
    }

    public BackButton(String text, boolean darkMode, Runnable action) {
        panelBackground = UITheme.getPanelBackground(darkMode);
        cardBackground = darkMode ? new Color(0x243249) : UITheme.getCardBackground(false);
        border = UITheme.getBorder(darkMode);
        hover = UITheme.getHover(darkMode);
        pressed = UITheme.getPressed(darkMode);
        textForeground = UITheme.getTextForeground(darkMode);
        accent = UITheme.getAccent(darkMode);

        setOpaque(false);
        setLayout(new BorderLayout());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setMaximumSize(new Dimension(WIDTH, HEIGHT));

        button = new JButton(text);
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setForeground(textForeground);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        button.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
        add(button, BorderLayout.CENTER);

        MouseAdapter stateListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressedState = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressedState = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressedState = false;
                hovered = contains(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), BackButton.this));
                repaint();
            }
        };
        addMouseListener(stateListener);
        button.addMouseListener(stateListener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = pressedState ? pressed : hovered ? blend(hover, cardBackground, 0.22f) : panelBackground;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.setColor(hovered ? accent : border);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.dispose();
    }

    private Color blend(Color top, Color bottom, float amount) {
        float keep = 1f - amount;
        int r = Math.round(top.getRed() * amount + bottom.getRed() * keep);
        int g = Math.round(top.getGreen() * amount + bottom.getGreen() * keep);
        int b = Math.round(top.getBlue() * amount + bottom.getBlue() * keep);
        return new Color(r, g, b);
    }


}

