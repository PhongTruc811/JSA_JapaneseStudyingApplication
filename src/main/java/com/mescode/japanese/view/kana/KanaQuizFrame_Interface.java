package com.mescode.japanese.view.kana;

import com.mescode.japanese.model.Kana;

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

    // get data (input)
    String getUserInput();

    // make event
    void setOnSubmit(Runnable action); // sự kiện khi enter InputField
    void setOnShowAnswer(Runnable action);

}
