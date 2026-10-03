package com.mescode.japanese.app.navigation;

import com.mescode.japanese.app.context.AppContext;
import com.mescode.japanese.service.music.MusicPlayer;
import com.mescode.japanese.service.music.MusicState;
import com.mescode.japanese.service.music.RepeatMode;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppNavigatorTest {

    @Test
    void getAppContext_shouldReturnTheContextProvidedToNavigator() {
        AppContext context = new AppContext();
        AppNavigator navigator = new AppNavigator(context, new RecordingMusicPlayer());

        try {
            assertSame(context, navigator.getAppContext());
        } finally {
            navigator.close();
        }
    }

    @Test
    void constructor_shouldUseUnavailableMusicPlayerWhenInjectedPlayerIsNull() {
        AppNavigator navigator = new AppNavigator(null, null);

        try {
            assertDoesNotThrow(navigator::close);
        } finally {
            navigator.close();
        }
    }

    @Test
    void restartKanaQuiz_shouldRejectNonKanaRoutes() {
        AppNavigator navigator = navigator();

        try {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> navigator.restartKanaQuiz(AppRoute.Vocab, null)
            );
            assertEquals("Kana quiz option required", exception.getMessage());
        } finally {
            navigator.close();
        }
    }

    @Test
    void restartKanaQuiz_shouldRequireOptions() {
        AppNavigator navigator = navigator();

        try {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> navigator.restartKanaQuiz(AppRoute.HiraQuiz, null)
            );
            assertEquals("Kana quiz options required", exception.getMessage());
        } finally {
            navigator.close();
        }
    }

    @Test
    void restartVocabQuiz_shouldRequireConfiguration() {
        AppNavigator navigator = navigator();

        try {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> navigator.restartVocabQuiz(null)
            );
            assertEquals("Vocabulary quiz config required", exception.getMessage());
        } finally {
            navigator.close();
        }
    }

    @Test
    void restartPeTrialQuiz_shouldRequireDifficulty() {
        AppNavigator navigator = navigator();

        try {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> navigator.restartPeTrialQuiz(null)
            );
            assertEquals("PE Trial difficulty required", exception.getMessage());
        } finally {
            navigator.close();
        }
    }

    @Test
    void openGrammarLesson_shouldRequireAChapterId() {
        AppNavigator navigator = navigator();

        try {
            assertThrows(IllegalArgumentException.class,
                    () -> navigator.openGrammarLesson(null));
            assertThrows(IllegalArgumentException.class,
                    () -> navigator.openGrammarLesson("   "));
        } finally {
            navigator.close();
        }
    }

    @Test
    void openGrammarQuiz_shouldRequireAChapterId() {
        AppNavigator navigator = navigator();

        try {
            assertThrows(IllegalArgumentException.class,
                    () -> navigator.openGrammarQuiz(null));
            assertThrows(IllegalArgumentException.class,
                    () -> navigator.openGrammarQuiz("   "));
        } finally {
            navigator.close();
        }
    }

    @Test
    void close_shouldCloseMusicPlayerOnlyOnce() {
        RecordingMusicPlayer musicPlayer = new RecordingMusicPlayer();
        AppNavigator navigator = new AppNavigator(null, musicPlayer);

        navigator.close();
        navigator.close();

        assertEquals(1, musicPlayer.closeCount);
    }

    private AppNavigator navigator() {
        return new AppNavigator(null, new RecordingMusicPlayer());
    }

    private static final class RecordingMusicPlayer implements MusicPlayer {
        private int closeCount;

        @Override
        public void play() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void next() {
        }

        @Override
        public void previous() {
        }

        @Override
        public void seek(Duration position) {
        }

        @Override
        public void setVolume(float volume) {
        }

        @Override
        public void setMuted(boolean muted) {
        }

        @Override
        public void setShuffle(boolean shuffle) {
        }

        @Override
        public void setRepeatMode(RepeatMode repeatMode) {
        }

        @Override
        public MusicState snapshot() {
            return new MusicState(null, -1, 0, null, 0, 0, 0.35f, false,
                    false, RepeatMode.ALL, "");
        }

        @Override
        public void addListener(Consumer<MusicState> listener) {
        }

        @Override
        public void removeListener(Consumer<MusicState> listener) {
        }

        @Override
        public void close() {
            closeCount++;
        }
    }
}
