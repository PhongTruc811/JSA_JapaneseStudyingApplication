package com.mescode.japanese.controller;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaQuizSessionStats;
import com.mescode.japanese.service.KanaService;
import com.mescode.japanese.view.kana.KanaQuizFrame_Interface;
import lombok.NoArgsConstructor;

@NoArgsConstructor

public class KanaController {
    private KanaQuizFrame_Interface view;
    private KanaService service;
    private Kana currentKana;
    private int score;
    private KanaQuizSessionStats stats = new KanaQuizSessionStats();
    private boolean answerRevealedForCurrentKana;

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
        view.updateStats(stats.snapshot());

        view.setOnSubmit(this::handleSubmit);
        view.setOnShowAnswer(this:: handleShowAnswer);
    }

    public void handleSubmit(){
        String answer = view.getUserInput();

        if(answer.equals(currentKana.getRomaji())){
            score +=1;
            stats.recordCorrect();
            //currentHiragana = service.getRandomHira(currentHiragana);
            currentKana = service.getRandomKana(currentKana);
            answerRevealedForCurrentKana = false;
            view.updateScore(score);
            view.updateStats(stats.snapshot());
            view.resetInput(); view.resetResult(); view.resetCorrectAnswer();
            view.showKana(currentKana);
        } else {
            score -=1;
            stats.recordWrong();
            view.updateScore(score);
            view.updateStats(stats.snapshot());
            view.showResult("Wrong Answer, Please try again");
        }
    }

    public void handleShowAnswer(){
        String answerText = "Kana: " + currentKana.getKana()
                + "  •  Romaji: " + currentKana.getRomaji()
                + "  •  Type: " + currentKana.getType();

        if (!answerRevealedForCurrentKana) {
            stats.recordAnswerReveal();
            answerRevealedForCurrentKana = true;
            view.updateStats(stats.snapshot());
        }
        view.showCorrectAnswer(answerText);
    }


}
