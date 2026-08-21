package com.mescode.japanese.model.vocab;

public record VocabQuizAnswerResult(VocabQuizQuestion question, String selectedAnswer) {
    public boolean isCorrect() {
        return question.isCorrect(selectedAnswer);
    }
}
