package com.mescode.japanese.view.kana;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaQuizSessionStats;

// interface là hợp giao kết nối giữa HiraganaController và HiraganaQuizFrame(view)
public interface KanaQuizFrame_Interface {
    // show data (output)
    void showKana(Kana kana);
    void showResult(String result);
    void resetResult();
    void resetInput();
    void showCorrectAnswer(String correctHiragana);
    void resetCorrectAnswer();
    void updateScore(int score);
    void updateStats(KanaQuizSessionStats.Snapshot stats);

    // get data (input)
    String getUserInput();

    // make event
    void setOnSubmit(Runnable action); // sự kiện khi enter InputField
    void setOnShowAnswer(Runnable action);

}
