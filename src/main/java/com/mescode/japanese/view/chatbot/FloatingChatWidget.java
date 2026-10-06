package com.mescode.japanese.view.chatbot;

import com.mescode.japanese.service.chatbot.MizukiReply;
import com.mescode.japanese.service.chatbot.ChatService;
import com.mescode.japanese.service.chatbot.MizukiAction;
import com.mescode.japanese.service.music.MusicPlayer;
import com.mescode.japanese.service.music.RepeatMode;
import com.mescode.japanese.util.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Web-style floating chatbot launcher and in-window chat panel.
 */
public class FloatingChatWidget extends JPanel implements AutoCloseable {
    private static final Dimension COLLAPSED_SIZE = new Dimension(310, 96);
    private static final Dimension EXPANDED_SIZE = new Dimension(430, 600);
    private static final Dimension MIN_EXPANDED_SIZE = new Dimension(350, 430);
    private static final int OPENING_DURATION_MS = 180;
    private static final int CLOSING_DURATION_MS = 140;
    private static final int DRAG_THRESHOLD = 5;
    private static final int RESIZE_HANDLE_SIZE = 9;
    private static final int RESIZE_NORTH = 1;
    private static final int RESIZE_SOUTH = 1 << 1;
    private static final int RESIZE_WEST = 1 << 2;
    private static final int RESIZE_EAST = 1 << 3;
    // câu chao
    private static final String[] LAUNCHER_GREETINGS = {
            "Chào bạn, mình là Mizuki san!",
            "^3^",
            "Học tiếng Nhật cùng mình nhé ^v^",
            "Bạn cần tra từ vựng? Mizuki có thể giúp nha ^v^",
            "Mình sẽ giúp bạn làm quiz về từ vựng :3"
    };
    private static final String CONTENT_FONT = selectContentFont();
    private static final Image MIZUKI_AVATAR = loadMizukiAvatar();

    private final CardLayout cardLayout = new CardLayout();
    private final BooleanSupplier darkModeSupplier;
    private final Runnable boundsChangeListener;
    private final Consumer<Point> launcherMoveListener;
    private final Consumer<Rectangle> resizeBoundsListener;
    private final ChatService chatService;
    private final MusicPlayer musicPlayer;
    private final boolean ownsMusicPlayer;
    private final ChatLauncherButton launcherButton;
    private final ChatPanel chatPanel;
    private final Timer launcherPulseTimer;
    private final Timer transitionTimer;
    private Dimension expandedSize = new Dimension(EXPANDED_SIZE);
    private float launcherPulsePhase;
    private int launcherAnimationTicks;
    private int launcherGreetingIndex;
    private long transitionStartedAtNanos;
    private TransitionState transitionState = TransitionState.COLLAPSED;
    private boolean expanded;
    private boolean launcherDragged;

    public FloatingChatWidget(BooleanSupplier darkModeSupplier,
                              ChatService chatService,
                              MusicPlayer musicPlayer,
                              Runnable boundsChangeListener,
                              Consumer<Point> launcherMoveListener,
                              Consumer<Rectangle> resizeBoundsListener) {
        this(
                darkModeSupplier,
                chatService,
                musicPlayer,
                boundsChangeListener,
                launcherMoveListener,
                resizeBoundsListener,
                false
        );
    }

    public FloatingChatWidget(BooleanSupplier darkModeSupplier,
                              ChatService chatService,
                              Runnable boundsChangeListener,
                              Consumer<Point> launcherMoveListener,
                              Consumer<Rectangle> resizeBoundsListener) {
        this(
                darkModeSupplier,
                chatService,
                MusicPlayer.unavailable(),
                boundsChangeListener,
                launcherMoveListener,
                resizeBoundsListener,
                false
        );
    }

