package com.mescode.japanese.controller;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaType;
import com.mescode.japanese.model.kana.KanaQuizSessionStats;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.view.kana.KanaQuizFrame_Interface;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KanaControllerTest {

    @Test
    void showAnswer_shouldKeepJapaneseKanaAndAvoidDuplicatedHiraField() {
        CapturingView view = new CapturingView();
        Kana kana = new Kana("ん", "n", KanaType.gojuuon);
        KanaService service = new KanaService(List.of(kana), new Random(0), false);
        KanaController controller = new KanaController(view, service);

        controller.handleShowAnswer();

        assertEquals("Kana: ん  •  Romaji: n  •  Type: gojuuon", view.correctAnswer);
        assertFalse(view.correctAnswer.contains("Hira:"));
        assertEquals(1, view.stats.answerReveals());

        controller.handleShowAnswer();
        assertEquals(1, view.stats.answerReveals());
    }

    @Test
    void answers_shouldUpdateCountsAndResetOnlyCurrentStreak() {
        CapturingView view = new CapturingView();
        Kana kana = new Kana("ん", "n", KanaType.gojuuon);
        KanaService service = new KanaService(List.of(kana), new Random(0), false);
        KanaController controller = new KanaController(view, service);

        view.userInput = "n";
        controller.handleSubmit();
        assertEquals(1, view.stats.correctAnswers());
        assertEquals(1, view.stats.currentStreak());

        view.userInput = "wrong";
        controller.handleSubmit();
        assertEquals(1, view.stats.wrongAnswers());
        assertEquals(0, view.stats.currentStreak());
        assertEquals(1, view.stats.bestStreak());
    }

    private static final class CapturingView implements KanaQuizFrame_Interface {
        private String correctAnswer;
        private String userInput = "";
        private KanaQuizSessionStats.Snapshot stats = KanaQuizSessionStats.Snapshot.empty();

        @Override public void showKana(Kana kana) { }
        @Override public void showResult(String result) { }
        @Override public void resetResult() { }
        @Override public void resetInput() { }
        @Override public void showCorrectAnswer(String answer) { correctAnswer = answer; }
        @Override public void resetCorrectAnswer() { }
        @Override public void updateScore(int score) { }
        @Override public void updateStats(KanaQuizSessionStats.Snapshot stats) { this.stats = stats; }
        @Override public String getUserInput() { return userInput; }
        @Override public void setOnSubmit(Runnable action) { }
        @Override public void setOnShowAnswer(Runnable action) { }
    }
}
