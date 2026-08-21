package com.mescode.japanese.model.kana;

import java.util.Objects;

public record KanaQuizOptions(KanaQuizDifficulty difficulty, KanaQuizGroup group) {
    public KanaQuizOptions {
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(group, "group");
    }

    public static KanaQuizOptions easyGojuuon() {
        return new KanaQuizOptions(KanaQuizDifficulty.EASY, KanaQuizGroup.GOJUUON);
    }

    public static KanaQuizOptions easyAll() {
        return new KanaQuizOptions(KanaQuizDifficulty.EASY, KanaQuizGroup.ALL);
    }
}
