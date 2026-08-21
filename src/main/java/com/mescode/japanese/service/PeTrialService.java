package com.mescode.japanese.service;

import com.mescode.japanese.model.petrial.PeTrialQuestion;
import com.mescode.japanese.repo.PeTrialRepository;

import java.util.List;

public class PeTrialService {
    private final List<PeTrialQuestion> questions;

    public PeTrialService(PeTrialRepository repository) {
        this(repository.loadQuestions());
    }

    public PeTrialService(List<PeTrialQuestion> questions) {
        this.questions = questions == null ? List.of() : List.copyOf(questions);
    }

    public List<PeTrialQuestion> getQuestions() {
        return questions;
    }
}
