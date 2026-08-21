package com.mescode.japanese.model.kana;

public enum KanaQuizDifficulty {
    EASY("Easy", 3 * 60, true),
    MEDIUM("Medium", 90, true),
    HARD("Hard", 60, false);

    private final String displayName;
    private final int durationSeconds;
    private final boolean answerRevealEnabled;

    KanaQuizDifficulty(String displayName, int durationSeconds, boolean answerRevealEnabled) {
        this.displayName = displayName;
        this.durationSeconds = durationSeconds;
        this.answerRevealEnabled = answerRevealEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isAnswerRevealEnabled() {
        return answerRevealEnabled;
    }
}
