package com.mescode.japanese.repository;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.service.GrammarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dữ liệu học đóng gói cùng ứng dụng")
class LearningContentRepositoryTest {
    @Test
    @DisplayName("Hiragana và Katakana có ký tự, cách đọc và loại hợp lệ")
    void kanaResourcesAreValid() {
        KanaRepository repository = new KanaRepository();
        for (List<Kana> entries : List.of(repository.readHira(), repository.readKata())) {
            assertFalse(entries.isEmpty());
            var seen = new HashSet<String>();
            for (Kana kana : entries) {
                assertNotNull(kana);
                assertText(kana.getKana());
                assertText(kana.getRomaji());
                assertNotNull(kana.getType(), "Loại kana không hợp lệ: " + kana.getKana());
                assertTrue(seen.add(kana.getKana()), "Kana bị trùng: " + kana.getKana());
            }
        }
    }

    @Test
    @DisplayName("Từ vựng đọc được từ classpath và có nội dung bắt buộc")
    void vocabularyResourceIsValid() {
        var entries = new VocabRepository().readVocabFromJson("/data/vocab.json");
        assertFalse(entries.isEmpty());
        for (var vocab : entries) {
            assertText(vocab.getKana());
            assertText(vocab.getMeaning());
            assertText(vocab.getRomaji());
            assertNotNull(vocab.getChapter());
            assertTrue(vocab.getChapter() > 0);
        }
    }

    @Test
    @DisplayName("Kanji có đủ chữ, cách đọc, nghĩa và bài học")
    void kanjiResourceIsValid() {
        var entries = new KanjiRepository().getKanjis();
        assertFalse(entries.isEmpty());
        for (var kanji : entries) {
            assertText(kanji.getKanji());
            assertText(kanji.getHiragana());
            assertText(kanji.getHanViet());
            assertText(kanji.getMeaning());
            assertTrue(kanji.getUnit() > 0);
        }
    }

    @Test
    @DisplayName("Đề thi thử có đủ 30 ảnh và giữ đáp án đã xác nhận")
    void trialExamResourcesAndAnswerKeyAreIntact() {
        var questions = new PeTrialRepository().loadQuestions();
        assertEquals(30, questions.size());
        StringBuilder key = new StringBuilder();
        for (int index = 0; index < questions.size(); index++) {
            var question = questions.get(index);
            assertEquals("pe_trial_%02d".formatted(index + 1), question.id());
            assertNotNull(getClass().getResource(question.image()), question.image());
            assertEquals(List.of("A", "B", "C", "D"), question.options());
            assertTrue(question.options().contains(question.answer()));
            key.append(question.answer());
        }
        assertEquals("BBBBBBACBBCCBBBBBCBABBABAABDBB", key.toString());
    }

    @Test
    @DisplayName("Các chương ngữ pháp đang mở có nội dung hợp lệ")
    void availableGrammarChaptersAreValid() {
        var repository = new GrammarRepository(null);
        var service = new GrammarService(repository);
        var available = repository.loadChapters().stream().filter(chapter -> chapter.isAvailable()).toList();
        assertFalse(available.isEmpty());
        for (var metadata : available) {
            var chapter = repository.loadChapter(metadata.getId()).orElseThrow();
            assertFalse(chapter.getQuiz().isEmpty());
            assertEquals(List.of(), service.validateChapter(chapter), metadata.getId());
        }
        assertTrue(repository.loadChapter("missing-chapter").isEmpty());
    }

    private static void assertText(String value) {
        assertNotNull(value);
        assertFalse(value.isBlank());
    }
}
