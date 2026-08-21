package com.mescode.japanese.model.kana;

public final class KanaQuizSessionStats {
    private int correctAnswers;
    private int wrongAnswers;
    private int answerReveals;
    private int currentStreak;
    private int bestStreak;

    public void recordCorrect() {
        correctAnswers++;
        currentStreak++;
        bestStreak = Math.max(bestStreak, currentStreak);
    }

    public void recordWrong() {
        wrongAnswers++;
        currentStreak = 0;
    }

    public void recordAnswerReveal() {
        answerReveals++;
    }

    public Snapshot snapshot() {
        return new Snapshot(correctAnswers, wrongAnswers, answerReveals, currentStreak, bestStreak);
    }

    public record Snapshot(
            int correctAnswers,
            int wrongAnswers,
            int answerReveals,
            int currentStreak,
            int bestStreak
    ) {
        public static Snapshot empty() {
            return new Snapshot(0, 0, 0, 0, 0);
        }
    }
}
