package com.mescode.japanese.repository;

import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocabRepositoryTest {

    private final VocabRepository repository = new VocabRepository();
    //   - Test 1: kiểm tra repository đọc được danh sách từ vựng.

    @Test
    void getVocabs_shouldLoadVocabularyData() {
        List<Vocabulary> vocabs = repository.getVocabs();

        assertNotNull(vocabs);
        assertFalse(vocabs.isEmpty());
        assertTrue(vocabs.stream().allMatch(this::isValidVocabulary));
    }
    //  - Test 2: kiểm tra đọc dữ liệu từ file JSON và dữ liệu hợp lệ.
    @Test
    void readVocabFromJson_shouldLoadVocabularyData() {
        List<Vocabulary> vocabs = repository.readVocabFromJson("/data/vocab.json");
        assertNotNull(vocabs);
        assertFalse(vocabs.isEmpty());
        assertTrue(vocabs.stream().allMatch(this::isValidVocabulary));
        assertTrue(vocabs.stream().anyMatch(vocabulary -> vocabulary.getChapter() != null && vocabulary.getChapter() > 0));
    }
    //  - Test 3: kiểm tra khi file không tồn tại thì chương trình ném ra RuntimeException như mong đợi.
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
