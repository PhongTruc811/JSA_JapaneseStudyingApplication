package com.mescode.japanese.service;

import com.mescode.japanese.model.Vocabulary;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Getter
public class VocabService {
    public final List<Vocabulary> vocabularies;
    private final Random random = new Random();

    public VocabService(List<Vocabulary> vocabularies) {
        this.vocabularies = vocabularies == null ? new ArrayList<>() : new ArrayList<>(vocabularies);
        Collections.shuffle(this.vocabularies);
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
}


