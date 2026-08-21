package com.mescode.japanese.controller;

import com.mescode.japanese.model.petrial.PeTrialAnswerResult;
import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.model.petrial.PeTrialQuestion;
import com.mescode.japanese.service.PeTrialService;
import com.mescode.japanese.view.petrial.PeTrialQuizView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeTrialControllerTest {
    private final List<PeTrialQuestion> questions = List.of(
            new PeTrialQuestion("pe_trial_01", "/q1.jpg", List.of("A", "B", "C", "D"), "B"),
            new PeTrialQuestion("pe_trial_02", "/q2.jpg", List.of("A", "B", "C", "D"), "C")
    );

    @Test
    void easyMode_shouldRestoreSelectionAndCheckAnswer() {
        FakeView view = new FakeView();
        new PeTrialController(view, new PeTrialService(questions), PeTrialConfig.EASY, (results, timed) -> { });

        view.answerAction.accept("B");
        view.questionAction.accept(1);
        assertNull(view.selectedAnswer);
        view.questionAction.accept(0);
        assertEquals("B", view.selectedAnswer);

        assertNotNull(view.checkAction);
        view.checkAction.run();
        assertEquals("Correct answer.", view.feedback);
        assertTrue(view.feedbackCorrect);
    }

    @Test
    void manualSubmit_shouldIncludeUnansweredQuestionsAfterConfirmation() {
        FakeView view = new FakeView();
        FinishCapture finish = new FinishCapture();
        new PeTrialController(view, new PeTrialService(questions), PeTrialConfig.EASY, finish);

        view.answerAction.accept("B");
        view.submitAction.run();

        assertEquals(1, view.confirmedUnanswered);
        assertEquals(2, finish.results.size());
        assertTrue(finish.results.get(0).isCorrect());
        assertFalse(finish.results.get(1).isAnswered());
        assertFalse(finish.timeExpired);
    }

    @Test
    void hardMode_shouldAutoSubmitOnceWithoutCheckAction() {
        FakeView view = new FakeView();
        FinishCapture finish = new FinishCapture();
        new PeTrialController(view, new PeTrialService(questions), PeTrialConfig.HARD, finish);

        assertNull(view.checkAction);
        view.timeExpiredAction.run();
        view.timeExpiredAction.run();

        assertEquals(1, finish.invocations);
        assertTrue(finish.timeExpired);
        assertEquals(2, finish.results.size());
    }

    private static final class FinishCapture
            implements BiConsumer<List<PeTrialAnswerResult>, Boolean> {
        private List<PeTrialAnswerResult> results = List.of();
        private boolean timeExpired;
        private int invocations;

        @Override
        public void accept(List<PeTrialAnswerResult> results, Boolean timeExpired) {
            this.results = results;
            this.timeExpired = timeExpired;
            invocations++;
        }
    }

    private static final class FakeView implements PeTrialQuizView {
        private Consumer<String> answerAction;
        private IntConsumer questionAction;
        private Runnable previousAction;
        private Runnable nextAction;
        private Runnable checkAction;
        private Runnable submitAction;
        private Runnable timeExpiredAction;
        private String selectedAnswer;
        private String feedback;
        private boolean feedbackCorrect;
        private int confirmedUnanswered = -1;

        @Override public void showQuiz(PeTrialConfig difficulty, List<PeTrialQuestion> questions) { }

        @Override
        public void showQuestion(int index, PeTrialQuestion question, String selectedAnswer) {
            this.selectedAnswer = selectedAnswer;
        }

        @Override public void updateQuestionNavigation(int currentIndex, List<Boolean> answered) { }

        @Override
        public void showFeedback(String message, boolean correct) {
            feedback = message;
            feedbackCorrect = correct;
        }

        @Override public void clearFeedback() { feedback = null; }
        @Override public void showUnavailable(String message) { }

        @Override
        public boolean confirmSubmit(int unansweredQuestions) {
            confirmedUnanswered = unansweredQuestions;
            return true;
        }

        @Override public void setOnAnswerSelected(Consumer<String> action) { answerAction = action; }
        @Override public void setOnQuestionSelected(IntConsumer action) { questionAction = action; }
        @Override public void setOnPrevious(Runnable action) { previousAction = action; }
        @Override public void setOnNext(Runnable action) { nextAction = action; }
        @Override public void setOnCheckAnswer(Runnable action) { checkAction = action; }
        @Override public void setOnSubmit(Runnable action) { submitAction = action; }
        @Override public void setOnTimeExpired(Runnable action) { timeExpiredAction = action; }
    }
}
