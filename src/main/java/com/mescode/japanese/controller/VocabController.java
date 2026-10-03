package com.mescode.japanese.controller;

import com.mescode.japanese.model.vocab.VocabQuizAnswerResult;
import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizQuestion;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.view.vocabulary.quiz.VocabQuizFrame_Interface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VocabController {
    private final VocabQuizFrame_Interface view;
    private final VocabService service;
    private final VocabQuizConfig config;
    private final Consumer<List<VocabQuizAnswerResult>> finishHandler;
    private final Map<Integer, String> answersByQuestion = new HashMap<>();
    private List<VocabQuizQuestion> questions = List.of();
    private int currentIndex;

    public VocabController(VocabQuizFrame_Interface view, VocabService service, VocabQuizConfig config,
                           Consumer<List<VocabQuizAnswerResult>> finishHandler) {
        this.view = view;
        this.service = service;
        this.config = config;
        this.finishHandler = finishHandler;
        init();
    }

    private void init() {
        questions = service.getQuizQuestions();
        if (questions.isEmpty()) {
            view.showUnavailable("This selection does not contain enough different answers to build a quiz.");
            return;
        }
        view.showQuiz(config, questions);
        view.setOnAnswerSelected(this::saveAnswer);
        view.setOnQuestionSelected(this::showQuestion);
        view.setOnNext(this::showNextQuestion);
        view.setOnFinish(this::finishQuiz);
        if (config.difficulty().isAnswerRevealEnabled()) {
            view.setOnCheckAnswer(this::checkAnswer);
        }
        showQuestion(0);
    }

    private void saveAnswer(String answer) {
        answersByQuestion.put(currentIndex, answer);
        view.clearFeedback();
        renderCurrentQuestion();
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }
        currentIndex = index;
        view.clearFeedback();
        renderCurrentQuestion();
    }

    private void showNextQuestion() {
        if (currentIndex < questions.size() - 1) {
            showQuestion(currentIndex + 1);
        }
    }

    private void renderCurrentQuestion() {
        view.showQuestion(currentIndex, questions.get(currentIndex), answersByQuestion.get(currentIndex));
        List<Boolean> answered = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            answered.add(answersByQuestion.containsKey(index));
        }
        view.updateQuestionNavigation(currentIndex, answered);
    }

    private void checkAnswer() {
        String selected = answersByQuestion.get(currentIndex);
        if (selected == null) {
            view.showFeedback("Choose an answer before checking.", false);
            return;
        }
        boolean correct = questions.get(currentIndex).isCorrect(selected);
        view.showFeedback(correct ? "Correct answer." : "Not quite. Try another answer or review it.", correct);
    }

    private void finishQuiz() {
        if (answersByQuestion.size() != questions.size()) {
            view.showIncompleteWarning(questions.size() - answersByQuestion.size());
            return;
        }
        List<VocabQuizAnswerResult> results = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            results.add(new VocabQuizAnswerResult(questions.get(index), answersByQuestion.get(index)));
        }
        finishHandler.accept(List.copyOf(results));
    }
}
