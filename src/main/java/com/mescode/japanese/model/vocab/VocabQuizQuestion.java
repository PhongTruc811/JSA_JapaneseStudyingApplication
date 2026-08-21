package com.mescode.japanese.model.vocab;

import java.util.List;

public record VocabQuizQuestion(String meaning, List<String> options, String correctAnswer) {
    public VocabQuizQuestion {
        options = List.copyOf(options);
    }

    public boolean isCorrect(String answer) {
        return correctAnswer.equals(answer);
    }
}
