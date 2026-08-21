package com.mescode.japanese.service.music;

import javax.swing.SwingUtilities;
import java.time.Duration;
import java.util.function.Consumer;

public interface MusicPlayer extends AutoCloseable {

    void play();

    void pause();

    default void togglePlayback() {
        if (snapshot().playing()) {
            pause();
        } else {
            play();
        }
    }

    void next();

    void previous();

    void seek(Duration position);

    void setVolume(float volume);

    void setMuted(boolean muted);

    void setShuffle(boolean shuffle);

    void setRepeatMode(RepeatMode repeatMode);

    MusicState snapshot();

    void addListener(Consumer<MusicState> listener);

    void removeListener(Consumer<MusicState> listener);

    @Override
    void close();

    static MusicPlayer unavailable() {
        return UnavailableHolder.INSTANCE;
    }

    final class UnavailableHolder {
        private static final MusicState STATE = new MusicState(
                null, -1, 0, PlaybackStatus.UNAVAILABLE,
                0L, 0L, 0.35f, false, false, RepeatMode.ALL,
                "Chưa có nhạc"
        );
        private static final MusicPlayer INSTANCE = new MusicPlayer() {
            @Override public void play() { }
            @Override public void pause() { }
            @Override public void next() { }
            @Override public void previous() { }
            @Override public void seek(Duration position) { }
            @Override public void setVolume(float volume) { }
            @Override public void setMuted(boolean muted) { }
            @Override public void setShuffle(boolean shuffle) { }
            @Override public void setRepeatMode(RepeatMode repeatMode) { }
            @Override public MusicState snapshot() { return STATE; }
            @Override public void addListener(Consumer<MusicState> listener) {
                if (listener == null) {
                    return;
                }
                if (SwingUtilities.isEventDispatchThread()) {
                    listener.accept(STATE);
                } else {
                    SwingUtilities.invokeLater(() -> listener.accept(STATE));
                }
            }
            @Override public void removeListener(Consumer<MusicState> listener) { }
            @Override public void close() { }
        };

        private UnavailableHolder() {
        }
    }
}
