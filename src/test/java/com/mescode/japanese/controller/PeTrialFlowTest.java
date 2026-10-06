package com.mescode.japanese.controller;

import com.mescode.japanese.model.petrial.*;
import com.mescode.japanese.service.PeTrialService;
import com.mescode.japanese.view.petrial.PeTrialQuizView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Đề thi thử: chọn đáp án, nộp bài và hết giờ")
class PeTrialFlowTest {
    @Test
    @DisplayName("Chuyển câu giữ đáp án và chế độ dễ cho kiểm tra đáp án")
    void restoresSelectionAndChecksAnswer() {
        var view = start(PeTrialConfig.EASY, (results, timed) -> { });
        view.answer.accept("B");
        view.select.accept(1);
        assertNull(view.selected);
        view.select.accept(0);
        assertEquals("B", view.selected);
        view.check.run();
        assertTrue(view.correct);
        view.answer.accept("invalid");
        assertEquals("B", view.selected);
    }

    @Test
    @DisplayName("Hủy xác nhận không nộp; xác nhận giữ cả câu bỏ trống")
    void manualSubmissionRequiresConfirmation() {
        var finished = new ArrayList<List<PeTrialAnswerResult>>();
        var view = start(PeTrialConfig.EASY, (results, timed) -> {
            assertFalse(timed);
            finished.add(results);
        });
        view.answer.accept("B");
        view.confirm = false;
        view.submit.run();
        assertTrue(finished.isEmpty());
        assertEquals(1, view.unanswered);
        view.confirm = true;
        view.submit.run();
        view.submit.run();
        assertEquals(1, finished.size());
        assertEquals(2, finished.getFirst().size());
        assertTrue(finished.getFirst().get(0).isCorrect());
        assertFalse(finished.getFirst().get(1).isAnswered());
    }

    @Test
    @DisplayName("Chế độ khó ẩn kiểm tra đáp án và hết giờ chỉ nộp một lần")
    void expirationSubmitsOnce() {
        var finished = new ArrayList<List<PeTrialAnswerResult>>();
        var view = start(PeTrialConfig.HARD, (results, timed) -> {
            assertTrue(timed);
            finished.add(results);
        });
        assertNull(view.check);
        view.expire.run();
        view.expire.run();
        view.submit.run();
        assertEquals(1, finished.size());
        assertEquals(2, finished.getFirst().size());
        assertTrue(finished.getFirst().stream().noneMatch(PeTrialAnswerResult::isAnswered));
    }

    @Test
    @DisplayName("Đồng hồ đề thi cảnh báo ở năm phút và không chạy xuống số âm")
    void countdownBoundaries() {
        var warning = new PeTrialCountdown(301);
        assertFalse(warning.isWarningTime());
        warning.tick();
        assertTrue(warning.isWarningTime());
        assertEquals("05:00", warning.getFormattedTime());
        var countdown = new PeTrialCountdown(1);
        assertTrue(countdown.tick().expiredNow());
        assertFalse(countdown.tick().expiredNow());
        assertEquals(0, countdown.getRemainingSeconds());
        assertThrows(IllegalArgumentException.class, () -> new PeTrialCountdown(-1));
    }

    private static FakeView start(PeTrialConfig difficulty,
                                  BiConsumer<List<PeTrialAnswerResult>, Boolean> finished) {
        var questions = List.of(
                new PeTrialQuestion("q1", "/q1.jpg", List.of("A", "B", "C", "D"), "B"),
                new PeTrialQuestion("q2", "/q2.jpg", List.of("A", "B", "C", "D"), "C"));
        var view = new FakeView();
        new PeTrialController(view, new PeTrialService(questions), difficulty, finished);
        return view;
    }

    private static class FakeView implements PeTrialQuizView {
        Consumer<String> answer;
        IntConsumer select;
        Runnable check, submit, expire;
        String selected;
        boolean correct, confirm;
        int unanswered;
        public void showQuiz(PeTrialConfig config, List<PeTrialQuestion> questions) { }
        public void showQuestion(int index, PeTrialQuestion question, String value) { selected = value; }
        public void updateQuestionNavigation(int index, List<Boolean> answered) { }
        public void showFeedback(String message, boolean value) { correct = value; }
        public void clearFeedback() { }
        public void showUnavailable(String message) { fail(message); }
        public boolean confirmSubmit(int value) { unanswered = value; return confirm; }
        public void setOnAnswerSelected(Consumer<String> action) { answer = action; }
        public void setOnQuestionSelected(IntConsumer action) { select = action; }
        public void setOnPrevious(Runnable action) { }
        public void setOnNext(Runnable action) { }
        public void setOnCheckAnswer(Runnable action) { check = action; }
        public void setOnSubmit(Runnable action) { submit = action; }
        public void setOnTimeExpired(Runnable action) { expire = action; }
    }
}
