package com.mescode.japanese.view.theme;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/** Provides the kana app icon used by every application window. */
public final class AppIcon {
    private static final BufferedImage IMAGE = createImage();

    private AppIcon() {
    }

    /** Installs the icon once, before the application creates any windows. */
    public static void install() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() != WindowEvent.WINDOW_OPENED || !(event.getSource() instanceof Window window)) {
                return;
            }

            if (window instanceof Frame frame) {
                frame.setIconImage(IMAGE);
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    private static BufferedImage createImage() {
        int size = 256;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(0xFF6B18));
        graphics.fillRoundRect(16, 16, 224, 224, 56, 56);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Yu Gothic UI", Font.BOLD, 136));
        String kana = "あ";
        int x = (size - graphics.getFontMetrics().stringWidth(kana)) / 2;
        int y = (size - graphics.getFontMetrics().getHeight()) / 2 + graphics.getFontMetrics().getAscent();
        graphics.drawString(kana, x, y);
        graphics.dispose();
        return image;
    }
}
