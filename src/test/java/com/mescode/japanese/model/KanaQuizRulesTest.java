package com.mescode.japanese.model;

import com.mescode.japanese.model.kana.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KanaQuizRulesTest {

    @Test
    void difficulties_shouldExposeExpectedDurationsAndAnswerRules() {
        assertEquals(180, KanaQuizDifficulty.EASY.getDurationSeconds());
        assertTrue(KanaQuizDifficulty.EASY.isAnswerRevealEnabled());

        assertEquals(90, KanaQuizDifficulty.MEDIUM.getDurationSeconds());
        assertTrue(KanaQuizDifficulty.MEDIUM.isAnswerRevealEnabled());

        assertEquals(60, KanaQuizDifficulty.HARD.getDurationSeconds());
        assertFalse(KanaQuizDifficulty.HARD.isAnswerRevealEnabled());
    }

    @Test
    void formatTime_shouldUseMinuteSecondDisplay() {
        assertEquals("03:00", KanaQuizCountdown.formatTime(180));
        assertEquals("01:30", KanaQuizCountdown.formatTime(90));
        assertEquals("01:00", KanaQuizCountdown.formatTime(60));
        assertEquals("00:00", KanaQuizCountdown.formatTime(-1));
    }

    @Test
    void tick_shouldReportExpirationOnlyOnce() {
        KanaQuizCountdown countdown = new KanaQuizCountdown(2);

        KanaQuizCountdown.TickResult first = countdown.tick();
        KanaQuizCountdown.TickResult second = countdown.tick();
        KanaQuizCountdown.TickResult third = countdown.tick();

        assertEquals(1, first.remainingSeconds());
        assertFalse(first.expiredNow());
        assertEquals(0, second.remainingSeconds());
        assertTrue(second.expiredNow());
        assertEquals(0, third.remainingSeconds());
        assertFalse(third.expiredNow());
    }

    @Test
    void warningTime_shouldStartAtTenSeconds() {
        KanaQuizCountdown countdown = new KanaQuizCountdown(11);

        assertFalse(countdown.isWarningTime());
        countdown.tick();
        assertTrue(countdown.isWarningTime());
    }

    @Test
    void negativeDuration_shouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new KanaQuizCountdown(-1));
    }

    @Test
    void groups_shouldMatchExpectedKanaTypes() {
        assertTrue(KanaQuizGroup.ALL.matches(KanaType.gojuuon));
        assertTrue(KanaQuizGroup.ALL.matches(KanaType.dakuon));
        assertTrue(KanaQuizGroup.ALL.matches(KanaType.handakuon));
        assertTrue(KanaQuizGroup.ALL.matches(KanaType.youon));
        assertTrue(KanaQuizGroup.GOJUUON.matches(KanaType.gojuuon));
        assertTrue(KanaQuizGroup.DAKUON.matches(KanaType.dakuon));
        assertTrue(KanaQuizGroup.DAKUON.matches(KanaType.handakuon));
        assertTrue(KanaQuizGroup.YOUON.matches(KanaType.youon));
        assertFalse(KanaQuizGroup.GOJUUON.matches(KanaType.dakuon));
    }

    @Test
    void sessionStats_shouldTrackAttemptsRevealsAndStreaks() {
        KanaQuizSessionStats stats = new KanaQuizSessionStats();

        stats.recordCorrect();
        stats.recordCorrect();
        stats.recordAnswerReveal();
        stats.recordWrong();

        KanaQuizSessionStats.Snapshot snapshot = stats.snapshot();
        assertEquals(2, snapshot.correctAnswers());
        assertEquals(1, snapshot.wrongAnswers());
        assertEquals(1, snapshot.answerReveals());
        assertEquals(0, snapshot.currentStreak());
        assertEquals(2, snapshot.bestStreak());
    }
}
