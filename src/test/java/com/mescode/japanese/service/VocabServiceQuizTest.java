package com.mescode.japanese.service;

import com.mescode.japanese.model.vocab.VocabQuizConfig;
import com.mescode.japanese.model.vocab.VocabQuizDifficulty;
import com.mescode.japanese.model.vocab.VocabQuizQuestion;
import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VocabServiceQuizTest {
    private final List<Vocabulary> vocabs = List.of(
            new Vocabulary("にほん", "日本", "Nhật Bản", "nihon", "", 1),
            new Vocabulary("かんこく", "韓国", "Hàn Quốc", "kankoku", "", 1),
            new Vocabulary("ちゅうごく", "中国", "Trung Quốc", "chuugoku", "", 2),
            new Vocabulary("たい", "タイ", "Thái Lan", "tai", "", 2),
            new Vocabulary("いぎりす", "英国", "Anh", "igirisu", "", 3)
    );

    @Test
    void createsKanaQuestionsWithFourDistinctOptions() {
        VocabQuizConfig config = new VocabQuizConfig(false, Set.of(1, 2), VocabQuizDifficulty.EASY);
        VocabService service = new VocabService(vocabs, config);

        assertEquals(4, service.getQuizQuestions().size());
        for (VocabQuizQuestion question : service.getQuizQuestions()) {
            assertEquals(4, question.options().size());
            assertEquals(4, question.options().stream().distinct().count());
            assertTrue(question.options().contains(question.correctAnswer()));
            assertTrue(question.options().stream().allMatch(answer -> List.of("にほん", "かんこく", "ちゅうごく", "たい").contains(answer)));
        }
    }

    @Test
    void createsKanjiQuestionsWhenKanjiModeIsSelected() {
        VocabQuizConfig config = new VocabQuizConfig(true, Set.of(1, 2), VocabQuizDifficulty.HARD);
        VocabService service = new VocabService(vocabs, config);

        assertTrue(service.getQuizQuestions().stream()
                .flatMap(question -> question.options().stream())
                .allMatch(answer -> List.of("日本", "韓国", "中国", "タイ").contains(answer)));
    }

    @Test
    void rejectsQuizDataWithFewerThanFourUniqueAnswers() {
        VocabQuizConfig config = new VocabQuizConfig(false, Set.of(1), VocabQuizDifficulty.EASY);

        assertTrue(VocabService.validateQuizData(vocabs, config).contains("at least 4"));
        assertNull(VocabService.validateQuizData(vocabs,
                new VocabQuizConfig(false, Set.of(1, 2), VocabQuizDifficulty.EASY)));
    }
}
