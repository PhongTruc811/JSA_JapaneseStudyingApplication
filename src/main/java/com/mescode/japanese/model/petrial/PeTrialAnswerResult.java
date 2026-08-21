package com.mescode.japanese.model.petrial;

public record PeTrialAnswerResult(PeTrialQuestion question, String selectedAnswer) {
    public boolean isAnswered() {
        return selectedAnswer != null;
    }

    public boolean isCorrect() {
        return isAnswered() && question.isCorrect(selectedAnswer);
    }
}
