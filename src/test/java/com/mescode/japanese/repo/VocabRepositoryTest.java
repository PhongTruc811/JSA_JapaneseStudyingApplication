package com.mescode.japanese.repo;

import com.mescode.japanese.model.Vocabulary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocabRepositoryTest {

    private final VocabRepository repository = new VocabRepository();

    @Test
    void getVocabs_shouldLoadVocabularyData() {
        List<Vocabulary> vocabs = repository.getVocabs();

        assertNotNull(vocabs);
        assertFalse(vocabs.isEmpty());
        assertTrue(vocabs.stream().allMatch(this::isValidVocabulary));
    }

    @Test
    void readVocabFromJson_shouldLoadVocabularyData() {
        List<Vocabulary> vocabs = repository.readVocabFromJson("/data/vocab.json");

        assertNotNull(vocabs);
        assertFalse(vocabs.isEmpty());
        assertTrue(vocabs.stream().allMatch(this::isValidVocabulary));
        assertTrue(vocabs.stream().anyMatch(vocabulary -> vocabulary.getLesson() != null && vocabulary.getLesson() > 0));
    }

    @Test
    void readVocabFromJson_shouldThrowWhenFileMissing() {
        assertThrows(RuntimeException.class, () -> repository.readVocabFromJson("/data/does-not-exist.json"));
    }

    private boolean isValidVocabulary(Vocabulary vocabulary) {
        return vocabulary != null
                && vocabulary.getKana() != null && !vocabulary.getKana().isBlank()
                && vocabulary.getMeaning() != null && !vocabulary.getMeaning().isBlank()
                && vocabulary.getRomaji() != null && !vocabulary.getRomaji().isBlank();
    }
}
