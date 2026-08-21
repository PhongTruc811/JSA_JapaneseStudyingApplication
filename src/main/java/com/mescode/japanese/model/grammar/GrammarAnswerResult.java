package com.mescode.japanese.model.grammar;

import java.util.List;

public record GrammarAnswerResult(
        GrammarQuestion question,
        List<String> submittedAnswer,
        boolean correct
) {
}
