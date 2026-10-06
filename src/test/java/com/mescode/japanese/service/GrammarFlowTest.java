package com.mescode.japanese.service;

import com.mescode.japanese.model.grammar.*;
import com.mescode.japanese.repository.GrammarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ngữ pháp: chấm bài và lưu tiến độ")
class GrammarFlowTest {
    @TempDir Path tempDir;

    @Test
    @DisplayName("Học đủ các điểm ngữ pháp mới mở bài kiểm tra, tải lại vẫn giữ tiến độ")
    void unlocksQuizAndPersistsCompletedPoints() {
        var service = service();
        var chapter = service.getChapter("chapter-1").orElseThrow();
        assertFalse(service.isQuizUnlocked(chapter));
        assertThrows(IllegalArgumentException.class,
                () -> service.markPointCompleted(chapter.getId(), "missing-point"));
        for (var point : chapter.getGrammarPoints()) {
            service.markPointCompleted(chapter.getId(), point.getId());
        }
        var reloaded = service();
        assertTrue(reloaded.isQuizUnlocked(chapter));
        assertFalse(reloaded.isChapterCompleted(chapter));
    }

    @Test
    @DisplayName("Các dạng câu hỏi chấp nhận đáp án đúng và từ chối đáp án sai hoặc trống")
    void gradesSupportedQuestionTypes() {
        var service = service();
        var chapter = service.getChapter("chapter-1").orElseThrow();
        for (var type : GrammarQuestion.Type.values()) {
            var question = chapter.getQuiz().stream().filter(q -> q.getType() == type).findFirst().orElseThrow();
            assertTrue(service.checkAnswer(question, question.getAnswer()), type.name());
            assertFalse(service.checkAnswer(question, List.of("đáp án sai")), type.name());
            assertFalse(service.checkAnswer(question, null), type.name());
        }
        var ordered = new GrammarQuestion();
        ordered.setAnswer(List.of("わたし", "は"));
        assertFalse(service.checkAnswer(ordered, List.of("は", "わたし")));
        assertTrue(service.checkAnswer(ordered, List.of(" わたし ", "は")));
    }

    @Test
    @DisplayName("Đạt từ 70 phần trăm và giữ kết quả tốt nhất sau lần làm kém hơn")
    void passBoundaryAndBestScoreSurviveReload() {
        var chapter = tenQuestionChapter();
        var service = service();
        assertFalse(service.submitQuiz(chapter, answers(6)).passed());
        var passing = service.submitQuiz(chapter, answers(7));
        assertTrue(passing.passed());
        assertEquals(7, passing.correct());
        assertEquals(10, passing.total());
        assertEquals(0, service.submitQuiz(chapter, Map.of()).correct());
        var persisted = service().getChapterProgress(chapter.getId());
        assertEquals(3, persisted.getAttempts());
        assertEquals(7, persisted.getBestCorrect());
        assertEquals(10, persisted.getBestTotal());
    }

    @Test
    @DisplayName("Đặt lại tiến độ xóa cả kết quả đã lưu")
    void resetClearsPersistedProgress() {
        var service = service();
        service.submitQuiz(tenQuestionChapter(), answers(10));
        service.resetProgress();
        assertTrue(new GrammarRepository(progressPath()).loadProgress().getChapters().isEmpty());
        assertEquals(0, service.getChapterProgress("test-chapter").getAttempts());
    }

    @Test
    @DisplayName("File tiến độ hỏng hoặc phiên bản không hỗ trợ trở về trạng thái ban đầu")
    void recoversFromInvalidProgress() throws Exception {
        for (String json : List.of("{broken", "null", "{\"version\":999}")) {
            Files.writeString(progressPath(), json);
            assertTrue(new GrammarRepository(progressPath()).loadProgress().getChapters().isEmpty());
        }
    }

    private Path progressPath() { return tempDir.resolve("progress.json"); }
    private GrammarService service() { return new GrammarService(new GrammarRepository(progressPath())); }

    private static GrammarChapter tenQuestionChapter() {
        var chapter = new GrammarChapter();
        chapter.setId("test-chapter");
        var questions = new ArrayList<GrammarQuestion>();
        for (int i = 0; i < 10; i++) {
            var question = new GrammarQuestion();
            question.setId("q" + i);
            question.setType(GrammarQuestion.Type.MULTIPLE_CHOICE);
            question.setOptions(List.of("は", "を"));
            question.setAnswer(List.of("は"));
            questions.add(question);
        }
        chapter.setQuiz(questions);
        return chapter;
    }

    private static Map<String, List<String>> answers(int correctCount) {
        var answers = new HashMap<String, List<String>>();
        for (int i = 0; i < correctCount; i++) answers.put("q" + i, List.of("は"));
        return answers;
    }
}
