package com.mescode.japanese.repository;

import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarChapterProgress;
import com.mescode.japanese.model.grammar.GrammarProgress;
import com.mescode.japanese.service.GrammarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrammarRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsAndValidatesChapterOneContent() {
        GrammarRepository repository = new GrammarRepository(tempDir.resolve("grammar-progress.json"));
        GrammarService service = new GrammarService(repository);

        assertEquals(3, repository.loadCatalog().getChapters().size());
        assertFalse(repository.loadChapter("chapter-2").isPresent());

        GrammarChapter chapter = repository.loadChapter("chapter-1").orElseThrow();
        assertEquals(6, chapter.getGrammarPoints().size());
        assertEquals(12, chapter.getQuiz().size());
        assertEquals(18, chapter.getGrammarPoints().stream()
                .mapToInt(point -> point.getPractice().size())
                .sum());
        chapter.getGrammarPoints().forEach(point ->
                assertEquals(3, point.getPractice().size()));
        assertTrue(service.validateChapter(chapter).isEmpty());

        Map<String, Long> questionsPerPoint = chapter.getQuiz().stream()
                .collect(Collectors.groupingBy(
                        question -> question.getGrammarPointId(),
                        Collectors.counting()
                ));
        chapter.getGrammarPoints().forEach(point ->
                assertEquals(2L, questionsPerPoint.getOrDefault(point.getId(), 0L)));
    }

    @Test
    void persistsProgressAndRecoversFromMalformedJson() throws Exception {
        Path progressPath = tempDir.resolve("grammar-progress.json");
        GrammarRepository repository = new GrammarRepository(progressPath);
        GrammarProgress progress = repository.loadProgress();
        progress.getChapters()
                .computeIfAbsent("chapter-1", ignored -> new GrammarChapterProgress())
                .getCompletedPointIds()
                .add("gp-1");
        repository.saveProgress(progress);

        GrammarProgress reloaded = new GrammarRepository(progressPath).loadProgress();
        assertTrue(reloaded.getChapters().get("chapter-1")
                .getCompletedPointIds()
                .contains("gp-1"));

        Files.writeString(progressPath, "{not-json", StandardCharsets.UTF_8);
        GrammarProgress recovered = new GrammarRepository(progressPath).loadProgress();
        assertTrue(recovered.getChapters().isEmpty());
    }
}
