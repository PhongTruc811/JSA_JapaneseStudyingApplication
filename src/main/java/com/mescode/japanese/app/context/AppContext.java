package com.mescode.japanese.app.context;

import com.mescode.japanese.model.User;
import com.mescode.japanese.model.Vocabulary;
import com.mescode.japanese.repo.KanaRepository;
import com.mescode.japanese.model.Kana;
import com.mescode.japanese.repo.VocabRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// quyết định lifecycle, bộ não chứa data của app
// AppContext CHỈ tồn tại trong Navigator

public class AppContext {
    private final KanaRepository kanaRepo;
    private final List<Kana> hiraList;
    private final List<Kana> kataList;
    private final VocabRepository vocabRepo;
    private final List<Vocabulary> vocabularies;
    private final Set<String> favoriteVocabKeys;
    private final Set<String> learnedVocabKeys;
    private boolean darkMode = true;
    private boolean soundEnabled = true;
    private int fontSize = 14;
    private final String appVersion = "1.0.0";
    private User currentUser;

    private final java.util.List<java.util.function.Consumer<Boolean>> themeListeners = new java.util.ArrayList<>();

    public AppContext() {
        this.vocabRepo = new VocabRepository();
        this.vocabularies = vocabRepo.getVocabs();
        this.favoriteVocabKeys = new LinkedHashSet<>(vocabRepo.readFavoriteVocabKeys());
        this.learnedVocabKeys = new LinkedHashSet<>(vocabRepo.readLearnedVocabKeys());
        this.kanaRepo = new KanaRepository();
        this.hiraList = kanaRepo.readHiraFromJson();
        this.kataList = kanaRepo.readKataFromJson();
    }

    public List<Kana> getHiraganaList() {
        return hiraList;
    }

    public List<Kana> getKatakanaList() {
        return kataList;
    }

    public List<Vocabulary> getVocabs() {
        return vocabularies;
    }

    public boolean isFavoriteVocab(Vocabulary vocabulary) {
        return favoriteVocabKeys.contains(createVocabKey(vocabulary));
    }

    public void setFavoriteVocab(Vocabulary vocabulary, boolean favorite) {
        String key = createVocabKey(vocabulary);
        if (favorite) {
            favoriteVocabKeys.add(key);
        } else {
            favoriteVocabKeys.remove(key);
        }
        vocabRepo.saveFavoriteVocabKeys(favoriteVocabKeys);
    }

    public int getFavoriteVocabCount() {
        return favoriteVocabKeys.size();
    }

    public boolean isLearnedVocab(Vocabulary vocabulary) {
        return learnedVocabKeys.contains(createVocabKey(vocabulary));
    }

    public void setLearnedVocab(Vocabulary vocabulary, boolean learned) {
        String key = createVocabKey(vocabulary);
        if (learned) {
            learnedVocabKeys.add(key);
        } else {
            learnedVocabKeys.remove(key);
        }
        vocabRepo.saveLearnedVocabKeys(learnedVocabKeys);
    }

    public int getLearnedVocabCount() {
        return learnedVocabKeys.size();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void toggleTheme() {
        this.darkMode = !this.darkMode;
        notifyThemeListeners();
    }

    public void resetPreferences() {
        boolean themeChanged = !this.darkMode;
        this.darkMode = true;
        this.soundEnabled = true;
        this.fontSize = 14;
        if (themeChanged) {
            notifyThemeListeners();
        }
    }

    private void notifyThemeListeners() {
        for (java.util.function.Consumer<Boolean> l : themeListeners) {
            try {
                l.accept(this.darkMode);
            } catch (Exception ignored) {}
        }
    }

    public void addThemeListener(java.util.function.Consumer<Boolean> listener) {
        if (listener != null) themeListeners.add(listener);
    }

    public void removeThemeListener(java.util.function.Consumer<Boolean> listener) {
        if (listener != null) themeListeners.remove(listener);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void resetProgress() {
        learnedVocabKeys.clear();
        vocabRepo.saveLearnedVocabKeys(learnedVocabKeys);
    }

    private String createVocabKey(Vocabulary vocabulary) {
        if (vocabulary == null) {
            return "";
        }
        return safePart(vocabulary.getKana()) + "|"
                + safePart(vocabulary.getRomaji()) + "|"
                + safePart(vocabulary.getMeaning());
    }

    private String safePart(String value) {
        return value == null ? "" : value.trim();
    }
}

