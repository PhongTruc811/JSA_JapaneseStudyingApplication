package com.mescode.japanese.model.grammar;

import java.util.List;

public record GrammarQuizResult(
        int correct,
        int total,
        boolean passed,
        List<GrammarAnswerResult> answers
) {
}
