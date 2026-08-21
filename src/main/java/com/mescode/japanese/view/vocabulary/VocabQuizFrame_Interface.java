package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizQuestion;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface VocabQuizFrame_Interface {
    void showQuiz(VocabQuizConfig config, List<VocabQuizQuestion> questions);
    void showQuestion(int index, VocabQuizQuestion question, String selectedAnswer);
    void updateQuestionNavigation(int currentIndex, List<Boolean> answered);
    void showFeedback(String message, boolean correct);
    void showIncompleteWarning(int remainingQuestions);
    void clearFeedback();
    void showUnavailable(String message);
    void setOnAnswerSelected(Consumer<String> action);
    void setOnQuestionSelected(IntConsumer action);
    void setOnNext(Runnable action);
    void setOnCheckAnswer(Runnable action);
    void setOnFinish(Runnable action);
}
