package com.mescode.japanese.controller;

import com.mescode.japanese.model.petrial.PeTrialAnswerResult;
import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.model.petrial.PeTrialQuestion;
import com.mescode.japanese.service.PeTrialService;
import com.mescode.japanese.view.petrial.PeTrialQuizView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PeTrialController {
    private final PeTrialQuizView view;
    private final PeTrialConfig difficulty;
    private final BiConsumer<List<PeTrialAnswerResult>, Boolean> finishHandler;
    private final Map<Integer, String> answersByQuestion = new HashMap<>();
    private List<PeTrialQuestion> questions = List.of();
    private int currentIndex;
    private boolean finished;

    public PeTrialController(PeTrialQuizView view, PeTrialService service, PeTrialConfig difficulty,
                             BiConsumer<List<PeTrialAnswerResult>, Boolean> finishHandler) {
        this.view = view;
        this.difficulty = difficulty;
        this.finishHandler = finishHandler;
        initialize(service);
    }

    private void initialize(PeTrialService service) {
        questions = service.getQuestions();
        if (questions.isEmpty()) {
            view.showUnavailable("PE Trial questions are unavailable.");
            return;
        }

        view.setOnAnswerSelected(this::saveAnswer);
        view.setOnQuestionSelected(this::showQuestion);
        view.setOnPrevious(this::showPreviousQuestion);
        view.setOnNext(this::showNextQuestion);
        view.setOnSubmit(this::submitManually);
        view.setOnTimeExpired(this::submitWhenTimeExpires);
        if (difficulty.isAnswerCheckEnabled()) {
            view.setOnCheckAnswer(this::checkAnswer);
        }
        view.showQuiz(difficulty, questions);
        showQuestion(0);
    }

    private void saveAnswer(String answer) {
        if (finished || !List.of("A", "B", "C", "D").contains(answer)) {
            return;
        }
        answersByQuestion.put(currentIndex, answer);
        view.clearFeedback();
        renderCurrentQuestion();
    }

    private void showQuestion(int index) {
        if (finished || index < 0 || index >= questions.size()) {
            return;
        }
        currentIndex = index;
        view.clearFeedback();
        renderCurrentQuestion();
    }

    private void showPreviousQuestion() {
        showQuestion(currentIndex - 1);
    }

    private void showNextQuestion() {
        showQuestion(currentIndex + 1);
    }

    private void renderCurrentQuestion() {
        view.showQuestion(currentIndex, questions.get(currentIndex), answersByQuestion.get(currentIndex));
        List<Boolean> answered = new ArrayList<>(questions.size());
        for (int index = 0; index < questions.size(); index++) {
            answered.add(answersByQuestion.containsKey(index));
        }
        view.updateQuestionNavigation(currentIndex, answered);
    }

    private void checkAnswer() {
        if (finished || !difficulty.isAnswerCheckEnabled()) {
            return;
        }
        String selected = answersByQuestion.get(currentIndex);
        if (selected == null) {
            view.showFeedback("Choose A, B, C or D before checking.", false);
            return;
        }
        boolean correct = questions.get(currentIndex).isCorrect(selected);
        view.showFeedback(correct ? "Correct answer." : "Not quite. You can change your answer.", correct);
    }

    private void submitManually() {
        if (finished) {
            return;
        }
        int unanswered = questions.size() - answersByQuestion.size();
        if (view.confirmSubmit(unanswered)) {
            finish(false);
        }
    }

    private void submitWhenTimeExpires() {
        finish(true);
    }

    private void finish(boolean timeExpired) {
        if (finished) {
            return;
        }
        finished = true;
        List<PeTrialAnswerResult> results = new ArrayList<>(questions.size());
        for (int index = 0; index < questions.size(); index++) {
            results.add(new PeTrialAnswerResult(questions.get(index), answersByQuestion.get(index)));
        }
        finishHandler.accept(List.copyOf(results), timeExpired);
    }
}
