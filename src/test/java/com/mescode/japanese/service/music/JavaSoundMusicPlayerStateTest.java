package com.mescode.japanese.service.music;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSoundMusicPlayerStateTest {

    @Test
    void controls_shouldUpdateSessionStateWithoutAutoplay() {
        JavaSoundMusicPlayer player = new JavaSoundMusicPlayer(List.of(
                track("one", "/missing-one.wav"),
                track("two", "/missing-two.mp3")
        ));
        try {
            MusicState initial = player.snapshot();
            assertEquals(PlaybackStatus.STOPPED, initial.status());
            assertEquals(0.35f, initial.volume(), 0.001f);
            assertEquals(RepeatMode.ALL, initial.repeatMode());
            assertFalse(initial.playing());

            player.setVolume(0.6f);
            player.setMuted(true);
            player.setShuffle(true);
            player.setRepeatMode(RepeatMode.ONE);
            player.next();
            player.seek(Duration.ofSeconds(12));

            MusicState changed = player.snapshot();
            assertEquals("two", changed.track().id());
            assertEquals(12_000L, changed.positionMillis());
            assertEquals(0.6f, changed.volume(), 0.001f);
            assertTrue(changed.muted());
            assertTrue(changed.shuffle());
            assertEquals(RepeatMode.ONE, changed.repeatMode());
            assertFalse(changed.playing());
        } finally {
            player.close();
        }
    }

    @Test
    void listeners_shouldBeDeliveredOnSwingEventThread() throws Exception {
        JavaSoundMusicPlayer player = new JavaSoundMusicPlayer(List.of(
                track("one", "/missing-one.wav")
        ));
        CountDownLatch delivered = new CountDownLatch(1);
        AtomicBoolean deliveredOnEdt = new AtomicBoolean();
        try {
            player.addListener(state -> {
                deliveredOnEdt.set(SwingUtilities.isEventDispatchThread());
                delivered.countDown();
            });

            assertTrue(delivered.await(2, TimeUnit.SECONDS));
            assertTrue(deliveredOnEdt.get());
        } finally {
            player.close();
        }
    }

    @Test
    void play_shouldExposeReadableErrorWhenResourceIsMissing() throws Exception {
        JavaSoundMusicPlayer player = new JavaSoundMusicPlayer(List.of(
                track("one", "/definitely-missing.wav")
        ));
        try {
            player.play();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (player.snapshot().status() != PlaybackStatus.ERROR
                    && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }

            MusicState state = player.snapshot();
            assertEquals(PlaybackStatus.ERROR, state.status());
            assertTrue(state.errorMessage().contains("Không thể phát"));
        } finally {
            player.close();
        }
    }

    private MusicTrack track(String id, String resource) {
        return new MusicTrack(id, id, "Mizuki", resource);
    }
}
