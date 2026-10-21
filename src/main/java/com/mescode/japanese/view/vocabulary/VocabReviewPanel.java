package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.service.VocabReviewSession;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public final class VocabReviewPanel extends JPanel {
    private final List<Vocabulary> words;
    private VocabReviewSession session;
    private boolean darkMode;
    private final JPanel content = transparent(new BorderLayout(0, 22));
    private final JComboBox<Integer> chapter;
    private final JCheckBoxMenuItem reverse = new JCheckBoxMenuItem("Hỏi nghĩa tiếng Việt trước");
    private final JMenuItem shuffle = new JMenuItem("Xáo trộn thẻ");
    private final JMenuItem reset = new JMenuItem("Ôn lại chapter từ đầu");
    private final JPopupMenu settings = new JPopupMenu();
    private final JLabel title = new JLabel("Ôn từ vựng");
    private final JLabel position = new JLabel("", SwingConstants.CENTER);
    private final JProgressBar progress = new JProgressBar();
    private final ReviewButton card = new ReviewButton("", false, true);
    private final ReviewButton previous = new ReviewButton("←", false, false);
    private final ReviewButton next = new ReviewButton("→", false, false);
    private final ReviewButton known = new ReviewButton("Đã nhớ", true, false);
    private final ReviewButton missed = new ReviewButton("Cần ôn lại", false, false);
    private final ReviewButton retry = new ReviewButton("Ôn từ chưa nhớ", true, false);
    private final ReviewButton restart = new ReviewButton("Ôn lại chapter", false, false);
    private final ReviewButton back = new ReviewButton("←  Vocabulary", false, false);
    private final ReviewButton options = new ReviewButton("···", false, false);
    private final JPanel actions = transparent(new FlowLayout(FlowLayout.CENTER, 12, 0));

    public VocabReviewPanel(List<Vocabulary> words, boolean darkMode, Runnable onBack) {
        this.words = words;
        this.darkMode = darkMode;
        setLayout(null);
        add(content);
        chapter = new JComboBox<>(VocabReviewSession.availableChapters(words).toArray(Integer[]::new));
        chapter.setName("reviewChapter");
        card.setName("reviewCard");
        known.setName("reviewKnown");
        missed.setName("reviewMissed");
        retry.setName("reviewRetry");
        options.setName("reviewOptions");
        reverse.setName("reviewReverse");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        card.setFont(new Font("Yu Gothic UI", Font.PLAIN, 48));
        card.setBorder(new EmptyBorder(42, 30, 52, 30));
        card.setToolTipText("Nhấn hoặc dùng phím Space để lật thẻ");
        previous.setToolTipText("Thẻ trước (←)");
        next.setToolTipText("Thẻ tiếp (→)");
        options.setToolTipText("Tuỳ chọn ôn tập");
        options.getAccessibleContext().setAccessibleName("Tuỳ chọn ôn tập");

        JPanel heading = transparent(new BorderLayout(0, 20));
        JPanel breadcrumb = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        breadcrumb.add(back);
        heading.add(breadcrumb, BorderLayout.NORTH);
        JPanel titleRow = transparent(new BorderLayout(16, 0));
        titleRow.add(title, BorderLayout.WEST);
        JPanel selection = transparent(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chapter.setPreferredSize(new Dimension(150, 40));
        chapter.setBorder(new EmptyBorder(0, 12, 0, 8));
        chapter.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, false);
                label.setText(value == null ? "Chưa có chapter" : "Chapter " + value);
                label.setBorder(new EmptyBorder(8, 8, 8, 8));
                label.setBackground(selected ? UITheme.getCardBackground(VocabReviewPanel.this.darkMode)
                        : UITheme.getPanelBackground(VocabReviewPanel.this.darkMode));
                label.setForeground(UITheme.getTextForeground(VocabReviewPanel.this.darkMode));
                return label;
            }
        });
        chapter.setUI(new BasicComboBoxUI() {
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean focus) {
                g.setColor(UITheme.getPanelBackground(VocabReviewPanel.this.darkMode));
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override public void paintCurrentValue(Graphics g, Rectangle bounds, boolean focus) {
                g.setFont(chapter.getFont());
                g.setColor(UITheme.getTextForeground(VocabReviewPanel.this.darkMode));
                FontMetrics metrics = g.getFontMetrics();
                String text = chapter.getSelectedItem() == null ? "Chưa có chapter" : "Chapter " + chapter.getSelectedItem();
                g.drawString(text, bounds.x + 8, bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent());
            }

            @Override protected JButton createArrowButton() {
                ReviewButton arrow = new ReviewButton("", false, false);
                arrow.setIcon(new Icon() {
                    @Override public int getIconWidth() { return 10; }
                    @Override public int getIconHeight() { return 6; }
                    @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(UITheme.getTextForeground(VocabReviewPanel.this.darkMode));
                        g.setStroke(new BasicStroke(1.5f));
                        g.drawLine(x, y, x + 5, y + 5);
                        g.drawLine(x + 5, y + 5, x + 10, y);
                        g.dispose();
                    }
                });
                arrow.setBorder(new EmptyBorder(0, 4, 0, 4));
                return arrow;
            }
        });
        selection.add(chapter);
        selection.add(options);
        titleRow.add(selection, BorderLayout.EAST);
        heading.add(titleRow, BorderLayout.CENTER);
        content.add(heading, BorderLayout.NORTH);
        content.add(card, BorderLayout.CENTER);

        JPanel footer = transparent(new BorderLayout(0, 16));
        progress.setBorderPainted(false);
        progress.setPreferredSize(new Dimension(1, 3));
        progress.setUI(new BasicProgressBarUI());
        footer.add(progress, BorderLayout.NORTH);
        JPanel navigation = transparent(new FlowLayout(FlowLayout.CENTER, 16, 0));
        position.setPreferredSize(new Dimension(90, 40));
        navigation.add(previous);
        navigation.add(position);
        navigation.add(next);
        footer.add(navigation, BorderLayout.CENTER);
        actions.setPreferredSize(new Dimension(1, 48));
        actions.add(missed);
        actions.add(known);
        actions.add(retry);
        actions.add(restart);
        footer.add(actions, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);

        settings.add(reverse);
        settings.add(shuffle);
        settings.addSeparator();
        settings.add(reset);
        options.setComponentPopupMenu(settings);
        options.addActionListener(event -> settings.show(options,
                options.getWidth() - settings.getPreferredSize().width, options.getHeight() + 6));
        back.addActionListener(event -> onBack.run());
        chapter.addActionListener(event -> selectChapter());
        reverse.addActionListener(event -> render());
        card.addActionListener(event -> { session.flip(); render(); });
        previous.addActionListener(event -> move(-1));
        next.addActionListener(event -> move(1));
        known.addActionListener(event -> rate(true));
        missed.addActionListener(event -> rate(false));
        shuffle.addActionListener(event -> { session.shuffle(); render(); });
        reset.addActionListener(event -> { session.restart(); render(); });
        retry.addActionListener(event -> { session.retryMissed(); render(); card.requestFocusInWindow(); });
        restart.addActionListener(event -> { session.restart(); render(); card.requestFocusInWindow(); });
        bindCardKey("LEFT", "previous", () -> move(-1));
        bindCardKey("RIGHT", "next", () -> move(1));
        card.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) { render(); }
        });
        selectChapter();
        applyTheme(darkMode);
    }

    /** Keep the study surface readable on maximized windows instead of stretching edge to edge. */
    @Override public void doLayout() {
        int width = Math.max(0, Math.min(820, getWidth() - 64));
        int height = Math.max(0, Math.min(650, getHeight() - 48));
        content.setBounds((getWidth() - width) / 2, (getHeight() - height) / 2, width, height);
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private void bindCardKey(String key, String name, Runnable action) {
        card.getInputMap().put(KeyStroke.getKeyStroke(key), name);
        card.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { action.run(); }
        });
    }

    private void selectChapter() {
        session = new VocabReviewSession(words, (Integer) chapter.getSelectedItem());
        render();
    }

    private void move(int offset) { session.move(offset); render(); card.requestFocusInWindow(); }
    private void rate(boolean remembered) { session.rate(remembered); render(); card.requestFocusInWindow(); }

    private void render() {
        if (session == null) return;
        boolean empty = session.size() == 0;
        boolean complete = session.isComplete();
        boolean active = !empty && !complete;
        position.setText(empty ? "0 / 0" : complete ? "Hoàn thành" : session.position() + " / " + session.size());
        progress.setMaximum(Math.max(1, session.size()));
        progress.setValue(session.reviewedCount());
        progress.setToolTipText("Đã ôn " + session.reviewedCount() + " / " + session.size() + " từ · Tiến độ trong phiên này");
        chapter.setEnabled(chapter.getItemCount() > 0);
        reverse.setEnabled(active);
        card.setEnabled(active);
        previous.setVisible(!complete);
        next.setVisible(!complete);
        previous.setEnabled(active && session.size() > 1);
        next.setEnabled(active && session.size() > 1);
        shuffle.setEnabled(active && session.size() > 1);
        reset.setEnabled(!empty);
        known.setEnabled(active && session.isFlipped());
        missed.setEnabled(active && session.isFlipped());
        known.setVisible(active && session.isFlipped());
        missed.setVisible(active && session.isFlipped());
        retry.setVisible(complete && session.retryCount() > 0);
        restart.setVisible(complete);
        String text;
        if (empty) {
            text = "<font size='5'>Chưa có từ vựng để ôn tập</font>";
        } else if (complete) {
            text = "<font size='6'><b>Hoàn thành!</b></font><br><br><font size='4'>Bạn đã nhớ "
                    + session.knownCount() + " / " + session.size() + " từ."
                    + (session.retryCount() == 0 ? "" : "<br>Còn " + session.retryCount() + " từ cần ôn lại.") + "</font>";
        } else {
            Vocabulary word = session.currentWord();
            boolean showMeaning = reverse.isSelected() != session.isFlipped();
            card.setFont(new Font("Yu Gothic UI", Font.PLAIN, showMeaning ? 32 : 48));
            String japanese = hasText(word.getKanji()) ? word.getKanji() : word.getKana();
            text = "<font size='3' color='" + mutedHex() + "'>" + (showMeaning ? "TIẾNG VIỆT" : "TIẾNG NHẬT")
                    + "</font><br><br><b>" + escape(showMeaning ? word.getMeaning() : japanese) + "</b>";
            if (session.isFlipped()) {
                if (hasText(word.getKana()) && !word.getKana().equals(japanese)) {
                    text += "<br><font size='5'>" + escape(word.getKana()) + "</font>";
                }
                if (hasText(word.getRomaji())) text += "<br><font size='4' color='" + mutedHex() + "'>" + escape(word.getRomaji()) + "</font>";
            }
        }
        int width = Math.max(180, Math.min(600, card.getWidth() - 100));
        card.setText("<html><div style='text-align:center;width:" + width + "px'>" + text + "</div></html>");
        revalidate();
        repaint();
    }

    private String mutedHex() {
        Color color = mutedColor();
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private Color mutedColor() {
        Color foreground = UITheme.getTextForeground(darkMode);
        Color background = UITheme.getBackground(darkMode);
        return new Color((foreground.getRed() * 2 + background.getRed()) / 3,
                (foreground.getGreen() * 2 + background.getGreen()) / 3,
                (foreground.getBlue() * 2 + background.getBlue()) / 3);
    }

    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void applyTheme(boolean darkMode) {
        this.darkMode = darkMode;
        setBackground(UITheme.getBackground(darkMode));
        title.setForeground(UITheme.getTitleForeground(darkMode));
        position.setForeground(mutedColor());
        position.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chapter.setBackground(UITheme.getPanelBackground(darkMode));
        chapter.setForeground(UITheme.getTextForeground(darkMode));
        chapter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (Component child : chapter.getComponents()) {
            if (child instanceof JButton arrow) {
                arrow.setBackground(UITheme.getPanelBackground(darkMode));
                arrow.setForeground(UITheme.getTextForeground(darkMode));
            }
        }
        settings.setBackground(UITheme.getPanelBackground(darkMode));
        settings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.getBorder(darkMode)), new EmptyBorder(6, 6, 6, 6)));
        for (JMenuItem item : List.of(reverse, shuffle, reset)) {
            item.setBackground(UITheme.getPanelBackground(darkMode));
            item.setForeground(UITheme.getTextForeground(darkMode));
            item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            item.setBorder(new EmptyBorder(10, 12, 10, 12));
        }
        for (ReviewButton button : List.of(card, previous, next, known, missed, retry, restart, back, options)) {
            button.setBackground(button.flashcard ? UITheme.getCardBackground(darkMode)
                    : button.primary ? UITheme.getAccent(darkMode) : UITheme.getPanelBackground(darkMode));
            button.setForeground(button.primary ? Color.WHITE : UITheme.getTitleForeground(darkMode));
        }
        back.setBackground(UITheme.getBackground(darkMode));
        back.setForeground(mutedColor());
        progress.setForeground(UITheme.getAccent(darkMode));
        progress.setBackground(UITheme.getPanelBackground(darkMode));
        render();
    }

    private final class ReviewButton extends JButton {
        private final boolean primary;
        private final boolean flashcard;

        private ReviewButton(String text, boolean primary, boolean flashcard) {
            super(text);
            this.primary = primary;
            this.flashcard = flashcard;
            setUI(new BasicButtonUI() {
                @Override protected void paintText(Graphics g, JComponent component, Rectangle bounds, String text) {
                    if (isEnabled()) {
                        super.paintText(g, component, bounds, text);
                    } else {
                        g.setColor(mutedColor());
                        g.drawString(text, bounds.x, bounds.y + g.getFontMetrics().getAscent());
                    }
                }
            });
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(11, 20, 11, 20));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int inset = flashcard ? 4 : 1;
            int radius = flashcard ? 28 : 16;
            if (flashcard) {
                g.setColor(new Color(0, 0, 0, darkMode ? 28 : 15));
                g.fillRoundRect(4, 8, getWidth() - 8, getHeight() - 8, radius, radius);
            }
            Color fill = getBackground();
            if (isEnabled() && getModel().isRollover()) fill = primary ? fill.brighter() : UITheme.getCardBackground(darkMode);
            g.setColor(fill);
            g.fillRoundRect(inset, inset, getWidth() - inset * 2, getHeight() - (flashcard ? 10 : 2), radius, radius);
            if (isFocusOwner() || (flashcard && getModel().isRollover())) {
                g.setColor(UITheme.getBorder(darkMode));
                g.drawRoundRect(inset, inset, getWidth() - inset * 2 - 1,
                        getHeight() - (flashcard ? 10 : 2) - 1, radius, radius);
            }
            super.paintComponent(g);
            if (flashcard && isEnabled()) {
                g.setColor(mutedColor());
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String hint = "Nhấn để lật thẻ";
                g.drawString(hint, (getWidth() - g.getFontMetrics().stringWidth(hint)) / 2, getHeight() - 26);
            }
            g.dispose();
        }
    }
}
