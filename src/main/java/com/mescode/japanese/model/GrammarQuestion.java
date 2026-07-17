package com.mescode.japanese.model;

import java.util.List;

public class GrammarQuestion {
    public enum Type { MCQ, FILL, TRUE_FALSE }

    private String id;
    private Type type;
    private String question;
    private List<String> options; // for MCQ
    private String answer; // canonical answer (for MCQ store option value; for TF "true"/"false"; for FILL store normalized answer)

    public GrammarQuestion() {}

    public GrammarQuestion(String id, Type type, String question, List<String> options, String answer) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