    private FloatingChatWidget(BooleanSupplier darkModeSupplier,
                               ChatService chatService,
                               MusicPlayer musicPlayer,
                               Runnable boundsChangeListener,
                               Consumer<Point> launcherMoveListener,
                               Consumer<Rectangle> resizeBoundsListener,
                               boolean ownsMusicPlayer) {
        this.darkModeSupplier = darkModeSupplier == null ? () -> true : darkModeSupplier;
        this.chatService = chatService;
        this.musicPlayer = musicPlayer == null ? MusicPlayer.unavailable() : musicPlayer;
        this.ownsMusicPlayer = ownsMusicPlayer;
        this.boundsChangeListener = boundsChangeListener == null ? () -> { } : boundsChangeListener;
        this.launcherMoveListener = launcherMoveListener == null ? point -> { } : launcherMoveListener;
        this.resizeBoundsListener = resizeBoundsListener == null
                ? bounds -> { }
                : resizeBoundsListener;

        setOpaque(false);
        setLayout(cardLayout);

        launcherButton = new ChatLauncherButton();
        launcherButton.addActionListener(event -> {
            if (!launcherDragged) {
                setExpanded(true);
            }
        });
        add(launcherButton, "launcher");

        chatPanel = new ChatPanel();
        add(chatPanel, "chat");

        transitionTimer = new Timer(16, event -> advanceTransition());
        transitionTimer.setCoalesce(true);

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "collapse-chat"
        );
        getActionMap().put("collapse-chat", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setExpanded(false);
            }
        });

        launcherPulseTimer = new Timer(48, event -> {
            launcherPulsePhase += 0.12f;
            if (!expanded && !isTransitioning()) {
                launcherAnimationTicks++;
                if (launcherAnimationTicks % 90 == 0) {
                    launcherGreetingIndex =
                            (launcherGreetingIndex + 1) % LAUNCHER_GREETINGS.length;
                }
                launcherButton.repaint();
            }
        });
        launcherPulseTimer.start();

        cardLayout.show(this, "launcher");
        updateTheme();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isTransitioning() {
        return transitionState == TransitionState.OPENING
                || transitionState == TransitionState.CLOSING;
    }

    public void setExpanded(boolean expanded) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setExpanded(expanded));
            return;
        }
        if (expanded) {
            if (transitionState == TransitionState.COLLAPSED) {
                beginOpening();
            }
        } else if (transitionState == TransitionState.EXPANDED) {
            beginClosing();
        }
    }

    /**
     * Used by navigation when the widget must disappear immediately, such as
     * when switching modules or entering PE Trial.
     */
    public void collapseImmediately() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::collapseImmediately);
            return;
        }
        transitionTimer.stop();
        transitionState = TransitionState.COLLAPSED;
        expanded = false;
        chatPanel.setRevealProgress(1f);
        cardLayout.show(this, "launcher");
        if (!launcherPulseTimer.isRunning()) {
            launcherPulseTimer.start();
        }
        revalidate();
        boundsChangeListener.run();
        repaint();
    }

    public void updateTheme() {
        launcherButton.repaint();
        chatPanel.applyTheme();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return expanded ? new Dimension(expandedSize) : new Dimension(COLLAPSED_SIZE);
    }

    public void setExpandedSize(Dimension size) {
        if (size == null) {
            return;
        }
        expandedSize = new Dimension(
                Math.max(1, size.width),
                Math.max(1, size.height)
        );
        if (transitionState == TransitionState.EXPANDED) {
            boundsChangeListener.run();
        }
    }

    @Override
    public void close() {
        launcherPulseTimer.stop();
        transitionTimer.stop();
        chatPanel.disposeChat();
        chatService.close();
        if (ownsMusicPlayer) {
            musicPlayer.close();
        }
    }

    private void beginOpening() {
        expanded = true;
        transitionState = TransitionState.OPENING;
        chatPanel.setRevealProgress(0f);
        cardLayout.show(this, "chat");
        launcherPulseTimer.stop();
        startTransitionTimer();
    }

    private void beginClosing() {
        transitionState = TransitionState.CLOSING;
        chatPanel.setRevealProgress(1f);
        startTransitionTimer();
    }

    private void startTransitionTimer() {
        transitionStartedAtNanos = System.nanoTime();
        if (!transitionTimer.isRunning()) {
            transitionTimer.start();
        }
        revalidate();
        boundsChangeListener.run();
        repaint();
    }

    private void advanceTransition() {
        float elapsed = (System.nanoTime() - transitionStartedAtNanos) / 1_000_000f;
        int duration = transitionState == TransitionState.CLOSING
                ? CLOSING_DURATION_MS
                : OPENING_DURATION_MS;
        float rawProgress = Math.min(1f, elapsed / duration);
        if (transitionState == TransitionState.OPENING) {
            advanceOpening(rawProgress);
        } else if (transitionState == TransitionState.CLOSING) {
            advanceClosing(rawProgress);
        } else {
            transitionTimer.stop();
        }
    }

    private void advanceOpening(float rawProgress) {
        float easedProgress = easeInOutCubic(rawProgress);
        chatPanel.setRevealProgress(easedProgress);
        repaint();
        if (rawProgress >= 1f) {
            transitionTimer.stop();
            transitionState = TransitionState.EXPANDED;
            chatPanel.setRevealProgress(1f);
            revalidate();
            boundsChangeListener.run();
            repaint();
            SwingUtilities.invokeLater(chatPanel::focusInput);
        }
    }

    private void advanceClosing(float rawProgress) {
        chatPanel.setRevealProgress(1f - easeInOutCubic(rawProgress));
        repaint();
        if (rawProgress >= 1f) {
            transitionTimer.stop();
            transitionState = TransitionState.COLLAPSED;
            expanded = false;
            chatPanel.setRevealProgress(1f);
            cardLayout.show(this, "launcher");
            launcherPulseTimer.start();
            revalidate();
            boundsChangeListener.run();
            repaint();
        }
    }

    private float easeInOutCubic(float progress) {
        float value = Math.max(0f, Math.min(1f, progress));
        return value < 0.5f
                ? 4f * value * value * value
                : 1f - (float) Math.pow(-2f * value + 2f, 3d) / 2f;
    }

    private enum TransitionState {
        COLLAPSED,
        OPENING,
        EXPANDED,
        CLOSING
    }

    private boolean isDarkMode() {
        return darkModeSupplier.getAsBoolean();
    }

    private void executeMizukiAction(MizukiAction action) {
        if (action == null || !action.isMusicAction()) {
            return;
        }
        switch (action.type()) {
            case MUSIC_PLAY -> musicPlayer.play();
            case MUSIC_PAUSE -> musicPlayer.pause();
            case MUSIC_NEXT -> musicPlayer.next();
            case MUSIC_PREVIOUS -> musicPlayer.previous();
            case MUSIC_SEEK_ABSOLUTE_SECONDS ->
                    musicPlayer.seek(Duration.ofSeconds(Math.max(0, action.value())));
            case MUSIC_SEEK_RELATIVE_SECONDS -> {
                long current = musicPlayer.snapshot().positionMillis();
                long target = Math.max(0L, current + action.value() * 1000L);
                musicPlayer.seek(Duration.ofMillis(target));
            }
            case MUSIC_SET_VOLUME_PERCENT ->
                    musicPlayer.setVolume(Math.max(0, Math.min(100, action.value())) / 100f);
            case MUSIC_CHANGE_VOLUME_PERCENT -> {
                float current = musicPlayer.snapshot().volume();
                musicPlayer.setVolume(current + action.value() / 100f);
            }
            case MUSIC_MUTE -> musicPlayer.setMuted(true);
            case MUSIC_UNMUTE -> musicPlayer.setMuted(false);
            case MUSIC_SHUFFLE_ON -> musicPlayer.setShuffle(true);
            case MUSIC_SHUFFLE_OFF -> musicPlayer.setShuffle(false);
            case MUSIC_REPEAT_OFF -> musicPlayer.setRepeatMode(RepeatMode.OFF);
            case MUSIC_REPEAT_ALL -> musicPlayer.setRepeatMode(RepeatMode.ALL);
            case MUSIC_REPEAT_ONE -> musicPlayer.setRepeatMode(RepeatMode.ONE);
            case NONE -> {
                // No side effect.
            }
        }
    }

    private static String selectContentFont() {
        String sample = "Tiếng Việt • ひらがな • カタカナ • 日本語";
        for (String candidate : List.of(
                Font.DIALOG,
                "Noto Sans JP",
                "Meiryo UI",
                "Meiryo",
                "Yu Gothic UI"
        )) {
            Font font = new Font(candidate, Font.PLAIN, 13);
            if (font.canDisplayUpTo(sample) == -1) {
                return candidate;
            }
        }
        return Font.DIALOG;
    }

    private static Image loadMizukiAvatar() {
        URL resource = FloatingChatWidget.class.getResource("/icons/mizuki.png");
        return resource == null ? null : new ImageIcon(resource).getImage();
    }

    private static void drawImageCover(Graphics2D g2, Image image,
                                       int x, int y, int width, int height) {
        if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            return;
        }
        double scale = Math.max(
                width / (double) image.getWidth(null),
                height / (double) image.getHeight(null)
        );
        int drawWidth = (int) Math.ceil(image.getWidth(null) * scale);
        int drawHeight = (int) Math.ceil(image.getHeight(null) * scale);
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2;
        g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    private class ChatLauncherButton extends JButton {
        private ChatLauncherButton() {
            setPreferredSize(COLLAPSED_SIZE);
            setMinimumSize(COLLAPSED_SIZE);
            setMaximumSize(COLLAPSED_SIZE);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Nhấp để mở Mizuki • Giữ và kéo để di chuyển");
            getAccessibleContext().setAccessibleName(
                    "Mở Mizuki AI hoặc giữ và kéo để di chuyển"
            );

            MouseAdapter dragHandler = new MouseAdapter() {
                private Point pressOnScreen;
                private Point widgetStart;

                @Override
                public void mousePressed(MouseEvent event) {
                    if (!SwingUtilities.isLeftMouseButton(event)) {
                        return;
                    }
                    pressOnScreen = event.getLocationOnScreen();
                    widgetStart = FloatingChatWidget.this.getLocation();
                    launcherDragged = false;
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    if (pressOnScreen == null || widgetStart == null) {
                        return;
                    }
                    Point current = event.getLocationOnScreen();
                    int deltaX = current.x - pressOnScreen.x;
                    int deltaY = current.y - pressOnScreen.y;
                    if (!launcherDragged
                            && Math.hypot(deltaX, deltaY) < DRAG_THRESHOLD) {
                        return;
                    }
                    launcherDragged = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    launcherMoveListener.accept(new Point(
                            widgetStart.x + deltaX,
                            widgetStart.y + deltaY
                    ));
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    pressOnScreen = null;
                    widgetStart = null;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (launcherDragged) {
                        SwingUtilities.invokeLater(() -> launcherDragged = false);
                    }
                }
            };
            addMouseListener(dragHandler);
            addMouseMotionListener(dragHandler);
        }

        @Override
        public boolean contains(int pointX, int pointY) {
            int diameter = 64;
            int avatarX = getWidth() - diameter - 5;
            int avatarY = getHeight() - diameter - 5;
            boolean onAvatar = new java.awt.geom.Ellipse2D.Double(
                    avatarX,
                    avatarY,
                    diameter,
                    diameter
            ).contains(pointX, pointY);
            boolean onGreeting = pointX >= 2
                    && pointX <= avatarX + 13
                    && pointY >= 7
                    && pointY <= 63;
            return onAvatar || onGreeting;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = 64;
            int x = getWidth() - diameter - 5;
            int y = getHeight() - diameter - 5;
            int bubbleX = 2;
            int bubbleY = 7;
            int bubbleWidth = Math.max(1, x - 12);
            int bubbleHeight = 56;

            Color bubbleFill = isDarkMode()
                    ? new Color(15, 23, 42, 238)
                    : new Color(255, 255, 255, 245);
            Color bubbleBorder = UITheme.getBorder(isDarkMode());
            g2.setColor(new Color(15, 23, 42, isDarkMode() ? 75 : 35));
            g2.fillRoundRect(bubbleX + 3, bubbleY + 4, bubbleWidth, bubbleHeight, 18, 18);
            g2.setColor(bubbleFill);
            g2.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 18, 18);
            Polygon tail = new Polygon(
                    new int[]{bubbleX + bubbleWidth - 2, bubbleX + bubbleWidth + 13,
                            bubbleX + bubbleWidth - 2},
                    new int[]{bubbleY + 27, bubbleY + 37, bubbleY + 43},
                    3
            );
            g2.fillPolygon(tail);
            g2.setColor(bubbleBorder);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(
                    bubbleX, bubbleY, bubbleWidth - 1, bubbleHeight - 1, 18, 18
            );

            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
            g2.setColor(isDarkMode() ? new Color(0xFDBA74) : new Color(0xC2410C));
            g2.drawString("MIZUKI • SẴN SÀNG", bubbleX + 13, bubbleY + 19);
            g2.setFont(new Font(CONTENT_FONT, Font.PLAIN, 13));
            g2.setColor(UITheme.getTitleForeground(isDarkMode()));
            g2.drawString(
                    LAUNCHER_GREETINGS[launcherGreetingIndex],
                    bubbleX + 13,
                    bubbleY + 40
            );

            float pulse = (float) ((Math.sin(launcherPulsePhase) + 1d) / 2d);
            int pulseSize = Math.round(4 + pulse * 5);
            Color pulseColor = UITheme.getGlow(isDarkMode());
            g2.setColor(new Color(
                    pulseColor.getRed(),
                    pulseColor.getGreen(),
                    pulseColor.getBlue(),
                    Math.round(24 + pulse * 28)
            ));
            g2.fillOval(x - pulseSize / 2, y - pulseSize / 2,
                    diameter + pulseSize, diameter + pulseSize);

            g2.setColor(new Color(15, 23, 42, isDarkMode() ? 95 : 55));
            g2.fillOval(x + 3, y + 5, diameter, diameter);

            Shape previousClip = g2.getClip();
            g2.clip(new java.awt.geom.Ellipse2D.Double(x, y, diameter, diameter));
            if (MIZUKI_AVATAR != null) {
                drawImageCover(g2, MIZUKI_AVATAR, x, y, diameter, diameter);
            } else {
                g2.setPaint(new GradientPaint(
                        x, y, UITheme.getGlow(isDarkMode()),
                        x + diameter, y + diameter, UITheme.getAccent(isDarkMode())
                ));
                g2.fillOval(x, y, diameter, diameter);
            }
            g2.setClip(previousClip);

            Color avatarBorder = getModel().isPressed()
                    ? UITheme.getPressed(isDarkMode())
                    : getModel().isRollover()
                    ? UITheme.getGlow(isDarkMode()).brighter()
                    : new Color(255, 255, 255, 225);
            g2.setColor(avatarBorder);
            g2.setStroke(new BasicStroke(2.4f));
            g2.drawOval(x, y, diameter, diameter);

            g2.setColor(new Color(34, 197, 94));
            g2.fillOval(x + diameter - 13, y + diameter - 13, 11, 11);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x + diameter - 13, y + diameter - 13, 11, 11);
            if (isFocusOwner()) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x + 2, y + 2, diameter - 4, diameter - 4);
            }
            g2.dispose();
        }
    }

    private class TutorAvatar extends JComponent {
        private TutorAvatar() {
            setPreferredSize(new Dimension(42, 42));
            setMinimumSize(new Dimension(42, 42));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            Shape previousClip = g2.getClip();
            g2.clip(new java.awt.geom.Ellipse2D.Double(x, y, size, size));
            if (MIZUKI_AVATAR != null) {
                drawImageCover(g2, MIZUKI_AVATAR, x, y, size, size);
            } else {
                g2.setPaint(new GradientPaint(
                        x, y, UITheme.getGlow(isDarkMode()),
                        x + size, y + size, UITheme.getAccent(isDarkMode())
                ));
                g2.fillOval(x, y, size, size);
            }
            g2.setClip(previousClip);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(x, y, size, size);
            g2.dispose();
        }
    }

    private enum ChatButtonStyle {
        PRIMARY,
        CHIP
    }

    private class RoundedButton extends JButton {
        private final ChatButtonStyle style;

        private RoundedButton(String text, ChatButtonStyle style) {
            super(text);
            this.style = style;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean dark = isDarkMode();
            boolean enabled = isEnabled();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();
            int arc = style == ChatButtonStyle.PRIMARY ? 14 : 18;

            Color fill;
            Color text;
            if (style == ChatButtonStyle.PRIMARY) {
                fill = !enabled
                        ? (dark ? new Color(0x334155) : new Color(0xCBD5E1))
                        : pressed
                        ? new Color(0x9A3412)
                        : rollover ? new Color(0xEA580C) : new Color(0xC2410C);
                text = enabled
                        ? Color.WHITE
                        : (dark ? new Color(0x94A3B8) : new Color(0x64748B));
            } else {
                fill = rollover && enabled
                        ? (dark ? new Color(0x334155) : new Color(0xE2E8F0))
                        : UITheme.getCardBackground(dark);
                text = enabled
                        ? UITheme.getTextForeground(dark)
                        : (dark ? new Color(0x64748B) : new Color(0x94A3B8));
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            if (style == ChatButtonStyle.CHIP) {
                g2.setColor(UITheme.getBorder(dark));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            if (isFocusOwner()) {
                g2.setColor(UITheme.getGlow(dark));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, arc - 2, arc - 2);
            }
            g2.setFont(getFont());
            g2.setColor(text);
            FontMetrics metrics = g2.getFontMetrics();
            String label = getText();
            int textX = (getWidth() - metrics.stringWidth(label)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(label, textX, textY);
            g2.dispose();
        }
    }

    private class MusicHeaderButton extends JButton {
        private MusicHeaderButton() {
            super("Nhạc");
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            setPreferredSize(new Dimension(78, 32));
            setMinimumSize(new Dimension(78, 32));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Mở trình phát nhạc");
            getAccessibleContext().setAccessibleName("Mở trình phát nhạc");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean dark = isDarkMode();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();
            Color accent = UITheme.getAccent(dark);
            Color bright = dark ? new Color(0xFB923C) : new Color(0xF97316);
            if (pressed) {
                accent = new Color(0xC2410C);
                bright = new Color(0xEA580C);
            } else if (rollover) {
                accent = new Color(0xEA580C);
                bright = new Color(0xFDBA74);
            }

            g2.setPaint(new GradientPaint(0, 0, accent, getWidth(), getHeight(), bright));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            if (isSelected() || isFocusOwner()) {
                g2.setColor(UITheme.getGlow(dark));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
            }

            int iconCenterX = 16;
            int iconCenterY = getHeight() / 2;
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillOval(iconCenterX - 9, iconCenterY - 9, 18, 18);
            g2.setColor(Color.WHITE);
            if (musicPlayer.snapshot().playing()) {
                g2.fillRoundRect(iconCenterX - 5, iconCenterY, 3, 5, 2, 2);
                g2.fillRoundRect(iconCenterX, iconCenterY - 5, 3, 10, 2, 2);
                g2.fillRoundRect(iconCenterX + 5, iconCenterY - 2, 3, 7, 2, 2);
            } else {
                Polygon play = new Polygon();
                play.addPoint(iconCenterX - 3, iconCenterY - 5);
                play.addPoint(iconCenterX + 6, iconCenterY);
                play.addPoint(iconCenterX - 3, iconCenterY + 5);
                g2.fill(play);
            }

            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(getText(), 30, textY);
            g2.dispose();
        }
    }

    private class ResetHeaderButton extends JButton {
        private ResetHeaderButton() {
            super("Mới");
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            setPreferredSize(new Dimension(68, 32));
            setMinimumSize(new Dimension(68, 32));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Bắt đầu cuộc trò chuyện mới");
            getAccessibleContext().setAccessibleName("Bắt đầu cuộc trò chuyện mới");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean dark = isDarkMode();
            boolean pressed = getModel().isPressed();
            boolean rollover = getModel().isRollover();
            Color start = dark ? new Color(0x1E3A5F) : new Color(0xDCEFFC);
            Color end = dark ? new Color(0x2563A8) : new Color(0xBAE6FD);
            if (pressed) {
                start = new Color(0x075985);
                end = new Color(0x0369A1);
            } else if (rollover) {
                start = dark ? new Color(0x2563A8) : new Color(0xBAE6FD);
                end = dark ? new Color(0x0EA5E9) : new Color(0x7DD3FC);
            }

            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(dark ? new Color(0x38BDF8) : new Color(0x0284C7));
            g2.setStroke(new BasicStroke(
                    isFocusOwner() ? 2f : 1f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            int centerX = 15;
            int centerY = getHeight() / 2;
            Color content = dark ? Color.WHITE : new Color(0x075985);
            g2.setColor(content);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(centerX - 6, centerY - 6, 12, 12, 35, 285);
            Polygon arrow = new Polygon();
            arrow.addPoint(centerX + 5, centerY - 7);
            arrow.addPoint(centerX + 8, centerY - 2);
            arrow.addPoint(centerX + 2, centerY - 2);
            g2.fill(arrow);

            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(getText(), 28, textY);
            g2.dispose();
        }
    }

    private class ChatPanel extends JPanel {
        private final JPanel header = new JPanel(new BorderLayout(10, 0));
        private final JPanel headerStack = new JPanel();
        private final JLabel titleLabel = new JLabel("Mizuki AI");
        private final JLabel statusLabel = new JLabel("●  OFFLINE • PRIVATE");
        private final JButton musicButton = new MusicHeaderButton();
        private final JButton resetButton = new ResetHeaderButton();
        private final JButton closeButton = createHeaderButton("×", "Thu nhỏ chatbot");
        private final MizukiMusicPanel musicPanel = new MizukiMusicPanel(
                musicPlayer,
                FloatingChatWidget.this::isDarkMode,
                () -> {
                    launcherButton.repaint();
                    musicButton.repaint();
                }
        );
        private final JPanel messagesPanel = new JPanel();
        private final JScrollPane messagesScrollPane;
        private final JPanel composerPanel = new JPanel(new BorderLayout(8, 0));
        private final JPanel promptButtonsPanel = new JPanel();
        private final JPanel footer = new JPanel(new BorderLayout());
        private final PromptTextArea inputArea = new PromptTextArea();
        private final JButton sendButton = new RoundedButton("Gửi", ChatButtonStyle.PRIMARY);
        private final List<JButton> promptButtons = new ArrayList<>();
        private final List<MessageBubble> messageBubbles = new ArrayList<>();
        private MessageBubble typingBubble;
        private Timer typingTimer;
        private CompletableFuture<MizukiReply> activeRequest;
        private long conversationGeneration;
        private boolean busy;
        private float revealProgress = 1f;

        private ChatPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(
                    RESIZE_HANDLE_SIZE,
                    RESIZE_HANDLE_SIZE,
                    RESIZE_HANDLE_SIZE,
                    RESIZE_HANDLE_SIZE
            ));
            ResizeHandler resizeHandler = new ResizeHandler();
            addMouseListener(resizeHandler);
            addMouseMotionListener(resizeHandler);

            buildHeader();
            headerStack.setOpaque(false);
            headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));
            headerStack.add(header);
            musicPanel.setVisible(false);
            headerStack.add(musicPanel);
            add(headerStack, BorderLayout.NORTH);

            messagesPanel.setOpaque(false);
            messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
            messagesPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

            messagesScrollPane = new JScrollPane(messagesPanel);
            messagesScrollPane.setBorder(null);
            messagesScrollPane.setOpaque(false);
            messagesScrollPane.getViewport().setOpaque(false);
            messagesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            messagesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            messagesScrollPane.getVerticalScrollBar().setUnitIncrement(14);
            styleVerticalScrollBar(messagesScrollPane);
            add(messagesScrollPane, BorderLayout.CENTER);

            buildComposer();
            resetConversation();
        }

        private void buildHeader() {
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(14, 14, 12, 10));

            header.add(new TutorAvatar(), BorderLayout.WEST);

            JPanel identity = new JPanel();
            identity.setOpaque(false);
            identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));
            titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            statusLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
            statusLabel.getAccessibleContext().setAccessibleName("Trạng thái Mizuki AI");
            identity.add(titleLabel);
            identity.add(Box.createVerticalStrut(2));
            identity.add(statusLabel);
            header.add(identity, BorderLayout.CENTER);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            actions.setOpaque(false);
            musicButton.addActionListener(event -> {
                musicPanel.setVisible(!musicPanel.isVisible());
                musicButton.setSelected(musicPanel.isVisible());
                String accessibleLabel = musicPanel.isVisible()
                        ? "Ẩn trình phát nhạc"
                        : "Mở trình phát nhạc";
                musicButton.setToolTipText(accessibleLabel);
                musicButton.getAccessibleContext().setAccessibleName(accessibleLabel);
                headerStack.revalidate();
                headerStack.repaint();
            });
            resetButton.addActionListener(event -> resetConversation());
            closeButton.addActionListener(event -> {
                if (!isTransitioning()) {
                    setExpanded(false);
                }
            });
            actions.add(musicButton);
            actions.add(resetButton);
            actions.add(closeButton);
            header.add(actions, BorderLayout.EAST);
        }

        private void buildComposer() {
            composerPanel.setOpaque(false);
            composerPanel.setBorder(new EmptyBorder(10, 12, 12, 12));

            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);
            inputArea.setOpaque(false);
            inputArea.setFont(new Font(CONTENT_FONT, Font.PLAIN, 13));
            inputArea.setBorder(new EmptyBorder(8, 10, 8, 10));
            inputArea.setToolTipText("Nhấn Enter để gửi, Shift+Enter để xuống dòng");
            inputArea.getAccessibleContext().setAccessibleName("Nội dung câu hỏi cho Mizuki");
            inputArea.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK),
                    "insert-line-break"
            );
            inputArea.getActionMap().put("insert-line-break", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    inputArea.replaceSelection("\n");
                }
            });
            inputArea.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                    "send-message"
            );
            inputArea.getActionMap().put("send-message", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    submitMessage();
                }
            });
            inputArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    updateComposerState();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    updateComposerState();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    updateComposerState();
                }
            });

            JScrollPane inputScrollPane = new ComposerScrollPane(inputArea);
            inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            inputScrollPane.setPreferredSize(new Dimension(1, 56));
            composerPanel.add(inputScrollPane, BorderLayout.CENTER);

            sendButton.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
            sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sendButton.addActionListener(event -> submitMessage());
            sendButton.setPreferredSize(new Dimension(62, 42));
            sendButton.getAccessibleContext().setAccessibleName("Gửi câu hỏi");
            composerPanel.add(sendButton, BorderLayout.EAST);

            promptButtonsPanel.setOpaque(false);
            promptButtonsPanel.setLayout(new BoxLayout(promptButtonsPanel, BoxLayout.X_AXIS));
            promptButtonsPanel.setBorder(new EmptyBorder(1, 12, 5, 12));
            setSuggestions(List.of(
                    "Cách dùng Mizuki",
                    "Phát nhạc",
                    "Tạo quiz nhanh"
            ));

            JScrollPane promptScrollPane = new JScrollPane(promptButtonsPanel);
            promptScrollPane.setBorder(null);
            promptScrollPane.setOpaque(false);
            promptScrollPane.getViewport().setOpaque(false);
            promptScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            promptScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            promptScrollPane.setPreferredSize(new Dimension(1, 38));

            footer.setOpaque(false);
            footer.add(promptScrollPane, BorderLayout.NORTH);
            footer.add(composerPanel, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);
            updateComposerState();
        }

        private void setSuggestions(List<String> suggestions) {
            promptButtonsPanel.removeAll();
            promptButtons.clear();
            List<String> values = suggestions == null || suggestions.isEmpty()
                    ? List.of("Cách dùng Mizuki", "Phát nhạc", "Tạo quiz nhanh")
                    : suggestions;
            for (String prompt : values) {
                addPromptButton(prompt);
            }
            promptButtonsPanel.revalidate();
            promptButtonsPanel.repaint();
            applyPromptTheme();
        }

        private void addPromptButton(String prompt) {
            String label = prompt.length() > 18 ? prompt.substring(0, 17).trim() + "…" : prompt;
            JButton button = new RoundedButton(label, ChatButtonStyle.CHIP);
            button.setFont(new Font(CONTENT_FONT, Font.PLAIN, 12));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBorder(new EmptyBorder(6, 10, 6, 10));
            button.setToolTipText(prompt);
            button.getAccessibleContext().setAccessibleName("Gợi ý: " + prompt);
            button.addActionListener(event -> {
                if (isTransitioning()) {
                    return;
                }
                inputArea.setText(prompt);
                submitMessage();
            });
            promptButtons.add(button);
            promptButtonsPanel.add(button);
            promptButtonsPanel.add(Box.createHorizontalStrut(6));
        }

        private JButton createHeaderButton(String text, String tooltip) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 18));
            button.setToolTipText(tooltip);
            button.getAccessibleContext().setAccessibleName(tooltip);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(true);
            button.setBorder(new EmptyBorder(4, 8, 4, 8));
            button.setContentAreaFilled(false);
            return button;
        }

        private void resetConversation() {
            if (isTransitioning()) {
                return;
            }
            conversationGeneration++;
            chatService.resetSession();
            if (activeRequest != null) {
                activeRequest.cancel(true);
                activeRequest = null;
            }
            removeTypingBubble();
            setBusy(false);
            messagesPanel.removeAll();
            messageBubbles.clear();
            typingBubble = null;
            addMessage(
                    "こんにちは! Mình là Mizuki — người bạn học tiếng Nhật của bạn.\n"
                            + "Mình có thể tra Kana, Kanji, từ vựng, tạo mini quiz và xem tiến độ học. "
                            + "Chọn một gợi ý bên dưới hoặc hỏi “Cách dùng Mizuki” để xem hướng dẫn đầy đủ.",
                    false,
                    "MIZUKI • DỮ LIỆU LOCAL"
            );
            setSuggestions(List.of("Cách dùng Mizuki", "Phát nhạc", "Tạo quiz nhanh"));
            inputArea.setText("");
            messagesPanel.revalidate();
            messagesPanel.repaint();
            scrollToBottom();
        }

        private void submitMessage() {
            if (isTransitioning()) {
                return;
            }
            String message = inputArea.getText().trim();
            if (message.isEmpty() || busy) {
                return;
            }

            addMessage(message, true, "");
            inputArea.setText("");
            setBusy(true);
            startTypingAnimation();
            long requestGeneration = conversationGeneration;
            activeRequest = chatService.reply(message);
            activeRequest.whenComplete((response, error) ->
                    SwingUtilities.invokeLater(() -> {
                        if (requestGeneration != conversationGeneration) {
                            return;
                        }
                        activeRequest = null;
                        removeTypingBubble();
                        if (error != null) {
                            addMessage(
                                    "Mình vừa gặp lỗi khi đọc dữ liệu local. Bạn có thể thử lại hoặc bắt đầu phiên mới.",
                                    false,
                                    "MIZUKI • THỬ LẠI"
                            );
                            setSuggestions(List.of("Thử lại", "Bắt đầu phiên mới", "Cách dùng Mizuki"));
                        } else {
                            executeMizukiAction(response.action());
                            if (response.action().isMusicAction()) {
                                musicPanel.setVisible(true);
                                musicButton.setSelected(true);
                                musicButton.setToolTipText("Ẩn trình phát nhạc");
                                musicButton.getAccessibleContext()
                                        .setAccessibleName("Ẩn trình phát nhạc");
                                headerStack.revalidate();
                            }
                            addMessage(response.message(), false, response.sourceLabel());
                            setSuggestions(response.suggestions());
                        }
                        setBusy(false);
                        focusInput();
                    })
            );
        }

        private void startTypingAnimation() {
            typingBubble = addMessage("●", false, "MIZUKI ĐANG TÌM TRONG DỮ LIỆU JSA");
            final int[] dotCount = {1};
            typingTimer = new Timer(280, event -> {
                dotCount[0] = dotCount[0] % 3 + 1;
                typingBubble.setText("●  ".repeat(dotCount[0]).trim());
            });
            typingTimer.start();
        }

        private MessageBubble addMessage(String text, boolean fromUser, String sourceLabel) {
            boolean autoScroll = isNearBottom();
            MessageBubble bubble = new MessageBubble(text, fromUser, sourceLabel);
            messageBubbles.add(bubble);
            messagesPanel.add(bubble);
            Component spacer = Box.createVerticalStrut(10);
            bubble.setSpacer(spacer);
            messagesPanel.add(spacer);
            trimHistory();
            messagesPanel.revalidate();
            messagesPanel.repaint();
            if (autoScroll) {
                scrollToBottom();
            }
            return bubble;
        }

        private void removeTypingBubble() {
            if (typingTimer != null) {
                typingTimer.stop();
                typingTimer = null;
            }
            if (typingBubble == null) {
                return;
            }
            messagesPanel.remove(typingBubble);
            if (typingBubble.getSpacer() != null) {
                messagesPanel.remove(typingBubble.getSpacer());
            }
            messageBubbles.remove(typingBubble);
            typingBubble = null;
            messagesPanel.revalidate();
            messagesPanel.repaint();
        }

        private void setBusy(boolean busy) {
            this.busy = busy;
            statusLabel.setText(busy ? "●  ĐANG SUY NGHĨ…" : "●  OFFLINE • PRIVATE");
            resetButton.setEnabled(true);
            for (JButton promptButton : promptButtons) {
                promptButton.setEnabled(!busy);
            }
            updateComposerState();
        }

        private void updateComposerState() {
            boolean canSend = !isTransitioning() && !busy && !inputArea.getText().trim().isEmpty();
            sendButton.setEnabled(canSend);
            inputArea.setEnabled(!isTransitioning());
        }

        private void focusInput() {
            if (!busy && !isTransitioning()) {
                inputArea.requestFocusInWindow();
            }
        }

        private void setRevealProgress(float progress) {
            revealProgress = Math.max(0f, Math.min(1f, progress));
            updateComposerState();
            repaint();
        }

        private void scrollToBottom() {
            SwingUtilities.invokeLater(() -> {
                JScrollBar scrollBar = messagesScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            });
        }

        private boolean isNearBottom() {
            JScrollBar scrollBar = messagesScrollPane.getVerticalScrollBar();
            return scrollBar.getMaximum() - (scrollBar.getValue() + scrollBar.getVisibleAmount()) < 90;
        }

        private void trimHistory() {
            while (messageBubbles.size() > 32) {
                MessageBubble oldest = messageBubbles.remove(0);
                messagesPanel.remove(oldest);
                if (oldest.getSpacer() != null) {
                    messagesPanel.remove(oldest.getSpacer());
                }
            }
        }

        private void disposeChat() {
            conversationGeneration++;
            if (activeRequest != null) {
                activeRequest.cancel(true);
                activeRequest = null;
            }
            removeTypingBubble();
            musicPanel.disposePanel();
        }

        private void applyTheme() {
            boolean dark = isDarkMode();
            titleLabel.setForeground(UITheme.getTitleForeground(dark));
            statusLabel.setForeground(dark ? new Color(0x86EFAC) : new Color(0x15803D));
            closeButton.setForeground(UITheme.getTextForeground(dark));
            inputArea.setBackground(dark ? new Color(0x0F172A) : Color.WHITE);
            inputArea.setForeground(UITheme.getTitleForeground(dark));
            inputArea.setCaretColor(UITheme.getAccent(dark));
            sendButton.setBackground(UITheme.getAccent(dark));
            sendButton.setForeground(Color.WHITE);
            musicPanel.applyTheme();
            applyPromptTheme();
            for (MessageBubble bubble : messageBubbles) {
                bubble.applyTheme();
            }
            repaint();
        }

        private void applyPromptTheme() {
            for (JButton promptButton : promptButtons) {
                promptButton.repaint();
            }
            messagesScrollPane.getVerticalScrollBar().repaint();
        }

        private class ComposerScrollPane extends JScrollPane {
            private ComposerScrollPane(Component view) {
                super(view);
                setOpaque(false);
                setBorder(new EmptyBorder(1, 1, 1, 1));
                getViewport().setOpaque(false);
                styleVerticalScrollBar(this);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dark = isDarkMode();
                g2.setColor(dark ? new Color(0x0F172A) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(UITheme.getBorder(dark));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(graphics);
            }
        }

        private class ResizeHandler extends MouseAdapter {
            private Point pressOnScreen;
            private Rectangle startBounds;
            private int resizeEdges;

            @Override
            public void mouseMoved(MouseEvent event) {
                if (!expanded || isTransitioning() || pressOnScreen != null) {
                    return;
                }
                setCursor(Cursor.getPredefinedCursor(cursorForEdges(
                        resizeEdgesAt(event.getPoint())
                )));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (pressOnScreen == null) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (!expanded || isTransitioning() || !SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }
                resizeEdges = resizeEdgesAt(event.getPoint());
                if (resizeEdges == 0) {
                    return;
                }
                pressOnScreen = event.getLocationOnScreen();
                startBounds = FloatingChatWidget.this.getBounds();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (pressOnScreen == null || startBounds == null || resizeEdges == 0) {
                    return;
                }
                Point current = event.getLocationOnScreen();
                int deltaX = current.x - pressOnScreen.x;
                int deltaY = current.y - pressOnScreen.y;
                Rectangle requested = new Rectangle(startBounds);

                if ((resizeEdges & RESIZE_WEST) != 0) {
                    requested.x += deltaX;
                    requested.width -= deltaX;
                } else if ((resizeEdges & RESIZE_EAST) != 0) {
                    requested.width += deltaX;
                }
                if ((resizeEdges & RESIZE_NORTH) != 0) {
                    requested.y += deltaY;
                    requested.height -= deltaY;
                } else if ((resizeEdges & RESIZE_SOUTH) != 0) {
                    requested.height += deltaY;
                }

                if (requested.width < MIN_EXPANDED_SIZE.width) {
                    if ((resizeEdges & RESIZE_WEST) != 0) {
                        requested.x = startBounds.x + startBounds.width
                                - MIN_EXPANDED_SIZE.width;
                    }
                    requested.width = MIN_EXPANDED_SIZE.width;
                }
                if (requested.height < MIN_EXPANDED_SIZE.height) {
                    if ((resizeEdges & RESIZE_NORTH) != 0) {
                        requested.y = startBounds.y + startBounds.height
                                - MIN_EXPANDED_SIZE.height;
                    }
                    requested.height = MIN_EXPANDED_SIZE.height;
                }

                resizeBoundsListener.accept(requested);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                pressOnScreen = null;
                startBounds = null;
                resizeEdges = 0;
                setCursor(Cursor.getDefaultCursor());
            }
        }

        private int resizeEdgesAt(Point point) {
            int edges = 0;
            if (point.y <= RESIZE_HANDLE_SIZE) {
                edges |= RESIZE_NORTH;
            } else if (point.y >= getHeight() - RESIZE_HANDLE_SIZE - 1) {
                edges |= RESIZE_SOUTH;
            }
            if (point.x <= RESIZE_HANDLE_SIZE) {
                edges |= RESIZE_WEST;
            } else if (point.x >= getWidth() - RESIZE_HANDLE_SIZE - 1) {
                edges |= RESIZE_EAST;
            }
            return edges;
        }

        private int cursorForEdges(int edges) {
            if (edges == (RESIZE_NORTH | RESIZE_WEST)
                    || edges == (RESIZE_SOUTH | RESIZE_EAST)) {
                return Cursor.NW_RESIZE_CURSOR;
            }
            if (edges == (RESIZE_NORTH | RESIZE_EAST)
                    || edges == (RESIZE_SOUTH | RESIZE_WEST)) {
                return Cursor.NE_RESIZE_CURSOR;
            }
            if ((edges & (RESIZE_NORTH | RESIZE_SOUTH)) != 0) {
                return Cursor.N_RESIZE_CURSOR;
            }
            if ((edges & (RESIZE_WEST | RESIZE_EAST)) != 0) {
                return Cursor.W_RESIZE_CURSOR;
            }
            return Cursor.DEFAULT_CURSOR;
        }

        private class PromptTextArea extends JTextArea {
            private PromptTextArea() {
                super(2, 20);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (!getText().isEmpty()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) graphics.create();
                Color hint = UITheme.getTextForeground(isDarkMode());
                g2.setColor(new Color(hint.getRed(), hint.getGreen(), hint.getBlue(), 135));
                g2.setFont(getFont());
                Insets insets = getInsets();
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString("Hỏi Mizuki điều gì đó…", insets.left, insets.top + metrics.getAscent());
                g2.dispose();
            }
        }

        @Override
        public void paint(Graphics graphics) {
            if (revealProgress >= 0.999f) {
                super.paint(graphics);
                return;
            }
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(revealProgress));
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            g2.setColor(new Color(15, 23, 42, isDarkMode() ? 105 : 45));
            g2.fillRoundRect(6, 8, width - 8, height - 8, 24, 24);
            g2.setColor(UITheme.getPanelBackground(isDarkMode()));
            g2.fillRoundRect(0, 0, width - 8, height - 8, 24, 24);
            g2.setColor(UITheme.getBorder(isDarkMode()));
            g2.drawRoundRect(0, 0, width - 9, height - 9, 24, 24);
            g2.dispose();
        }
    }

    private void styleVerticalScrollBar(JScrollPane scrollPane) {
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setUI(new ModernScrollBarUI());
        scrollBar.setOpaque(false);
        scrollBar.setPreferredSize(new Dimension(10, 0));
        scrollBar.setUnitIncrement(14);
    }

    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createHiddenButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createHiddenButton();
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(8, 34);
        }

        @Override
        protected void paintTrack(Graphics graphics, JComponent component,
                                  Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            Color border = UITheme.getBorder(isDarkMode());
            g2.setColor(new Color(
                    border.getRed(),
                    border.getGreen(),
                    border.getBlue(),
                    isDarkMode() ? 40 : 55
            ));
            int x = trackBounds.x + Math.max(2, (trackBounds.width - 4) / 2);
            g2.fillRoundRect(x, trackBounds.y + 3, 4,
                    Math.max(0, trackBounds.height - 6), 4, 4);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics graphics, JComponent component,
                                  Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            Color base = isThumbRollover()
                    ? UITheme.getAccent(isDarkMode())
                    : UITheme.getGlow(isDarkMode());
            g2.setColor(new Color(
                    base.getRed(),
                    base.getGreen(),
                    base.getBlue(),
                    isThumbRollover() ? 215 : 145
            ));
            int x = thumbBounds.x + 2;
            int y = thumbBounds.y + 2;
            int width = Math.max(5, thumbBounds.width - 4);
            int height = Math.max(8, thumbBounds.height - 4);
            g2.fillRoundRect(x, y, width, height, width, width);
            g2.dispose();
        }

        private JButton createHiddenButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            button.setOpaque(false);
            button.setFocusable(false);
            button.setBorder(null);
            return button;
        }
    }

    private class MessageBubble extends JPanel {
        private final JTextArea textArea = new JTextArea();
        private final BubbleBody body;
        private final JLabel sourceLabel = new JLabel();
        private final boolean fromUser;
        private Component spacer;

        private MessageBubble(String text, boolean fromUser, String source) {
            this.fromUser = fromUser;
            setOpaque(false);
            setLayout(new BorderLayout());
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            textArea.setText(text);
            textArea.setEditable(false);
            textArea.setFocusable(true);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setOpaque(false);
            textArea.setFont(new Font(CONTENT_FONT, Font.PLAIN, 13));
            textArea.setColumns(preferredColumns(text));
            textArea.setBorder(null);
            textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            textArea.setToolTipText("Có thể bôi đen và sao chép nội dung");

            body = new BubbleBody();
            body.setLayout(new BorderLayout());
            body.setBorder(new EmptyBorder(9, 11, 9, 11));
            body.add(textArea, BorderLayout.CENTER);

            JPanel stack = new JPanel();
            stack.setOpaque(false);
            stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
            body.setAlignmentX(fromUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            stack.add(body);
            if (!fromUser && source != null && !source.isBlank()) {
                sourceLabel.setText(source.toUpperCase());
                sourceLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 10));
                sourceLabel.setBorder(new EmptyBorder(5, 4, 0, 2));
                sourceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                stack.add(sourceLabel);
            }
            add(stack, fromUser ? BorderLayout.EAST : BorderLayout.WEST);
            applyTheme();
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension preferred = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, preferred.height);
        }

        private void applyTheme() {
            boolean dark = isDarkMode();
            textArea.setForeground(fromUser ? Color.WHITE : UITheme.getTitleForeground(dark));
            sourceLabel.setForeground(dark ? new Color(0x94A3B8) : new Color(0x64748B));
            body.repaint();
        }

        private void setText(String text) {
            textArea.setText(text);
            textArea.setColumns(preferredColumns(text));
            revalidate();
            repaint();
        }

        private int preferredColumns(String text) {
            int longestLine = 0;
            for (String line : text.split("\\R", -1)) {
                longestLine = Math.max(longestLine, line.codePointCount(0, line.length()));
            }
            int minimum = fromUser ? 7 : 12;
            int maximum = fromUser ? 25 : 31;
            return Math.max(minimum, Math.min(maximum, longestLine + 1));
        }

        private void setSpacer(Component spacer) {
            this.spacer = spacer;
        }

        private Component getSpacer() {
            return spacer;
        }

        private class BubbleBody extends JPanel {
            private BubbleBody() {
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = fromUser
                        ? new Color(0xC2410C)
                        : UITheme.getCardBackground(isDarkMode());
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (!fromUser) {
                    g2.setColor(UITheme.getBorder(isDarkMode()));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
                g2.dispose();
                super.paintComponent(graphics);
            }
        }
    }
}
