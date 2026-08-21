package com.mescode.japanese.model.grammar;

import java.util.ArrayList;
import java.util.List;

public class GrammarCatalog {
    private List<GrammarChapter> chapters = new ArrayList<>();

    public List<GrammarChapter> getChapters() {
        return chapters == null ? List.of() : chapters;
    }

    public void setChapters(List<GrammarChapter> chapters) {
        this.chapters = chapters;
    }
}
