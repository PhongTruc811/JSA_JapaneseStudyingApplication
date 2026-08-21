package com.mescode.japanese.model.petrial;

public enum PeTrialConfig {
    EASY("Easy", 0, true),
    HARD("Hard", 40 * 60, false);

    private final String displayName;
    private final int durationSeconds;
    private final boolean answerCheckEnabled;

    PeTrialConfig(String displayName, int durationSeconds, boolean answerCheckEnabled) {
        this.displayName = displayName;
        this.durationSeconds = durationSeconds;
        this.answerCheckEnabled = answerCheckEnabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isAnswerCheckEnabled() {
        return answerCheckEnabled;
    }

    public boolean isTimed() {
        return durationSeconds > 0;
    }
}
