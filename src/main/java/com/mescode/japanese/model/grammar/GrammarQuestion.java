package com.mescode.japanese.model.grammar;

import java.util.ArrayList;
import java.util.List;

public class GrammarQuestion {
    public enum Type {
        MULTIPLE_CHOICE,
        WORD_BANK,
        SENTENCE_ORDER
    }

    private String id;
    private String grammarPointId;
    private Type type;
    private String prompt;
    private String reading;
    private List<String> options = new ArrayList<>();
    private List<String> answer = new ArrayList<>();
    private String explanation;

    public GrammarQuestion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGrammarPointId() {
        return grammarPointId;
    }

    public void setGrammarPointId(String grammarPointId) {
        this.grammarPointId = grammarPointId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public List<String> getOptions() {
        return options == null ? List.of() : options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getAnswer() {
        return answer == null ? List.of() : answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
