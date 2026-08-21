package com.mescode.japanese.repository;

import com.mescode.japanese.model.petrial.PeTrialQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PeTrialRepositoryTest {
    @Test
    void loadQuestions_shouldLoadOrderedExamAndResources() {
        List<PeTrialQuestion> questions = new PeTrialRepository().loadQuestions();

        assertEquals(30, questions.size());
        for (int index = 0; index < questions.size(); index++) {
            PeTrialQuestion question = questions.get(index);
            assertEquals("pe_trial_%02d".formatted(index + 1), question.id());
            assertEquals("/data/pe_trial/Q" + (index + 1) + ".jpg", question.image());
            assertEquals(List.of("A", "B", "C", "D"), question.options());
            assertNotNull(PeTrialRepository.class.getResource(question.image()));
        }
    }

    @Test
    void loadQuestions_shouldUseConfirmedAnswerKey() {
        List<PeTrialQuestion> questions = new PeTrialRepository().loadQuestions();

        String answerKey = questions.stream()
                .map(PeTrialQuestion::answer)
                .reduce("", String::concat);

        assertEquals("BBBBBBACBBCCBBBBBCBABBABAABDBB", answerKey);
        assertEquals("A", questions.get(6).answer());
        assertEquals("B", questions.get(12).answer());
    }
}
