package com.mescode.japanese.service;

import com.mescode.japanese.model.vocab.Vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** In-memory flashcard round; review ratings do not change saved learning progress. */
public final class VocabReviewSession {
    private final List<Vocabulary> chapterWords;
    private final List<Card> cards = new ArrayList<>();
    private int index;
    private boolean flipped;

    public VocabReviewSession(List<Vocabulary> words, Integer chapter) {
        chapterWords = words == null || chapter == null ? List.of() : words.stream()
                .filter(Objects::nonNull)
                .filter(word -> chapter.equals(word.getChapter()))
                .filter(VocabReviewSession::isReviewable)
                .toList();
        restart();
    }

    public static List<Integer> availableChapters(List<Vocabulary> words) {
        if (words == null) return List.of();
        return words.stream().filter(Objects::nonNull)
                .filter(VocabReviewSession::isReviewable)
                .map(Vocabulary::getChapter).filter(Objects::nonNull)
                .distinct().sorted().toList();
    }

    private static boolean isReviewable(Vocabulary word) {
        return hasText(word.getMeaning()) && (hasText(word.getKana()) || hasText(word.getKanji()));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Vocabulary currentWord() { return cards.isEmpty() ? null : cards.get(index).word; }
    public int size() { return cards.size(); }
    public int position() { return cards.isEmpty() ? 0 : index + 1; }
    public boolean isFlipped() { return flipped; }
    public void flip() { if (!cards.isEmpty() && !isComplete()) flipped = !flipped; }
    public int reviewedCount() { return (int) cards.stream().filter(card -> card.known != null).count(); }
    public int knownCount() { return (int) cards.stream().filter(card -> Boolean.TRUE.equals(card.known)).count(); }
    public int retryCount() { return reviewedCount() - knownCount(); }
    public boolean isComplete() { return !cards.isEmpty() && reviewedCount() == size(); }

    public void move(int offset) {
        if (cards.isEmpty() || isComplete()) return;
        index = Math.floorMod(index + offset, cards.size());
        flipped = false;
    }

    /** Require revealing the answer before self-assessment. */
    public void rate(boolean known) {
        if (cards.isEmpty() || !flipped || isComplete()) return;
        cards.get(index).known = known;
        if (!isComplete()) {
            do { index = (index + 1) % cards.size(); } while (cards.get(index).known != null);
        }
        flipped = false;
    }

    public void shuffle() {
        if (cards.isEmpty() || isComplete()) return;
        Card current = cards.get(index);
        Collections.shuffle(cards);
        index = cards.indexOf(current);
        flipped = false;
    }

    public void retryMissed() {
        if (!isComplete() || retryCount() == 0) return;
        List<Vocabulary> missed = cards.stream().filter(card -> Boolean.FALSE.equals(card.known))
                .map(card -> card.word).toList();
        startRound(missed);
    }

    public void restart() { startRound(chapterWords); }

    private void startRound(List<Vocabulary> words) {
        cards.clear();
        words.forEach(word -> cards.add(new Card(word)));
        index = 0;
        flipped = false;
    }

    private static final class Card {
        private final Vocabulary word;
        private Boolean known;
        private Card(Vocabulary word) { this.word = word; }
    }
}
