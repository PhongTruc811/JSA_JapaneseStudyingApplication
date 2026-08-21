package com.mescode.japanese.view.petrial;

import com.mescode.japanese.model.petrial.PeTrialConfig;
import com.mescode.japanese.model.petrial.PeTrialQuestion;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface PeTrialQuizView {
    void showQuiz(PeTrialConfig difficulty, List<PeTrialQuestion> questions);
    void showQuestion(int index, PeTrialQuestion question, String selectedAnswer);
    void updateQuestionNavigation(int currentIndex, List<Boolean> answered);
    void showFeedback(String message, boolean correct);
    void clearFeedback();
    void showUnavailable(String message);
    boolean confirmSubmit(int unansweredQuestions);
    void setOnAnswerSelected(Consumer<String> action);
    void setOnQuestionSelected(IntConsumer action);
    void setOnPrevious(Runnable action);
    void setOnNext(Runnable action);
    void setOnCheckAnswer(Runnable action);
    void setOnSubmit(Runnable action);
    void setOnTimeExpired(Runnable action);
}
