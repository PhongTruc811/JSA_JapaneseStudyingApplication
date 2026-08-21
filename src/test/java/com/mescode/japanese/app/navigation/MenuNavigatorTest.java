package com.mescode.japanese.app.navigation;

import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.repo.VocabRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuNavigatorTest {

    @Test
    void filterChapterVocabs_shouldKeepOnlyChaptersOneToThree() {
        List<Vocabulary> allVocabs = new VocabRepository()
                .readVocabFromJson("/data/vocab.json");

        List<Vocabulary> filtered = AppNavigator.filterChapterVocabs(allVocabs);

        assertEquals(193, filtered.size());
        assertEquals(40, countChapter(filtered, 1));
        assertEquals(74, countChapter(filtered, 2));
        assertEquals(79, countChapter(filtered, 3));
        assertTrue(filtered.stream()
                .allMatch(vocab -> vocab.getLesson() >= 1 && vocab.getLesson() <= 3));
        assertFalse(filtered.stream()
                .anyMatch(vocab -> "kirai".equalsIgnoreCase(vocab.getRomaji())));
    }

    @Test
    void filterChapterVocabs_shouldHandleMissingAndInvalidLessons() {
        Vocabulary chapterOne = vocab("chapter-one", 1);
        Vocabulary noChapter = vocab("no-chapter", null);
        Vocabulary chapterFour = vocab("chapter-four", 4);

        List<Vocabulary> filtered = AppNavigator.filterChapterVocabs(
                List.of(chapterOne, noChapter, chapterFour));

        assertEquals(List.of(chapterOne), filtered);
        assertTrue(AppNavigator.filterChapterVocabs(null).isEmpty());
    }

    private long countChapter(List<Vocabulary> vocabularies, int chapter) {
        return vocabularies.stream()
                .filter(vocab -> Integer.valueOf(chapter).equals(vocab.getLesson()))
                .count();
    }

    private Vocabulary vocab(String romaji, Integer lesson) {
        return new Vocabulary("かな", "Nghĩa", romaji, "", lesson);
    }
}
