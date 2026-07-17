package com.mescode.japanese.view.theme;

import java.awt.Color;
import java.awt.Font;

public final class UITheme {

    // Dark palette
    private static final Color DARK_BACKGROUND = new Color(0x0F172A);
    private static final Color DARK_PANEL = new Color(0x1E293B);
    private static final Color DARK_CARD = new Color(0x334155);
    private static final Color DARK_BORDER = new Color(0x475569);
    private static final Color DARK_HOVER = new Color(0x3B82F6);
    private static final Color DARK_PRESSED = new Color(0x2563EB);
    private static final Color DARK_TITLE = new Color(0xF8FAFC);
    private static final Color DARK_TEXT = new Color(0xE2E8F0);
    private static final Color DARK_ACCENT = new Color(0xF97316);
    private static final Color DARK_GLOW = new Color(0x38BDF8);

    // Light palette
    private static final Color LIGHT_BACKGROUND = new Color(0xF0F9FF);
    private static final Color LIGHT_PANEL = new Color(0xFFFFFF);
    private static final Color LIGHT_CARD = new Color(0xE0F2FE);
    private static final Color LIGHT_BORDER = new Color(0xBAE6FD);
    private static final Color LIGHT_HOVER = new Color(0x38BDF8);
    private static final Color LIGHT_PRESSED = new Color(0x0EA5E9);
    private static final Color LIGHT_TITLE = new Color(0x0F172A);
    private static final Color LIGHT_TEXT = new Color(0x1E293B);
    private static final Color LIGHT_ACCENT = new Color(0xEA580C);
    private static final Color LIGHT_GLOW = new Color(0x0EA5E9);

    // Fonts
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font CARD_TITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font CARD_ICON_FONT = new Font("Arial Emoji", Font.PLAIN, 32);
    private static final Font SECTION_FONT = new Font("Arial", Font.BOLD, 13);

    private UITheme() {}

    public static Color getBackground(boolean darkMode) {
        return darkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }

    public static Color getPanelBackground(boolean darkMode) {
        return darkMode ? DARK_PANEL : LIGHT_PANEL;
    }

    public static Color getCardBackground(boolean darkMode) {
        return darkMode ? DARK_CARD : LIGHT_CARD;
    }

    public static Color getBorder(boolean darkMode) {
        return darkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color getHover(boolean darkMode) {
        return darkMode ? DARK_HOVER : LIGHT_HOVER;
    }

    public static Color getPressed(boolean darkMode) {
        return darkMode ? DARK_PRESSED : LIGHT_PRESSED;
    }

    public static Color getTitleForeground(boolean darkMode) {
        return darkMode ? DARK_TITLE : LIGHT_TITLE;
    }

    public static Color getTextForeground(boolean darkMode) {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getAccent(boolean darkMode) {
        return darkMode ? DARK_ACCENT : LIGHT_ACCENT;
    }

    public static Color getGlow(boolean darkMode) {
        return darkMode ? DARK_GLOW : LIGHT_GLOW;
    }

    public static Font getTitleFont() {
        return TITLE_FONT;
    }

    public static Font getSubtitleFont() {
        return SUBTITLE_FONT;
    }

    public static Font getCardTitleFont() {
        return CARD_TITLE_FONT;
    }

    public static Font getCardIconFont() {
        return CARD_ICON_FONT;
    }

    public static Font getSectionFont() {
        return SECTION_FONT;
    }
}
