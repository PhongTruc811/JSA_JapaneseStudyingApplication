package com.mescode.japanese.model.grammar;

import java.util.ArrayList;
import java.util.List;

public class GrammarPoint {
    private String id;
    private String pattern;
    private String meaning;
    private String explanation;
    private String commonMistake;
    private List<GrammarExample> examples = new ArrayList<>();
    private List<GrammarQuestion> practice = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getCommonMistake() {
        return commonMistake;
    }

    public void setCommonMistake(String commonMistake) {
        this.commonMistake = commonMistake;
    }

    public List<GrammarExample> getExamples() {
        return examples == null ? List.of() : examples;
    }

    public void setExamples(List<GrammarExample> examples) {
        this.examples = examples;
    }

    public List<GrammarQuestion> getPractice() {
        return practice == null ? List.of() : practice;
    }

    public void setPractice(List<GrammarQuestion> practice) {
        this.practice = practice;
    }
}
