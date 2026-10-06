package com.mescode.japanese.view.grammar;

import com.mescode.japanese.util.theme.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

final class GrammarUi {
    // Logical fonts are composite fonts: Vietnamese stays intact while Java
    // falls back to an installed CJK font for Japanese glyphs.
    private static final String MIXED_FONT_FAMILY = Font.SANS_SERIF;
    static final Font TITLE_FONT = new Font(MIXED_FONT_FAMILY, Font.BOLD, 32);
    static final Font HEADING_FONT = new Font(MIXED_FONT_FAMILY, Font.BOLD, 22);
    static final Font BODY_FONT = new Font(MIXED_FONT_FAMILY, Font.PLAIN, 14);
    static final Font SMALL_FONT = new Font(MIXED_FONT_FAMILY, Font.PLAIN, 12);
    static final Font BUTTON_FONT = new Font(MIXED_FONT_FAMILY, Font.BOLD, 14);
    static final Font JAPANESE_FONT = new Font(MIXED_FONT_FAMILY, Font.PLAIN, 19);

    private GrammarUi() {
    }

    static String html(String value, int width) {
        String safe = value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><div style='width:" + width + "px'>" + safe + "</div></html>";
    }

    static Color card(boolean dark) {
        return dark ? new Color(0x243249) : new Color(0xF7F9FC);
    }

    static Color softCard(boolean dark) {
        return dark ? new Color(0x1B293D) : new Color(0xE7EFF7);
    }

    static Color success(boolean dark) {
        return dark ? new Color(0x86EFAC) : new Color(0x166534);
    }

    static Color danger(boolean dark) {
        return dark ? new Color(0xFCA5A5) : new Color(0xB91C1C);
    }

    static class GradientPanel extends JPanel {
        private final Supplier<Boolean> darkSupplier;

        GradientPanel(Supplier<Boolean> darkSupplier) {
            this.darkSupplier = darkSupplier;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            boolean dark = darkSupplier.get();
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            Color top = dark ? new Color(0x0B1220) : new Color(0xD4E0EC);
            Color bottom = UITheme.getBackground(dark);
            g2.setPaint(new GradientPaint(0, 0, top, width, height, bottom));
            g2.fillRect(0, 0, width, height);
            g2.setComposite(AlphaComposite.SrcOver.derive(dark ? 0.10f : 0.08f));
            g2.setColor(UITheme.getGlow(dark));
            g2.fillOval(width - 260, -120, 340, 340);
            g2.setColor(UITheme.getAccent(dark));
            g2.fillOval(-140, height - 210, 300, 300);
            g2.dispose();
        }
    }

    static class SurfacePanel extends JPanel {
        private final Supplier<Boolean> darkSupplier;
        private final int radius;

        SurfacePanel(Supplier<Boolean> darkSupplier) {
            this(darkSupplier, 22);
        }

        SurfacePanel(Supplier<Boolean> darkSupplier, int radius) {
            this.darkSupplier = darkSupplier;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            boolean dark = darkSupplier.get();
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g2.setColor(new Color(15, 23, 42, dark ? 52 : 28));
            g2.fillRoundRect(7, 9, width - 14, height - 14, radius, radius);
            g2.setColor(card(dark));
            g2.fillRoundRect(0, 0, width - 10, height - 10, radius, radius);
            g2.setColor(UITheme.getBorder(dark));
            g2.drawRoundRect(0, 0, width - 11, height - 11, radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static class ActionButton extends JButton {
        private final Supplier<Boolean> darkSupplier;
        private final boolean primary;
        private boolean hovered;

        ActionButton(String text, Supplier<Boolean> darkSupplier, boolean primary) {
            super(text);
            this.darkSupplier = darkSupplier;
            this.primary = primary;
            setFont(BUTTON_FONT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(11, 18, 11, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            boolean dark = darkSupplier.get();
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            if (!isEnabled()) {
                fill = dark ? new Color(0x334155) : new Color(0xCBD5E1);
            } else if (primary) {
                fill = hovered ? UITheme.getPressed(dark) : UITheme.getAccent(dark);
            } else {
                fill = hovered ? UITheme.getHover(dark) : softCard(dark);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(primary ? fill : UITheme.getBorder(dark));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            setForeground(primary && isEnabled() ? Color.WHITE : UITheme.getTitleForeground(dark));
            super.paintComponent(graphics);
        }
    }

    static void stripScrollPane(javax.swing.JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
    }

}
