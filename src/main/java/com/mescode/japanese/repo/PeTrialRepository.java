package com.mescode.japanese.repo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.petrial.PeTrialQuestion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PeTrialRepository {
    public static final String DATA_RESOURCE = "/data/pe_trial/pe_trial.json";
    private static final List<String> EXPECTED_OPTIONS = List.of("A", "B", "C", "D");
    private static final int EXPECTED_QUESTION_COUNT = 30;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<PeTrialQuestion>>() { }.getType();

    public List<PeTrialQuestion> loadQuestions() {
        try (InputStream input = PeTrialRepository.class.getResourceAsStream(DATA_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("PE Trial data is missing: " + DATA_RESOURCE);
            }
            try (Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                List<PeTrialQuestion> questions = gson.fromJson(reader, listType);
                validateQuestions(questions);
                return List.copyOf(questions);
            }
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load PE Trial data", exception);
        }
    }

    static void validateQuestions(List<PeTrialQuestion> questions) {
        if (questions == null || questions.size() != EXPECTED_QUESTION_COUNT) {
            throw new IllegalStateException("PE Trial must contain exactly 30 questions");
        }

        Set<String> ids = new HashSet<>();
        for (int index = 0; index < questions.size(); index++) {
            PeTrialQuestion question = questions.get(index);
            String expectedId = "pe_trial_%02d".formatted(index + 1);
            if (question == null || !expectedId.equals(question.id())) {
                throw new IllegalStateException("Invalid or out-of-order PE Trial id at question " + (index + 1));
            }
            if (!ids.add(question.id())) {
                throw new IllegalStateException("Duplicate PE Trial id: " + question.id());
            }
            if (!EXPECTED_OPTIONS.equals(question.options())) {
                throw new IllegalStateException("Question " + question.id() + " must use options A, B, C and D");
            }
            if (!EXPECTED_OPTIONS.contains(question.answer())) {
                throw new IllegalStateException("Invalid answer for " + question.id());
            }
            if (question.image() == null || PeTrialRepository.class.getResource(question.image()) == null) {
                throw new IllegalStateException("Missing image for " + question.id() + ": " + question.image());
            }
        }
    }
}
