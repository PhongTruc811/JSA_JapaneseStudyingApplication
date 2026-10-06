package com.mescode.japanese.controller;

import com.mescode.japanese.model.vocab.*;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.view.vocabulary.quiz.VocabQuizFrame_Interface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Làm và nộp bài từ vựng")
class VocabControllerTest {
    @Test
    @DisplayName("Đổi câu giữ lựa chọn và chưa cho nộp khi còn câu bỏ trống")
    void restoresAnswersAndBlocksIncompleteSubmission() {
        var finished = new ArrayList<List<VocabQuizAnswerResult>>();
        var view = start(VocabQuizDifficulty.EASY, finished::add);
        String correct = view.questions.getFirst().correctAnswer();
        view.answer.accept(correct);
        view.select.accept(1);
        assertNull(view.selected);
        view.select.accept(0);
        assertEquals(correct, view.selected);
        view.check.run();
        assertTrue(view.correct);
        view.finish.run();
        assertEquals(3, view.remaining);
        assertTrue(finished.isEmpty());
    }

    @Test
    @DisplayName("Nộp đủ câu trả về kết quả đúng của từng câu")
    void submitsCompletedQuiz() {
        var finished = new ArrayList<List<VocabQuizAnswerResult>>();
        var view = start(VocabQuizDifficulty.HARD, finished::add);
        assertNull(view.check);
        for (int i = 0; i < view.questions.size(); i++) {
            view.select.accept(i);
            view.answer.accept(view.questions.get(i).correctAnswer());
        }
        view.finish.run();
        assertEquals(1, finished.size());
        assertEquals(4, finished.getFirst().size());
        assertTrue(finished.getFirst().stream().allMatch(VocabQuizAnswerResult::isCorrect));
    }

    @Test
    @DisplayName("Đề rỗng báo không khả dụng và không cho làm bài")
    void emptyQuizIsUnavailable() {
        var config = new VocabQuizConfig(false, Set.of(1), VocabQuizDifficulty.EASY);
        var view = new FakeView();
        new VocabController(view, new VocabService(List.of(), config), config,
                results -> fail("Không được nộp đề rỗng"));
        assertTrue(view.unavailable);
        assertNull(view.finish);
    }

    private static FakeView start(VocabQuizDifficulty difficulty, Consumer<List<VocabQuizAnswerResult>> finish) {
        var config = new VocabQuizConfig(false, Set.of(1), difficulty);
        var entries = List.of(
                new Vocabulary("あ", "", "A", "a", "", 1),
                new Vocabulary("い", "", "I", "i", "", 1),
                new Vocabulary("う", "", "U", "u", "", 1),
                new Vocabulary("え", "", "E", "e", "", 1));
        var view = new FakeView();
        new VocabController(view, new VocabService(entries, config), config, finish);
        return view;
    }

    private static class FakeView implements VocabQuizFrame_Interface {
        List<VocabQuizQuestion> questions;
        String selected;
        boolean correct, unavailable;
        int remaining;
        Consumer<String> answer;
        IntConsumer select;
        Runnable check, finish;
        public void showQuiz(VocabQuizConfig config, List<VocabQuizQuestion> value) { questions = value; }
        public void showQuestion(int index, VocabQuizQuestion question, String value) { selected = value; }
        public void updateQuestionNavigation(int index, List<Boolean> answered) { }
        public void showFeedback(String message, boolean value) { correct = value; }
        public void showIncompleteWarning(int value) { remaining = value; }
        public void clearFeedback() { }
        public void showUnavailable(String message) { unavailable = true; }
        public void setOnAnswerSelected(Consumer<String> action) { answer = action; }
        public void setOnQuestionSelected(IntConsumer action) { select = action; }
        public void setOnNext(Runnable action) { }
        public void setOnCheckAnswer(Runnable action) { check = action; }
        public void setOnFinish(Runnable action) { finish = action; }
    }
}
