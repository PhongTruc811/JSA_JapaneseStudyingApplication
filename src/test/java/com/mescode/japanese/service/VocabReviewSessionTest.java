package com.mescode.japanese.service;

import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VocabReviewSessionTest {
    private Vocabulary word(String kana, int chapter) {
        return new Vocabulary(kana, "nghĩa", "romaji", null, chapter);
    }

    @Test void selectsOnlyReviewableWordsInTheChosenChapterWithoutChangingSource() {
        Vocabulary kanjiOnly = new Vocabulary(null, "猫", "mèo", null, null, 2);
        List<Vocabulary> words = new ArrayList<>(Arrays.asList(word("あ", 2), null,
                word("い", 1), word(" ", 2), new Vocabulary("う", null, null, null, 3), kanjiOnly));
        assertEquals(List.of(1, 2), VocabReviewSession.availableChapters(words));
        VocabReviewSession session = new VocabReviewSession(words, 2);
        assertEquals(2, session.size());
        session.shuffle();
        assertEquals("あ", words.getFirst().getKana());
        assertEquals(6, words.size());
    }

    @Test void requiresFlipAndRetriesOnlyMissedWordsThenRestartsEntireChapter() {
        Vocabulary first = word("あ", 1);
        Vocabulary second = word("い", 1);
        VocabReviewSession session = new VocabReviewSession(List.of(first, second), 1);
        session.rate(true);
        assertEquals(0, session.reviewedCount());
        session.flip();
        session.rate(false);
        assertSame(second, session.currentWord());
        assertFalse(session.isFlipped());
        session.flip();
        session.rate(true);
        assertTrue(session.isComplete());
        assertEquals(1, session.knownCount());
        session.rate(false);
        assertEquals(1, session.retryCount());
        session.retryMissed();
        assertEquals(1, session.size());
        assertSame(first, session.currentWord());
        assertEquals(0, session.reviewedCount());
        session.flip();
        session.rate(true);
        assertTrue(session.isComplete());
        session.retryMissed();
        assertTrue(session.isComplete());
        session.restart();
        assertEquals(2, session.size());
        assertEquals(0, session.reviewedCount());
    }

    @Test void browsingAndShufflingKeepRatingsAndHideAnswer() {
        VocabReviewSession session = new VocabReviewSession(List.of(word("あ", 1), word("い", 1), word("う", 1)), 1);
        session.move(-1);
        assertEquals(3, session.position());
        session.flip();
        session.move(1);
        assertEquals(1, session.position());
        assertFalse(session.isFlipped());
        session.flip();
        session.rate(true);
        Vocabulary current = session.currentWord();
        session.flip();
        session.shuffle();
        assertSame(current, session.currentWord());
        assertEquals(1, session.knownCount());
        assertFalse(session.isFlipped());
    }

    @Test void emptyOrUnavailableChapterIsSafe() {
        for (VocabReviewSession session : List.of(new VocabReviewSession(null, 1),
                new VocabReviewSession(List.of(word("あ", 1)), 2))) {
            session.flip();
            session.move(-1);
            session.rate(true);
            session.shuffle();
            session.retryMissed();
            assertNull(session.currentWord());
            assertEquals(0, session.size());
            assertEquals(0, session.position());
            assertFalse(session.isComplete());
        }
    }
}
