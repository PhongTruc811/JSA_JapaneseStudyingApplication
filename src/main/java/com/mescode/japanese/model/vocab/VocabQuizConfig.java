package com.mescode.japanese.model.vocab;

import java.util.Set;

public record VocabQuizConfig(boolean showKanji, Set<Integer> chapters, VocabQuizDifficulty difficulty) {
    public VocabQuizConfig {
        chapters = Set.copyOf(chapters);
        if (chapters.isEmpty()) {
            throw new IllegalArgumentException("At least one chapter is required");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("Quiz difficulty is required");
        }
    }

    public String getQuizTypeLabel() {
        return showKanji ? "Kanji" : "Vocabulary";
    }

    public String getChapterLabel() {
        return chapters.stream().sorted().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(", ", "Chapter ", ""));
    }
}
