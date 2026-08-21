package com.mescode.japanese.service;

import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarChapterProgress;
import com.mescode.japanese.model.grammar.GrammarQuestion;
import com.mescode.japanese.model.grammar.GrammarQuizResult;
import com.mescode.japanese.repository.GrammarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void checksAllSupportedQuestionTypes() {
        GrammarService service = new GrammarService(
                new GrammarRepository(tempDir.resolve("progress.json"))
        );
        GrammarChapter chapter = service.getChapter("chapter-1").orElseThrow();

        for (GrammarQuestion.Type type : GrammarQuestion.Type.values()) {
            GrammarQuestion question = chapter.getQuiz().stream()
                    .filter(candidate -> candidate.getType() == type)
                    .findFirst()
                    .orElseThrow();
            assertTrue(service.checkAnswer(question, question.getAnswer()));
            assertFalse(service.checkAnswer(question, List.of("đáp án sai")));
        }
    }

    @Test
    void unlocksQuizAndPersistsBestPassingAttempt() {
        Path progressPath = tempDir.resolve("progress.json");
        GrammarService service = new GrammarService(new GrammarRepository(progressPath));
        GrammarChapter chapter = service.getChapter("chapter-1").orElseThrow();

        assertFalse(service.isQuizUnlocked(chapter));
        chapter.getGrammarPoints().forEach(point ->
                service.markPointCompleted(chapter.getId(), point.getId()));
        assertTrue(service.isQuizUnlocked(chapter));

        GrammarQuizResult firstAttempt = service.submitQuiz(chapter, answersFor(chapter, 8));
        assertEquals(8, firstAttempt.correct());
        assertFalse(firstAttempt.passed());

        GrammarQuizResult secondAttempt = service.submitQuiz(chapter, answersFor(chapter, 9));
        assertEquals(9, secondAttempt.correct());
        assertTrue(secondAttempt.passed());
        assertTrue(service.isChapterCompleted(chapter));

        GrammarService reloadedService = new GrammarService(new GrammarRepository(progressPath));
        GrammarChapterProgress persisted = reloadedService.getChapterProgress(chapter.getId());
        assertEquals(2, persisted.getAttempts());
        assertEquals(9, persisted.getBestCorrect());
        assertEquals(12, persisted.getBestTotal());
        assertFalse(persisted.getLastAttemptAt().isBlank());
    }

    private static Map<String, List<String>> answersFor(GrammarChapter chapter, int correctCount) {
        Map<String, List<String>> answers = new LinkedHashMap<>();
        for (int index = 0; index < chapter.getQuiz().size(); index++) {
            GrammarQuestion question = chapter.getQuiz().get(index);
            answers.put(
                    question.getId(),
                    index < correctCount ? question.getAnswer() : List.of("đáp án sai")
            );
        }
        return answers;
    }
}
