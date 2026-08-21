package com.mescode.japanese.model.vocab;

public enum VocabQuizDifficulty {
    EASY("Easy"),
    HARD("Hard");

    private final String displayName;

    VocabQuizDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAnswerRevealEnabled() {
        return this == EASY;
    }
}
