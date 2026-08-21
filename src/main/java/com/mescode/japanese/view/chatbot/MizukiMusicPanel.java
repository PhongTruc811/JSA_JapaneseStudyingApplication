package com.mescode.japanese.view.chatbot;

import com.mescode.japanese.service.music.MusicPlayer;
import com.mescode.japanese.service.music.MusicState;
import com.mescode.japanese.view.theme.UITheme;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Compact full-featured controller for the application-wide Mizuki playlist.
 * Transport icons are painted with Graphics2D so they render consistently
 * without relying on symbol fonts installed on the operating system.
 */
final class MizukiMusicPanel extends JPanel {
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font UI_FONT_MEDIUM = new Font("Segoe UI Semibold", Font.BOLD, 12);
    private static final BufferedImage MIZUKI_MUSIC_PET = loadPetImage();

    private final MusicPlayer musicPlayer;
    private final BooleanSupplier darkModeSupplier;
    private final Runnable pulseCallback;
    private final Consumer<MusicState> stateListener = this::applyState;

    private final RoundedSurface playerCard = new RoundedSurface(18);
    private final MiniMizukiPet artwork = new MiniMizukiPet();
    private final JLabel titleLabel = new JLabel("Chưa có nhạc");
    private final JLabel artistLabel = new JLabel("Playlist của Mizuki");
    private final JLabel elapsedLabel = timeLabel("0:00", SwingConstants.LEFT);
    private final JLabel durationLabel = timeLabel("0:00", SwingConstants.RIGHT);
    private final JSlider seekSlider = new JSlider(0, 1000, 0);
    private final JSlider volumeSlider = new JSlider(0, 100, 35);
    private final PlayerButton previousButton = iconButton(
            PlayerIcon.Type.PREVIOUS, "Bài trước", false
    );
    private final PlayerButton playButton = iconButton(
            PlayerIcon.Type.PLAY, "Phát hoặc tạm dừng nhạc", true
    );
    private final PlayerButton nextButton = iconButton(
            PlayerIcon.Type.NEXT, "Bài tiếp theo", false
    );
    private final PlayerButton muteButton = textButton("Tắt tiếng", "Bật hoặc tắt tiếng");
    private final JLabel volumeLabel = new JLabel("Âm lượng");

    private MusicState state;
    private boolean applyingState;
    private boolean showingPauseIcon;

