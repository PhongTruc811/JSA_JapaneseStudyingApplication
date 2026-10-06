package com.mescode.japanese.controller;

import com.mescode.japanese.model.kana.*;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.view.kana.KanaQuizFrame_Interface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Làm bài Kana và giới hạn thời gian")
class KanaQuizTest {
    @Test
    @DisplayName("Đáp án đúng và sai cập nhật điểm cùng chuỗi trả lời đúng")
    void answersUpdateScoreAndStreak() {
        var view = startQuiz();
        view.input = "a";
        view.submit.run();
        assertEquals(1, view.score);
        assertEquals(1, view.stats.correctAnswers());
        assertEquals(1, view.stats.currentStreak());
        assertEquals("", view.input);
        view.input = "wrong";
        view.submit.run();
        assertEquals(0, view.score);
        assertEquals(1, view.stats.wrongAnswers());
        assertEquals(0, view.stats.currentStreak());
        assertEquals(1, view.stats.bestStreak());
    }

    @Test
    @DisplayName("Mỗi lượt chỉ ghi nhận xem đáp án một lần")
    void revealIsCountedOncePerQuestion() {
        var view = startQuiz();
        view.reveal.run();
        view.reveal.run();
        assertEquals(1, view.stats.answerReveals());
        assertTrue(view.answer.contains("あ"));
        view.input = "a";
        view.submit.run();
        view.reveal.run();
        assertEquals(2, view.stats.answerReveals());
    }

    @Test
    @DisplayName("Độ khó quy định thời gian và quyền xem đáp án")
    void difficultyRules() {
        assertEquals(180, KanaQuizDifficulty.EASY.getDurationSeconds());
        assertEquals(90, KanaQuizDifficulty.MEDIUM.getDurationSeconds());
        assertEquals(60, KanaQuizDifficulty.HARD.getDurationSeconds());
        assertTrue(KanaQuizDifficulty.EASY.isAnswerRevealEnabled());
        assertTrue(KanaQuizDifficulty.MEDIUM.isAnswerRevealEnabled());
        assertFalse(KanaQuizDifficulty.HARD.isAnswerRevealEnabled());
    }

    @Test
    @DisplayName("Đồng hồ cảnh báo ở 10 giây và chỉ báo hết giờ một lần")
    void countdownExpiresOnce() {
        var countdown = new KanaQuizCountdown(11);
        assertFalse(countdown.isWarningTime());
        countdown.tick();
        assertTrue(countdown.isWarningTime());
        for (int i = 0; i < 9; i++) assertFalse(countdown.tick().expiredNow());
        assertTrue(countdown.tick().expiredNow());
        assertFalse(countdown.tick().expiredNow());
        assertEquals("00:00", countdown.getFormattedTime());
        assertThrows(IllegalArgumentException.class, () -> new KanaQuizCountdown(-1));
    }

    private static FakeView startQuiz() {
        var view = new FakeView();
        new KanaController(view, new KanaService(
                List.of(new Kana("あ", "a", KanaType.gojuuon)), new Random(1), false));
        return view;
    }

    private static class FakeView implements KanaQuizFrame_Interface {
        String input = "";
        String answer;
        int score;
        Runnable submit, reveal;
        KanaQuizSessionStats.Snapshot stats;
        public void showKana(Kana kana) { }
        public void showResult(String result) { }
        public void resetResult() { }
        public void resetInput() { input = ""; }
        public void showCorrectAnswer(String value) { answer = value; }
        public void resetCorrectAnswer() { answer = null; }
        public void updateScore(int value) { score = value; }
        public void updateStats(KanaQuizSessionStats.Snapshot value) { stats = value; }
        public String getUserInput() { return input; }
        public void setOnSubmit(Runnable action) { submit = action; }
        public void setOnShowAnswer(Runnable action) { reveal = action; }
    }
}
