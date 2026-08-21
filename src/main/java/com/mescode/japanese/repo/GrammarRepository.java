package com.mescode.japanese.repo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mescode.japanese.model.grammar.GrammarCatalog;
import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarProgress;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class GrammarRepository {
    public static final String CATALOG_RESOURCE = "/data/grammar/catalog.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path progressPath;

    public GrammarRepository() {
        this(Path.of("user_data", "grammar_progress.json"));
    }

    public GrammarRepository(Path progressPath) {
        this.progressPath = progressPath;
    }

    public GrammarCatalog loadCatalog() {
        GrammarCatalog catalog = readResource(CATALOG_RESOURCE, GrammarCatalog.class);
        return catalog == null ? new GrammarCatalog() : catalog;
    }

    public List<GrammarChapter> loadChapters() {
        return loadCatalog().getChapters();
    }

    public Optional<GrammarChapter> loadChapter(String chapterId) {
        if (chapterId == null || chapterId.isBlank()) {
            return Optional.empty();
        }
        return loadChapters().stream()
                .filter(chapter -> chapterId.equals(chapter.getId()))
                .filter(GrammarChapter::isAvailable)
                .findFirst()
                .flatMap(metadata -> loadChapterContent(metadata).map(content -> {
                    content.setAvailable(true);
                    content.setContentResource(metadata.getContentResource());
                    return content;
                }));
    }

    public GrammarProgress loadProgress() {
        if (progressPath == null || !Files.isRegularFile(progressPath)) {
            return new GrammarProgress();
        }
        try (Reader reader = Files.newBufferedReader(progressPath, StandardCharsets.UTF_8)) {
            GrammarProgress progress = gson.fromJson(reader, GrammarProgress.class);
            return progress == null || progress.getVersion() != 1 ? new GrammarProgress() : progress;
        } catch (Exception exception) {
            System.err.println("Failed to read grammar progress: " + exception.getMessage());
            return new GrammarProgress();
        }
    }

    public void saveProgress(GrammarProgress progress) {
        if (progressPath == null) {
            return;
        }
        try {
            Path parent = progressPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(progressPath, StandardCharsets.UTF_8)) {
                gson.toJson(progress == null ? new GrammarProgress() : progress, writer);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu tiến độ ngữ pháp.", exception);
        }
    }

    public void resetProgress() {
        saveProgress(new GrammarProgress());
    }

    public Path getProgressPath() {
        return progressPath;
    }

    private Optional<GrammarChapter> loadChapterContent(GrammarChapter metadata) {
        String resource = metadata.getContentResource();
        if (resource == null || resource.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(readResource(resource, GrammarChapter.class));
    }

    private <T> T readResource(String resourcePath, Class<T> type) {
        try (InputStream stream = GrammarRepository.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.err.println("Grammar resource not found: " + resourcePath);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return gson.fromJson(reader, type);
            }
        } catch (Exception exception) {
            System.err.println("Failed to load grammar resource " + resourcePath + ": "
                    + exception.getMessage());
            return null;
        }
    }
}
