package com.mescode.japanese.model.grammar;

import java.util.LinkedHashMap;
import java.util.Map;

public class GrammarProgress {
    private int version = 1;
    private Map<String, GrammarChapterProgress> chapters = new LinkedHashMap<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, GrammarChapterProgress> getChapters() {
        if (chapters == null) {
            chapters = new LinkedHashMap<>();
        }
        return chapters;
    }

    public void setChapters(Map<String, GrammarChapterProgress> chapters) {
        this.chapters = chapters;
    }
}