    MizukiMusicPanel(MusicPlayer musicPlayer,
                     BooleanSupplier darkModeSupplier,
                     Runnable pulseCallback) {
        this.musicPlayer = Objects.requireNonNullElse(musicPlayer, MusicPlayer.unavailable());
        this.darkModeSupplier = darkModeSupplier == null ? () -> true : darkModeSupplier;
        this.pulseCallback = pulseCallback == null ? () -> { } : pulseCallback;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(9, 12, 10, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 196));
        buildUi();
        wireActions();
        applyTheme();
        this.musicPlayer.addListener(stateListener);
    }

    private void buildUi() {
        playerCard.setLayout(new BoxLayout(playerCard, BoxLayout.Y_AXIS));
        playerCard.setBorder(new EmptyBorder(10, 13, 9, 13));
        add(playerCard, BorderLayout.CENTER);

        JPanel nowPlaying = transparentPanel(new BorderLayout(11, 0));
        artwork.setPreferredSize(new Dimension(60, 60));
        artwork.setMinimumSize(new Dimension(60, 60));
        nowPlaying.add(artwork, BorderLayout.WEST);

        JPanel metadata = transparentPanel();
        metadata.setLayout(new BoxLayout(metadata, BoxLayout.Y_AXIS));
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metadata.add(Box.createVerticalGlue());
        metadata.add(titleLabel);
        metadata.add(Box.createVerticalStrut(3));
        metadata.add(artistLabel);
        metadata.add(Box.createVerticalGlue());
        nowPlaying.add(metadata, BorderLayout.CENTER);

        playerCard.add(nowPlaying);
        playerCard.add(Box.createVerticalStrut(6));

        JPanel timeline = transparentPanel(new BorderLayout(8, 0));
        seekSlider.setOpaque(false);
        seekSlider.setFocusable(true);
        seekSlider.setBorder(null);
        seekSlider.getAccessibleContext().setAccessibleName("Vị trí bài nhạc");
        elapsedLabel.setPreferredSize(new Dimension(35, 20));
        durationLabel.setPreferredSize(new Dimension(35, 20));
        timeline.add(elapsedLabel, BorderLayout.WEST);
        timeline.add(seekSlider, BorderLayout.CENTER);
        timeline.add(durationLabel, BorderLayout.EAST);
        playerCard.add(timeline);
        playerCard.add(Box.createVerticalStrut(6));

        JPanel transport = transparentPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        previousButton.setPreferredSize(new Dimension(34, 34));
        playButton.setPreferredSize(new Dimension(42, 42));
        nextButton.setPreferredSize(new Dimension(34, 34));
        transport.add(previousButton);
        transport.add(playButton);
        transport.add(nextButton);
        playerCard.add(transport);
        playerCard.add(Box.createVerticalStrut(6));

        JPanel volume = transparentPanel(new BorderLayout(8, 0));
        volumeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        volumeLabel.setPreferredSize(new Dimension(48, 26));
        volumeSlider.setOpaque(false);
        volumeSlider.setFocusable(true);
        volumeSlider.setBorder(null);
        volumeSlider.getAccessibleContext().setAccessibleName("Âm lượng nhạc");
        muteButton.setPreferredSize(new Dimension(77, 28));
        volume.add(volumeLabel, BorderLayout.WEST);
        volume.add(volumeSlider, BorderLayout.CENTER);
        volume.add(muteButton, BorderLayout.EAST);
        playerCard.add(volume);
    }

    private void wireActions() {
        previousButton.addActionListener(event -> musicPlayer.previous());
        playButton.addActionListener(event -> musicPlayer.togglePlayback());
        nextButton.addActionListener(event -> musicPlayer.next());
        muteButton.addActionListener(event ->
                musicPlayer.setMuted(state == null || !state.muted()));
        volumeSlider.addChangeListener(event -> {
            if (!applyingState) {
                musicPlayer.setVolume(volumeSlider.getValue() / 100f);
            }
        });
        seekSlider.addChangeListener(event -> {
            if (applyingState || state == null || state.durationMillis() <= 0L
                    || seekSlider.getValueIsAdjusting()) {
                return;
            }
            long target = Math.round(
                    state.durationMillis() * seekSlider.getValue() / 1000d
            );
            musicPlayer.seek(Duration.ofMillis(target));
        });
    }

    private PlayerButton iconButton(PlayerIcon.Type type,
                                    String accessibleName,
                                    boolean primary) {
        PlayerButton button = new PlayerButton("", primary);
        button.setIcon(new PlayerIcon(type, primary ? 17 : 14));
        configureButton(button, accessibleName);
        return button;
    }

    private PlayerButton textButton(String text, String accessibleName) {
        PlayerButton button = new PlayerButton(text, false);
        button.setFont(UI_FONT_MEDIUM);
        configureButton(button, accessibleName);
        return button;
    }

    private void configureButton(JButton button, String accessibleName) {
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(4, 8, 4, 8));
        button.setToolTipText(accessibleName);
        button.getAccessibleContext().setAccessibleName(accessibleName);
    }

    private static JPanel transparentPanel() {
        return transparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    }

    private static JPanel transparentPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel timeLabel(String value, int alignment) {
        JLabel label = new JLabel(value, alignment);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        return label;
    }

    private void applyState(MusicState nextState) {
        state = nextState;
        applyingState = true;
        try {
            boolean available = nextState != null && nextState.available();
            if (available) {
                titleLabel.setText(nextState.track().title());
                String artist = nextState.track().artist();
                artistLabel.setText(artist.isBlank() ? "Playlist của Mizuki" : artist);
                elapsedLabel.setText(formatTime(nextState.positionMillis()));
                durationLabel.setText(formatTime(nextState.durationMillis()));
                int seekValue = nextState.durationMillis() <= 0L
                        ? 0
                        : (int) Math.min(1000L,
                                nextState.positionMillis() * 1000L / nextState.durationMillis());
                if (!seekSlider.getValueIsAdjusting()) {
                    seekSlider.setValue(seekValue);
                }
                updatePlayIcon(nextState.playing());
                playButton.getAccessibleContext().setAccessibleName(
                        nextState.playing() ? "Tạm dừng nhạc" : "Phát nhạc"
                );
                muteButton.setText(nextState.muted() ? "Bật tiếng" : "Tắt tiếng");
                muteButton.setSelected(nextState.muted());
                volumeSlider.setValue(Math.round(nextState.volume() * 100f));
                playerCard.setToolTipText(nextState.errorMessage().isBlank()
                        ? null
                        : nextState.errorMessage());
                artwork.setPlaying(nextState.playing());
            } else {
                titleLabel.setText("Chưa có nhạc");
                artistLabel.setText("Thêm WAV/MP3 vào playlist của Mizuki");
                elapsedLabel.setText("0:00");
                durationLabel.setText("0:00");
                seekSlider.setValue(0);
                updatePlayIcon(false);
                muteButton.setSelected(false);
                playerCard.setToolTipText(
                        nextState == null || nextState.errorMessage().isBlank()
                                ? null
                                : nextState.errorMessage()
                );
                artwork.setPlaying(false);
            }
            previousButton.setEnabled(available);
            playButton.setEnabled(available);
            nextButton.setEnabled(available);
            muteButton.setEnabled(available);
            seekSlider.setEnabled(available && nextState.durationMillis() > 0L);
            volumeSlider.setEnabled(available);
        } finally {
            applyingState = false;
        }
        pulseCallback.run();
        revalidate();
        repaint();
    }

    void applyTheme() {
        boolean dark = darkModeSupplier.getAsBoolean();
        Color outside = dark ? new Color(0x172236) : new Color(0xF3F7FB);
        Color card = dark ? new Color(0x111C2F) : Color.WHITE;
        Color title = UITheme.getTitleForeground(dark);
        Color secondary = dark ? new Color(0x94A3B8) : new Color(0x64748B);
        Color accent = UITheme.getAccent(dark);
        Color glow = UITheme.getGlow(dark);
        Color border = dark ? new Color(0x34445F) : new Color(0xD8E2EC);
        Color control = dark ? new Color(0x25344C) : new Color(0xEDF3F8);

        setOpaque(true);
        setBackground(outside);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, border),
                new EmptyBorder(9, 12, 10, 12)
        ));
        playerCard.setSurface(card, border);
        artwork.setPalette(accent, glow, card);
        titleLabel.setForeground(title);
        artistLabel.setForeground(secondary);
        elapsedLabel.setForeground(secondary);
        durationLabel.setForeground(secondary);
        volumeLabel.setForeground(secondary);

        for (PlayerButton button : new PlayerButton[]{
                previousButton, nextButton, muteButton
        }) {
            button.setPalette(
                    control,
                    dark ? new Color(0x31435F) : new Color(0xDEEAF3),
                    accent,
                    title
            );
        }
        playButton.setPalette(
                accent,
                accent.brighter(),
                accent.darker(),
                Color.WHITE
        );
        muteButton.setActiveColor(accent);

        seekSlider.setUI(new MusicSliderUI(seekSlider, accent, border, 12));
        volumeSlider.setUI(new MusicSliderUI(volumeSlider, glow, border, 10));
        repaint();
    }

    private void updatePlayIcon(boolean playing) {
        if (playing == showingPauseIcon && playButton.getIcon() != null) {
            return;
        }
        showingPauseIcon = playing;
        playButton.setIcon(new PlayerIcon(
                playing ? PlayerIcon.Type.PAUSE : PlayerIcon.Type.PLAY,
                17
        ));
    }

    void disposePanel() {
        musicPlayer.removeListener(stateListener);
        artwork.disposePet();
    }

    private String formatTime(long millis) {
        long totalSeconds = Math.max(0L, millis) / 1000L;
        return "%d:%02d".formatted(totalSeconds / 60L, totalSeconds % 60L);
    }

    private static Color translucent(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static final class RoundedSurface extends JPanel {
        private final int radius;
        private Color surface = Color.WHITE;
        private Color outline = Color.LIGHT_GRAY;

        private RoundedSurface(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        private void setSurface(Color surface, Color outline) {
            this.surface = surface;
            this.outline = outline;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(surface);
            copy.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            copy.setColor(outline);
            copy.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }

    private static BufferedImage loadPetImage() {
        try {
            return ImageIO.read(
                    MizukiMusicPanel.class.getResource("/icons/mizuki-music-pet.png")
            );
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private static final class MiniMizukiPet extends JPanel {
        private Color accent = Color.ORANGE;
        private Color glow = Color.CYAN;
        private Color background = Color.DARK_GRAY;
        private final javax.swing.Timer danceTimer;
        private boolean playing;
        private int animationFrame;

        private MiniMizukiPet() {
            setOpaque(false);
            setToolTipText("Mini Mizuki đang nghe nhạc");
            getAccessibleContext().setAccessibleName("Pet Mini Mizuki");
            danceTimer = new javax.swing.Timer(110, event -> {
                animationFrame = (animationFrame + 1) % 24;
                repaint();
            });
            danceTimer.setCoalesce(true);
        }

        private void setPalette(Color accent, Color glow, Color background) {
            this.accent = accent;
            this.glow = glow;
            this.background = background;
        }

        private void setPlaying(boolean playing) {
            this.playing = playing;
            if (playing && isShowing()) {
                if (!danceTimer.isRunning()) {
                    danceTimer.start();
                }
            } else {
                danceTimer.stop();
                animationFrame = 0;
            }
            repaint();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (playing && !danceTimer.isRunning()) {
                danceTimer.start();
            }
        }

        @Override
        public void removeNotify() {
            danceTimer.stop();
            super.removeNotify();
        }

        private void disposePet() {
            danceTimer.stop();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            copy.setPaint(new GradientPaint(
                    0, 0, translucent(accent, 68),
                    size, size, translucent(glow, 48)
            ));
            copy.fillOval(3, 4, size - 6, size - 7);
            copy.setColor(translucent(background, 55));
            copy.fillOval(8, 9, size - 16, size - 17);

            if (MIZUKI_MUSIC_PET != null) {
                double scale = Math.min(
                        (size - 3d) / MIZUKI_MUSIC_PET.getWidth(),
                        (size - 2d) / MIZUKI_MUSIC_PET.getHeight()
                );
                int drawWidth = (int) Math.round(MIZUKI_MUSIC_PET.getWidth() * scale);
                int drawHeight = (int) Math.round(MIZUKI_MUSIC_PET.getHeight() * scale);
                double wave = playing
                        ? Math.sin(animationFrame * Math.PI / 6d)
                        : 0d;
                int drawX = (getWidth() - drawWidth) / 2;
                int drawY = (getHeight() - drawHeight) / 2 + (int) Math.round(wave * 1.5d);
                double angle = playing ? Math.toRadians(wave * 2.2d) : 0d;
                AffineTransform original = copy.getTransform();
                copy.rotate(
                        angle,
                        drawX + drawWidth / 2d,
                        drawY + drawHeight * 0.72d
                );
                copy.drawImage(
                        MIZUKI_MUSIC_PET,
                        drawX,
                        drawY,
                        drawWidth,
                        drawHeight,
                        null
                );
                copy.setTransform(original);
            } else {
                copy.setColor(Color.WHITE);
                copy.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
                copy.drawString("M", size / 2 - 4, size / 2 + 4);
            }

            if (playing && animationFrame % 12 < 7) {
                copy.setColor(new Color(255, 255, 255, 220));
                copy.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                copy.drawLine(size - 10, 6, size - 10, 13);
                copy.drawLine(size - 10, 6, size - 5, 5);
                copy.fillOval(size - 14, 11, 5, 4);
            }
            copy.dispose();
        }
    }

    private static final class PlayerButton extends JButton {
        private final boolean round;
        private Color base = Color.GRAY;
        private Color hover = Color.LIGHT_GRAY;
        private Color pressed = Color.DARK_GRAY;
        private Color text = Color.WHITE;
        private Color active = Color.ORANGE;

        private PlayerButton(String text, boolean round) {
            super(text);
            this.round = round;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
        }

        private void setPalette(Color base,
                                Color hover,
                                Color pressed,
                                Color text) {
            this.base = base;
            this.hover = hover;
            this.pressed = pressed;
            this.text = text;
            setForeground(text);
        }

        private void setActiveColor(Color active) {
            this.active = active;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = isSelected() ? translucent(active, 48) : base;
            if (!isEnabled()) {
                fill = translucent(base, 105);
            } else if (getModel().isPressed()) {
                fill = pressed;
            } else if (getModel().isRollover()) {
                fill = hover;
            }
            copy.setColor(fill);
            if (round) {
                int diameter = Math.min(getWidth(), getHeight()) - 2;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                copy.fillOval(x, y, diameter, diameter);
            } else {
                copy.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            copy.dispose();

            setForeground(isSelected() ? active : text);
            super.paintComponent(graphics);
        }
    }

    private static final class PlayerIcon implements Icon {
        private enum Type {
            PREVIOUS,
            PLAY,
            PAUSE,
            NEXT
        }

        private final Type type;
        private final int size;

        private PlayerIcon(Type type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(component.getForeground());
            int centerY = y + size / 2;
            switch (type) {
                case PLAY -> {
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x + 4, y + 2);
                    triangle.addPoint(x + size - 2, centerY);
                    triangle.addPoint(x + 4, y + size - 2);
                    copy.fill(triangle);
                }
                case PAUSE -> {
                    int barWidth = Math.max(3, size / 4);
                    copy.fillRoundRect(x + 3, y + 2, barWidth, size - 4, 2, 2);
                    copy.fillRoundRect(
                            x + size - barWidth - 3, y + 2, barWidth, size - 4, 2, 2
                    );
                }
                case PREVIOUS -> {
                    copy.fillRoundRect(x + 1, y + 2, 3, size - 4, 2, 2);
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x + size - 2, y + 2);
                    triangle.addPoint(x + 5, centerY);
                    triangle.addPoint(x + size - 2, y + size - 2);
                    copy.fill(triangle);
                }
                case NEXT -> {
                    copy.fillRoundRect(x + size - 4, y + 2, 3, size - 4, 2, 2);
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x + 2, y + 2);
                    triangle.addPoint(x + size - 5, centerY);
                    triangle.addPoint(x + 2, y + size - 2);
                    copy.fill(triangle);
                }
            }
            copy.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static final class MusicSliderUI extends BasicSliderUI {
        private final Color accent;
        private final Color track;
        private final int thumbSize;

        private MusicSliderUI(JSlider slider, Color accent, Color track, int thumbSize) {
            super(slider);
            this.accent = accent;
            this.track = track;
            this.thumbSize = thumbSize;
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(thumbSize, thumbSize);
        }

        @Override
        public void paintTrack(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = trackRect.y + trackRect.height / 2 - 2;
            int width = Math.max(1, trackRect.width);
            int progress = thumbRect.x + thumbRect.width / 2 - trackRect.x;
            copy.setColor(track);
            copy.fill(new RoundRectangle2D.Float(trackRect.x, y, width, 4, 4, 4));
            copy.setColor(accent);
            copy.fill(new RoundRectangle2D.Float(
                    trackRect.x, y, Math.max(0, progress), 4, 4, 4
            ));
            copy.dispose();
        }

        @Override
        public void paintThumb(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(0, 0, 0, 45));
            copy.fillOval(thumbRect.x + 1, thumbRect.y + 2, thumbRect.width, thumbRect.height);
            copy.setColor(accent);
            copy.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            copy.setColor(new Color(255, 255, 255, 160));
            copy.drawOval(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);
            copy.dispose();
        }
    }
}
