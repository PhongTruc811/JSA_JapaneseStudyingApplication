package com.mescode.japanese.model.grammar;

import java.util.ArrayList;
import java.util.List;

public class GrammarChapter {
    private String id;
    private int number;
    private String title;
    private String description;
    private boolean available;
    private String contentResource;
    private List<GrammarPoint> grammarPoints = new ArrayList<>();
    private List<GrammarQuestion> quiz = new ArrayList<>();
    private List<GrammarExample> applications = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getContentResource() {
        return contentResource;
    }

    public void setContentResource(String contentResource) {
        this.contentResource = contentResource;
    }

    public List<GrammarPoint> getGrammarPoints() {
        return grammarPoints == null ? List.of() : grammarPoints;
    }

    public void setGrammarPoints(List<GrammarPoint> grammarPoints) {
        this.grammarPoints = grammarPoints;
    }

    public List<GrammarQuestion> getQuiz() {
        return quiz == null ? List.of() : quiz;
    }

    public void setQuiz(List<GrammarQuestion> quiz) {
        this.quiz = quiz;
    }

    public List<GrammarExample> getApplications() {
        return applications == null ? List.of() : applications;
    }

    public void setApplications(List<GrammarExample> applications) {
        this.applications = applications;
    }
}
