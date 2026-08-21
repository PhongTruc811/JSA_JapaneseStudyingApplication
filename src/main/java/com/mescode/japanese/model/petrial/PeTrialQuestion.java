package com.mescode.japanese.model.petrial;

import java.util.List;

public record PeTrialQuestion(String id, String image, List<String> options, String answer) {
    public PeTrialQuestion {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public boolean isCorrect(String selectedAnswer) {
        return selectedAnswer != null && answer.equals(selectedAnswer);
    }
}
