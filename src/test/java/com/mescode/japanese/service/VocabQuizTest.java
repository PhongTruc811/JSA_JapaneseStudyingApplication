package com.mescode.japanese.service;

import com.mescode.japanese.model.vocab.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tạo đề trắc nghiệm từ vựng")
class VocabQuizTest {
    private final List<Vocabulary> entries = List.of(
            new Vocabulary("にほん", "日本", "Nhật Bản", "nihon", "", 1),
            new Vocabulary("かんこく", "韓国", "Hàn Quốc", "kankoku", "", 1),
            new Vocabulary("ちゅうごく", "中国", "Trung Quốc", "chuugoku", "", 2),
            new Vocabulary("たい", "タイ", "Thái Lan", "tai", "", 2),
            new Vocabulary("いぎりす", "英国", "Anh", "igirisu", "", 3));

    @Test
    @DisplayName("Câu hỏi Kana có bốn lựa chọn khác nhau, thuộc chương đã chọn")
    void buildsKanaQuestionsFromSelectedChapters() {
        var config = new VocabQuizConfig(false, Set.of(1, 2), VocabQuizDifficulty.EASY);
        var questions = new VocabService(entries, config).getQuizQuestions();
        assertEquals(4, questions.size());
        var expected = Set.of("にほん", "かんこく", "ちゅうごく", "たい");
        for (var question : questions) {
            assertEquals(4, question.options().size());
            assertEquals(expected, Set.copyOf(question.options()));
            assertTrue(question.options().contains(question.correctAnswer()));
            assertTrue(question.isCorrect(question.correctAnswer()));
            assertFalse(question.isCorrect("wrong"));
        }
    }

    @Test
    @DisplayName("Chế độ Kanji dùng chữ Kanji làm lựa chọn")
    void buildsKanjiQuestions() {
        var config = new VocabQuizConfig(true, Set.of(1, 2), VocabQuizDifficulty.HARD);
        var questions = new VocabService(entries, config).getQuizQuestions();
        assertEquals(4, questions.size());
        for (var question : questions) {
            assertEquals(Set.of("日本", "韓国", "中国", "タイ"), Set.copyOf(question.options()));
        }
    }

    @Test
    @DisplayName("Không tạo đề khi thiếu bốn đáp án phân biệt, kể cả dữ liệu trùng")
    void rejectsInsufficientUniqueAnswers() {
        var config = new VocabQuizConfig(false, Set.of(1), VocabQuizDifficulty.EASY);
        var duplicates = List.of(entries.get(0), entries.get(0), entries.get(1), entries.get(1));
        assertNotNull(VocabService.validateQuizData(duplicates, config));
        assertTrue(new VocabService(duplicates, config).getQuizQuestions().isEmpty());
        assertTrue(new VocabService(List.of(), config).getQuizQuestions().isEmpty());
    }
}
