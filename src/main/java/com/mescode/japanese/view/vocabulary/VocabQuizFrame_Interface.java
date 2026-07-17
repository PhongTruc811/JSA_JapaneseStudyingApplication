package com.mescode.japanese.view.vocabulary;

import com.mescode.japanese.model.Vocabulary;

public interface VocabQuizFrame_Interface {
    void showVocab(Vocabulary vocab);
    String getRomajiInput();
    String getMeaningInput();
    void resetInput();
    void showResult(String result);
    void resetResult();
    void showCorrectAnswer(String answer);
    void resetCorrectAnswer();
    void updateScore(int score);
    void setOnSubmit(Runnable action);
    void setOnShowAnswer(Runnable action);
}
