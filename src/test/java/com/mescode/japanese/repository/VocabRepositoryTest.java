package com.mescode.japanese.repository;

import com.google.gson.JsonParser;
import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lưu dữ liệu từ vựng cá nhân")
class VocabRepositoryTest {
    @TempDir Path tempDir;

    @Test
    @DisplayName("Yêu thích và đã học được lưu độc lập, tải lại và xóa được")
    void persistsIndependentPreferences() {
        Path directory = tempDir.resolve("nested/data");
        var repository = new VocabRepository(directory);
        assertTrue(repository.readFavoriteVocabKeys().isEmpty());
        assertTrue(repository.readLearnedVocabKeys().isEmpty());
        repository.saveFavoriteVocabKeys(Set.of("日本|nihon|Nhật Bản"));
        repository.saveLearnedVocabKeys(Set.of("先生|sensei|giáo viên"));

        var reloaded = new VocabRepository(directory);
        assertEquals(Set.of("日本|nihon|Nhật Bản"), reloaded.readFavoriteVocabKeys());
        assertEquals(Set.of("先生|sensei|giáo viên"), reloaded.readLearnedVocabKeys());
        reloaded.saveLearnedVocabKeys(Set.of());
        assertTrue(new VocabRepository(directory).readLearnedVocabKeys().isEmpty());
        assertEquals(1, reloaded.readFavoriteVocabKeys().size());
    }

    @Test
    @DisplayName("File tùy chọn hỏng được khôi phục bằng lần lưu tiếp theo")
    void recoversFromMalformedPreferences() throws Exception {
        Files.writeString(tempDir.resolve("favorite_vocab.json"), "{broken");
        Files.writeString(tempDir.resolve("learned_vocab.json"), "null");
        var repository = new VocabRepository(tempDir);
        assertTrue(repository.readFavoriteVocabKeys().isEmpty());
        assertTrue(repository.readLearnedVocabKeys().isEmpty());
        repository.saveFavoriteVocabKeys(Set.of("あ"));
        assertEquals(Set.of("あ"), new VocabRepository(tempDir).readFavoriteVocabKeys());
    }

    @Test
    @DisplayName("Thêm từ giữ lại các từ đã lưu và bảo toàn tiếng Nhật, tiếng Việt")
    void appendsCustomVocabulary() throws Exception {
        var repository = new VocabRepository(tempDir);
        repository.addCustomVocab(vocab("にほん", 1));
        new VocabRepository(tempDir).addCustomVocab(vocab("せんせい", 2));
        var saved = JsonParser.parseString(Files.readString(tempDir.resolve("user_vocab.json"))).getAsJsonArray();
        assertEquals(2, saved.size());
        assertEquals("にほん", saved.get(0).getAsJsonObject().get("kana").getAsString());
        assertEquals("せんせい", saved.get(1).getAsJsonObject().get("kana").getAsString());
        assertEquals("nghĩa thử", saved.get(1).getAsJsonObject().get("meaning").getAsString());
    }

    @Test
    @DisplayName("Lọc từ vựng theo đúng số chương")
    void filtersByChapter() {
        var first = vocab("あ", 1);
        var second = vocab("い", 2);
        assertEquals(List.of(second), new VocabRepository(tempDir)
                .getVocabsByChapter(List.of(first, second), 2));
    }

    @Test
    @DisplayName("Thiếu tài nguyên từ vựng phải báo lỗi")
    void missingResourceFailsClearly() {
        assertThrows(RuntimeException.class,
                () -> new VocabRepository(tempDir).readVocabFromJson("/missing-vocab.json"));
    }

    private static Vocabulary vocab(String kana, int chapter) {
        return new Vocabulary(kana, "", "nghĩa thử", "reading", "", chapter);
    }
}
