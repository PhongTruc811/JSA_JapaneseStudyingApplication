package com.mescode.japanese.model.grammar;

import java.util.LinkedHashSet;
import java.util.Set;

public class GrammarChapterProgress {
    private Set<String> completedPointIds = new LinkedHashSet<>();
    private int bestCorrect;
    private int bestTotal;
    private int attempts;
    private String lastAttemptAt;

    public Set<String> getCompletedPointIds() {
        if (completedPointIds == null) {
            completedPointIds = new LinkedHashSet<>();
        }
        return completedPointIds;
    }

    public void setCompletedPointIds(Set<String> completedPointIds) {
        this.completedPointIds = completedPointIds;
    }

    public int getBestCorrect() {
        return bestCorrect;
    }

    public void setBestCorrect(int bestCorrect) {
        this.bestCorrect = bestCorrect;
    }

    public int getBestTotal() {
        return bestTotal;
    }

    public void setBestTotal(int bestTotal) {
        this.bestTotal = bestTotal;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public String getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(String lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }
}
