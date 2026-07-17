package com.mescode.japanese.controller;

import com.mescode.japanese.model.Kana;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.view.kana.KanaQuizFrame_Interface;
import lombok.NoArgsConstructor;

@NoArgsConstructor

public class KanaController {
    private KanaQuizFrame_Interface view;
    private KanaService service;
    private Kana currentKana;
    private int score;

    public KanaController(KanaQuizFrame_Interface view, KanaService service){
        this.view = view;

        this.service = service;

        init();
    }

    public void init(){
        // chữ đầu tiên khi chạy chương trình
        currentKana = service.getRandomKana(currentKana);
        view.showKana(currentKana);

        view.updateScore(score);

        view.setOnSubmit(this::handleSubmit);
        view.setOnShowAnswer(this:: handleShowAnswer);
    }

    public void handleSubmit(){
        String answer = view.getUserInput();

        if(answer.equals(currentKana.getRomaji())){
            score +=1;
            //currentHiragana = service.getRandomHira(currentHiragana);
            currentKana = service.getRandomKana(currentKana);
            view.updateScore(score);
            view.resetInput(); view.resetResult(); view.resetCorrectAnswer();
            view.showKana(currentKana);
        } else {
            score -=1;
            view.updateScore(score);
            view.showResult("Wrong Answer, Please try again");
        }
    }

    public void handleShowAnswer(){
        String answerText = "Kana: " + currentKana.getKana()
                + " | Romaji: " + currentKana.getRomaji()
                + " | Type: " + currentKana.getType();

        if (currentKana.getKana() != null && !currentKana.getKana().isBlank()) {
            answerText += " | Hira: " + currentKana.getKana();
        }

        view.showCorrectAnswer(answerText);
    }


}
