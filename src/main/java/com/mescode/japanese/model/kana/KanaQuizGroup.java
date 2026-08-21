package com.mescode.japanese.model.kana;

public enum KanaQuizGroup {
    ALL("All Kana"),
    GOJUUON("Gojuuon"),
    DAKUON("Dakuon"),
    YOUON("Youon");

    private final String displayName;

    KanaQuizGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean matches(KanaType type) {
        if (type == null) {
            return false;
        }
        return switch (this) {
            case ALL -> true;
            case GOJUUON -> type == KanaType.gojuuon;
            case DAKUON -> type == KanaType.dakuon || type == KanaType.handakuon;
            case YOUON -> type == KanaType.youon;
        };
    }
}
