package com.mescode.japanese.service;

import com.mescode.japanese.model.vocab.Vocabulary;
import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizQuestion;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class VocabService {
    public final List<Vocabulary> vocabularies;
    private final Random random = new Random();
    private final List<VocabQuizQuestion> quizQuestions;

    public VocabService(List<Vocabulary> allVocabs, VocabQuizConfig config) {
        this.vocabularies = filterVocabs(allVocabs, config);
        Collections.shuffle(this.vocabularies);
        this.quizQuestions = buildQuestions(this.vocabularies, config.showKanji(), random);
        loadVocabs();
    }
    private void loadVocabs() {
        for (Vocabulary vocabulary : vocabularies) {
            if(vocabulary != null){
                System.out.println(vocabulary.toString());
            }
        }
    }

    public Vocabulary getRandomVocab(Vocabulary previous) {
        if (vocabularies == null || vocabularies.isEmpty()) {
            return null;
        }
        if (previous == null) {
            return vocabularies.get(random.nextInt(vocabularies.size()));
        }
        Vocabulary current;
        do {
            current = vocabularies.get(random.nextInt(vocabularies.size()));
        } while (current.equals(previous) && vocabularies.size() > 1);
        return current;
    }

    public List<VocabQuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }

    public static String validateQuizData(List<Vocabulary> allVocabs, VocabQuizConfig config) {
        List<Vocabulary> selected = filterVocabs(allVocabs, config);
        Set<String> answers = selected.stream()
                .map(vocab -> getAnswerText(vocab, config.showKanji()))
                .filter(VocabService::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (answers.size() < 4) {
            return "Selected chapters need at least 4 different "
                    + (config.showKanji() ? "kanji" : "kana") + " answers.";
        }
        return null;
    }

    private static List<Vocabulary> filterVocabs(List<Vocabulary> allVocabs, VocabQuizConfig config) {
        if (allVocabs == null || config == null) {
            return new ArrayList<>();
        }
        return allVocabs.stream()
                .filter(vocab -> vocab != null && vocab.getChapter() != null)
                .filter(vocab -> config.chapters().contains(vocab.getChapter()))
                .filter(vocab -> hasText(getAnswerText(vocab, config.showKanji())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static List<VocabQuizQuestion> buildQuestions(
            List<Vocabulary> selectedVocabs, boolean showKanji, Random random) {
        List<String> answerPool = selectedVocabs.stream()
                .map(vocab -> getAnswerText(vocab, showKanji))
                .distinct()
                .toList();
        if (answerPool.size() < 4) {
            return Collections.emptyList();
        }

        List<VocabQuizQuestion> questions = new ArrayList<>();
        for (Vocabulary vocab : selectedVocabs) {
            String correctAnswer = getAnswerText(vocab, showKanji);
            List<String> distractors = answerPool.stream()
                    .filter(answer -> !answer.equals(correctAnswer))
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(distractors, random);
            List<String> options = new ArrayList<>(distractors.subList(0, 3));
            options.add(correctAnswer);
            Collections.shuffle(options, random);
            questions.add(new VocabQuizQuestion(vocab.getMeaning(), options, correctAnswer));
        }
        Collections.shuffle(questions, random);
        return List.copyOf(questions);
    }

    private static String getAnswerText(Vocabulary vocab, boolean showKanji) {
        return showKanji ? vocab.getKanji() : vocab.getKana();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}


