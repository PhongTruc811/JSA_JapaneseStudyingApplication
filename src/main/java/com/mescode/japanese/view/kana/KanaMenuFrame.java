package com.mescode.japanese.view.kana;

import com.mescode.japanese.app.navigation.MenuNavigator;
import com.mescode.japanese.app.navigation.MenuOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KanaMenuFrame extends JFrame {
    private final MenuNavigator menuNavigator;

    private Color background;
    private Color panelBackground;
    private Color border;
    private Color HOVER;
    private Color PRESSED;
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private final Font labelFont = new Font("Arial Emoji", Font.PLAIN, 16);

    public KanaMenuFrame(MenuNavigator nav) {
        this.menuNavigator = nav;
        com.mescode.japanese.app.context.AppContext appContext = nav.getAppContext();
        appContext.addThemeListener(isDark -> javax.swing.SwingUtilities.invokeLater(() -> refreshTheme(isDark)));
        setTitle("Kana Menu");

        setupFrame();
        refreshTheme(appContext.isDarkMode());
        setupUI();
        setVisible(true);
    }

    private void refreshTheme(boolean dark) {
        boolean d = dark;
        background = d ? new Color(0x0F172A) : new Color(0xE0F6FF);
        panelBackground = d ? new Color(0x1E293B) : Color.WHITE;
        border = d ? new Color(0x334155) : new Color(0xB3D9E6);
        HOVER = d ? new Color(0x334155) : new Color(0xD4EAFF);
        PRESSED = d ? new Color(0x475569) : new Color(0xB8D4FF);
        getContentPane().setBackground(background);
        repaint();
    }

    private void setupFrame() {
        getContentPane().setBackground(background);
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void setupUI() {
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setBackground(background);
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
        wrapperPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Kana");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(0x1F2D3D));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapperPanel.add(titleLabel);
        wrapperPanel.add(Box.createVerticalStrut(15));

        JPanel shadowPanel = new ShadowPanel();
        shadowPanel.setLayout(new BorderLayout());
        shadowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        shadowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(panelBackground);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        cardPanel.add(createMenuItem("Hiragana Quiz あ", MenuOptions.HiraQuiz));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createMenuItem("Katakana Quiz ア", MenuOptions.KataQuiz));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createMenuItem("Back", MenuOptions.MenuHome));

        shadowPanel.add(cardPanel, BorderLayout.CENTER);
        wrapperPanel.add(shadowPanel);
        wrapperPanel.add(Box.createVerticalGlue());

        add(wrapperPanel, BorderLayout.CENTER);
    }

    private JPanel createMenuItem(String text, MenuOptions option) {
        return new GradientMenuItemPanel(text, option, menuNavigator);
    }

    private class ShadowPanel extends JPanel {
        private static final int SHADOW_SIZE = 6;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            for (int i = SHADOW_SIZE; i > 0; i--) {
                float alpha = 0.05f * (SHADOW_SIZE - i);
                g2d.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                g2d.fillRoundRect(i, i, width - 2 * i, height - 2 * i, 8, 8);
            }

            g2d.setColor(panelBackground);
            g2d.fillRoundRect(0, 0, width - SHADOW_SIZE, height - SHADOW_SIZE, 8, 8);

            g2d.setColor(border);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawRoundRect(0, 0, width - SHADOW_SIZE - 1, height - SHADOW_SIZE - 1, 8, 8);

            g2d.dispose();
            super.paintComponent(g);
        }
    }

    private class GradientMenuItemPanel extends JPanel {
        private Color gradientStartColor;
        private Color gradientEndColor;
        private boolean isHovered = false;

        GradientMenuItemPanel(String text, MenuOptions option, MenuNavigator navigator) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

            gradientStartColor = panelBackground;
            gradientEndColor = panelBackground;

            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(labelFont);
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 45));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            label.setForeground(new Color(0x111827));
            label.setOpaque(false);
            label.setBackground(new Color(255, 255, 255, 0));

            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    gradientStartColor = HOVER;
                    gradientEndColor = new Color(0xD4E8FF);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    gradientStartColor = panelBackground;
                    gradientEndColor = panelBackground;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    gradientStartColor = PRESSED;
                    gradientEndColor = new Color(0xB8D4FF);
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (getBounds().contains(e.getPoint())) {
                        isHovered = true;
                        gradientStartColor = HOVER;
                        gradientEndColor = new Color(0xD4E8FF);
                    } else {
                        isHovered = false;
                        gradientStartColor = panelBackground;
                        gradientEndColor = panelBackground;
                    }
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (navigator != null) {
                        navigator.navigateTo(option);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            GradientPaint gradient = new GradientPaint(
                    0, 0, gradientStartColor,
                    width, height, gradientEndColor
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, width, height, 6, 6);

            if (isHovered) {
                g2d.setColor(new Color(0xB3D9FF));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 6, 6);
            }

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}
