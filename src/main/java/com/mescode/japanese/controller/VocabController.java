package com.mescode.japanese.controller;

import com.mescode.japanese.model.Vocabulary;
import com.mescode.japanese.service.VocabService;
import com.mescode.japanese.view.vocabulary.VocabQuizFrame_Interface;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;

@NoArgsConstructor
public class VocabController {
    private VocabQuizFrame_Interface view;
    private VocabService service;
    private Vocabulary currentVocab;
    private int score;

    public VocabController(VocabQuizFrame_Interface view, VocabService service) {
        this.view = view;
        this.service = service;
        init();
    }

    private void init() {
        currentVocab = service.getRandomVocab(currentVocab);
        if (currentVocab != null) {
            view.showVocab(currentVocab);
        } else {
            view.showResult("No vocabulary found.");
            return;
        }
        view.setOnSubmit(this::handleSubmit);
        view.setOnShowAnswer(this::handleShowAnswer);
    }

    private void handleSubmit() {
        if (currentVocab == null) {
            view.showResult("No vocabulary found.");
            return;
        }
        String romaji = view.getRomajiInput();
        String meaning = view.getMeaningInput();

        boolean correctRomaji = normalizeRomajiAnswer(romaji)
                .equals(normalizeRomajiAnswer(currentVocab.getRomaji()));
        boolean correctMeaning = normalizeMeaningAnswer(meaning)
                .equals(normalizeMeaningAnswer(currentVocab.getMeaning()));

        if (correctRomaji && correctMeaning) {
            score += 1;
            currentVocab = service.getRandomVocab(currentVocab);
            view.updateScore(score);
            view.resetInput();
            view.resetResult();
            view.resetCorrectAnswer();
            if (currentVocab != null) {
                view.showVocab(currentVocab);
            }
        } else {
            score -= 1;
            view.updateScore(score);
            view.showResult("Wrong answer, please try again.");
        }
    }

    private void handleShowAnswer() {
        if (currentVocab == null) {
            view.showCorrectAnswer("No vocabulary found.");
            return;
        }
        String answer = currentVocab.getRomaji() + " - " + currentVocab.getMeaning();
        view.showCorrectAnswer(answer);
    }

    private String normalizeRomajiAnswer(String value) {
        return normalizeCommon(value).replace("-", "").replace(" ", "");
    }

    private String normalizeMeaningAnswer(String value) {
        String normalized = normalizeCommon(value);
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.replace('đ', 'd');
    }

    private String normalizeCommon(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }
}
