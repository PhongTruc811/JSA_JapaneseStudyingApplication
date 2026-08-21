package com.mescode.japanese.view.components;

import com.mescode.japanese.view.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;

public class MenuActionCard extends JPanel {
    private static final int MENU_ITEM_HEIGHT = 64;

    private final JLabel arrow;
    private final Runnable action;
    private final BooleanSupplier darkModeSupplier;
    private boolean hovered;
    private boolean pressedState;

    public MenuActionCard(String title, String description, JComponent mark, boolean darkMode, Runnable action) {
        this(title, description, mark, () -> darkMode, action);
    }

    public MenuActionCard(String title, String description, JComponent mark,
                          BooleanSupplier darkModeSupplier, Runnable action) {
        this.action = action;
        this.darkModeSupplier = darkModeSupplier == null ? () -> false : darkModeSupplier;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setLayout(new BorderLayout(16, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, MENU_ITEM_HEIGHT));
        setPreferredSize(new Dimension(1, MENU_ITEM_HEIGHT));

        add(mark, BorderLayout.WEST);

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.getTitleForeground(isDarkMode()));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(UITheme.getTextForeground(isDarkMode()));
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textWrap.add(titleLabel);
        textWrap.add(descriptionLabel);
        add(textWrap, BorderLayout.CENTER);

        arrow = new JLabel("›", SwingConstants.CENTER);
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
        arrow.setForeground(defaultArrowColor());
        arrow.setPreferredSize(new Dimension(24, 28));
        add(arrow, BorderLayout.EAST);

        attachInteraction(this);
        updateHoverState();
    }

    private void attachInteraction(Component component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                updateHoverState();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(component, e.getPoint(), MenuActionCard.this);
                if (!contains(point)) {
                    hovered = false;
                    pressedState = false;
                    updateHoverState();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressedState = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressedState = false;
                Point point = SwingUtilities.convertPoint(component, e.getPoint(), MenuActionCard.this);
                hovered = contains(point);
                updateHoverState();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run();
                }
            }
        });

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                attachInteraction(child);
            }
        }
    }

    private boolean isDarkMode() {
        return darkModeSupplier.getAsBoolean();
    }

    private Color defaultArrowColor() {
        return isDarkMode() ? new Color(0x94A3B8) : new Color(0x64748B);
    }

    private void updateHoverState() {
        arrow.setForeground(hovered ? UITheme.getAccent(isDarkMode()) : defaultArrowColor());
        arrow.setBorder(BorderFactory.createEmptyBorder(0, hovered ? 6 : 0, 0, 0));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean darkMode = isDarkMode();
        Color cardBackground = darkMode ? new Color(0x243249) : UITheme.getCardBackground(false);
        Color hover = UITheme.getHover(darkMode);
        Color pressed = UITheme.getPressed(darkMode);
        Color border = UITheme.getBorder(darkMode);
        Color accent = UITheme.getAccent(darkMode);
        int w = getWidth();
        int h = getHeight();

        Color fill = pressedState ? pressed : hovered ? blend(hover, cardBackground, 0.32f) : cardBackground;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, 18, 18);
        g2.setColor(hovered ? accent : border);
        g2.setStroke(new BasicStroke(hovered ? 1.4f : 1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);
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

