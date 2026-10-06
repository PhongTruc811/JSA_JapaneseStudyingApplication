package com.mescode.japanese.service;

import com.mescode.japanese.model.grammar.GrammarAnswerResult;
import com.mescode.japanese.model.grammar.GrammarChapter;
import com.mescode.japanese.model.grammar.GrammarChapterProgress;
import com.mescode.japanese.model.grammar.GrammarPoint;
import com.mescode.japanese.model.grammar.GrammarProgress;
import com.mescode.japanese.model.grammar.GrammarQuestion;
import com.mescode.japanese.model.grammar.GrammarQuizResult;
import com.mescode.japanese.repository.GrammarRepository;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GrammarService {
    public static final double PASS_RATIO = 0.70d;

    private final GrammarRepository repository;
    private GrammarProgress progress;

    public GrammarService(GrammarRepository repository) {
        this.repository = repository;
        this.progress = repository.loadProgress();
    }

    public List<GrammarChapter> getChapters() {
        return repository.loadChapters();
    }

    public Optional<GrammarChapter> getChapter(String chapterId) {
        return repository.loadChapter(chapterId);
    }

    public synchronized GrammarChapterProgress getChapterProgress(String chapterId) {
        return progress.getChapters().computeIfAbsent(chapterId, ignored -> new GrammarChapterProgress());
    }

    public synchronized boolean isPointCompleted(String chapterId, String pointId) {
        return getChapterProgress(chapterId).getCompletedPointIds().contains(pointId);
    }

    public synchronized void markPointCompleted(String chapterId, String pointId) {
        GrammarChapter chapter = getChapter(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter không tồn tại: " + chapterId));
        boolean validPoint = chapter.getGrammarPoints().stream()
                .anyMatch(point -> pointId != null && pointId.equals(point.getId()));
        if (!validPoint) {
            throw new IllegalArgumentException("Grammar point không tồn tại: " + pointId);
        }
        if (getChapterProgress(chapterId).getCompletedPointIds().add(pointId)) {
            repository.saveProgress(progress);
        }
    }

    public boolean isQuizUnlocked(GrammarChapter chapter) {
        if (chapter == null) {
            return false;
        }
        Set<String> completed = getChapterProgress(chapter.getId()).getCompletedPointIds();
        return !chapter.getGrammarPoints().isEmpty()
                && chapter.getGrammarPoints().stream().allMatch(point -> completed.contains(point.getId()));
    }

    public boolean isChapterCompleted(GrammarChapter chapter) {
        if (!isQuizUnlocked(chapter)) {
            return false;
        }
        GrammarChapterProgress chapterProgress = getChapterProgress(chapter.getId());
        return ratio(chapterProgress.getBestCorrect(), chapterProgress.getBestTotal()) >= PASS_RATIO;
    }

    public boolean checkAnswer(GrammarQuestion question, List<String> submittedAnswer) {
        if (question == null) {
            return false;
        }
        List<String> expected = normalize(question.getAnswer());
        List<String> submitted = normalize(submittedAnswer);
        return !expected.isEmpty() && expected.equals(submitted);
    }

    public synchronized GrammarQuizResult submitQuiz(
            GrammarChapter chapter,
            Map<String, List<String>> submittedAnswers
    ) {
        if (chapter == null) {
            throw new IllegalArgumentException("Chapter không được để trống.");
        }
        Map<String, List<String>> safeAnswers = submittedAnswers == null
                ? Map.of()
                : submittedAnswers;
        List<GrammarAnswerResult> results = new ArrayList<>();
        int correct = 0;
        for (GrammarQuestion question : chapter.getQuiz()) {
            List<String> answer = List.copyOf(safeAnswers.getOrDefault(question.getId(), List.of()));
            boolean answerCorrect = checkAnswer(question, answer);
            if (answerCorrect) {
                correct++;
            }
            results.add(new GrammarAnswerResult(question, answer, answerCorrect));
        }

        GrammarChapterProgress chapterProgress = getChapterProgress(chapter.getId());
        chapterProgress.setAttempts(chapterProgress.getAttempts() + 1);
        chapterProgress.setLastAttemptAt(Instant.now().toString());
        int total = chapter.getQuiz().size();
        if (ratio(correct, total)
                > ratio(chapterProgress.getBestCorrect(), chapterProgress.getBestTotal())) {
            chapterProgress.setBestCorrect(correct);
            chapterProgress.setBestTotal(total);
        }
        repository.saveProgress(progress);
        return new GrammarQuizResult(correct, total, ratio(correct, total) >= PASS_RATIO, results);
    }

    public synchronized void resetProgress() {
        progress = new GrammarProgress();
        repository.saveProgress(progress);
    }

    public List<String> validateChapter(GrammarChapter chapter) {
        List<String> errors = new ArrayList<>();
        if (chapter == null) {
            return List.of("Chapter không tồn tại.");
        }
        if (isBlank(chapter.getId())) {
            errors.add("Chapter thiếu id.");
        }
        if (chapter.getGrammarPoints().isEmpty()) {
            errors.add("Chapter chưa có grammar point.");
        }
        Set<String> pointIds = new HashSet<>();
        Set<String> practiceIds = new HashSet<>();
        for (GrammarPoint point : chapter.getGrammarPoints()) {
            if (isBlank(point.getId()) || !pointIds.add(point.getId())) {
                errors.add("Grammar point có id thiếu hoặc trùng.");
            }
            if (isBlank(point.getPattern()) || point.getPractice().size() < 3) {
                errors.add("Grammar point " + point.getId()
                        + " thiếu nội dung hoặc chưa đủ 3 câu luyện nhanh.");
            }
            for (GrammarQuestion practice : point.getPractice()) {
                if (isBlank(practice.getId()) || !practiceIds.add(practice.getId())) {
                    errors.add("Bài luyện nhanh có id thiếu hoặc trùng.");
                }
                if (!java.util.Objects.equals(point.getId(), practice.getGrammarPointId())) {
                    errors.add("Bài luyện " + practice.getId()
                            + " tham chiếu grammar point không hợp lệ.");
                }
                if (!hasValidQuestionPayload(practice)) {
                    errors.add("Bài luyện " + practice.getId()
                            + " có payload đáp án không hợp lệ.");
                }
            }
        }

        Set<String> questionIds = new HashSet<>();
        Map<String, Integer> coverage = new LinkedHashMap<>();
        for (String pointId : pointIds) {
            coverage.put(pointId, 0);
        }
        for (GrammarQuestion question : chapter.getQuiz()) {
            if (isBlank(question.getId()) || !questionIds.add(question.getId())) {
                errors.add("Quiz có id thiếu hoặc trùng.");
            }
            if (!pointIds.contains(question.getGrammarPointId())) {
                errors.add("Quiz " + question.getId() + " tham chiếu grammar point không hợp lệ.");
            } else {
                coverage.computeIfPresent(question.getGrammarPointId(), (ignored, count) -> count + 1);
            }
            if (!hasValidQuestionPayload(question)) {
                errors.add("Quiz " + question.getId() + " có payload đáp án không hợp lệ.");
            }
        }
        coverage.forEach((pointId, count) -> {
            if (count < 1) {
                errors.add("Grammar point " + pointId + " chưa được phủ trong quiz.");
            }
        });
        return errors;
    }

    private boolean hasValidQuestionPayload(GrammarQuestion question) {
        return question != null
                && question.getType() != null
                && !question.getOptions().isEmpty()
                && !question.getAnswer().isEmpty()
                && containsAnswerTokens(question);
    }

    private boolean containsAnswerTokens(GrammarQuestion question) {
        List<String> options = normalize(question.getOptions());
        return options.containsAll(normalize(question.getAnswer()));
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(this::normalize).toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ");
    }

    private double ratio(int correct, int total) {
        return total <= 0 ? 0d : (double) correct / total;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
