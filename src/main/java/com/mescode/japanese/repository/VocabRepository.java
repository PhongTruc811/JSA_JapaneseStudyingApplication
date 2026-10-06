package com.mescode.japanese.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.vocab.Vocabulary;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class VocabRepository {

    private final Type type = new TypeToken<List<Vocabulary>>(){}.getType();
    private final Type keySetType = new TypeToken<Set<String>>(){}.getType();
    private final Gson gson = new Gson();
    private final String customPath = "user_data" + System.getProperty("file.separator") + "user_vocab.json";
    private final String favoritesPath = "user_data" + System.getProperty("file.separator") + "favorite_vocab.json";
    private final String learnedPath = "user_data" + System.getProperty("file.separator") + "learned_vocab.json";

    public VocabRepository() {
    }

    public List<Vocabulary> getVocabs() {
        List<Vocabulary> originalData = readVocabFromJson("/data/vocab.json");

        // data của vocab được hiển thị trong app
        List<Vocabulary> outputData = custom_VocabFilter(originalData);
        return outputData;
    }

    // Hàm này giúp lọc dư liệu vocab theo chapter tùy ý
    private List<Vocabulary> custom_VocabFilter(List<Vocabulary> original) {
        List<Vocabulary> filtered = original.stream()
                .filter(vocabulary -> vocabulary!= null && vocabulary.getChapter() != null)
                .filter(vocabulary -> vocabulary.getChapter() >=1 && vocabulary.getChapter() <=5)
                .toList();
        return filtered;
    }

    public List<Vocabulary> getVocabsByChapter(List<Vocabulary> original, Integer customChapter) {
        List<Vocabulary> filtered = original.stream()
                .filter(vocabulary -> vocabulary!= null && vocabulary.getChapter() != null)
                .filter(vocabulary -> Objects.equals(vocabulary.getKana(), customChapter))
                .toList();
        return filtered;
    }

    public List<Vocabulary> readVocabFromJson(String filePath){
        try(InputStream ipS = VocabRepository.class.getResourceAsStream(filePath)){
            if(ipS == null){
                throw new RuntimeException("Cann not find file: " + filePath);
            }
            try(BufferedReader bfReader = new BufferedReader(new InputStreamReader(ipS, StandardCharsets.UTF_8))){
                return gson.fromJson(bfReader, type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read vocab from json: ", e);
        }

    }

    private List<Vocabulary> readCustomVocabs() {
        File file = new File(customPath);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (Reader r = new FileReader(file, StandardCharsets.UTF_8)) {
            return gson.fromJson(r, type);
        } catch (Exception e) {
            // if corrupted, ignore custom file
            System.err.println("Failed to read custom vocab file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Append a new vocabulary to the custom user file.
     */
    public void addCustomVocab(Vocabulary vocab) {
        List<Vocabulary> custom = new java.util.ArrayList<>(readCustomVocabs());
        custom.add(vocab);
        try {
            ensureUserDataDirectory();
            try (Writer w = new FileWriter(customPath, StandardCharsets.UTF_8)) {
                gson.toJson(custom, w);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save custom vocab: ", e);
        }
    }

    public Set<String> readFavoriteVocabKeys() {
        return readKeySet(favoritesPath, "favorite vocab");
    }

    public void saveFavoriteVocabKeys(Set<String> favoriteKeys) {
        saveKeySet(favoritesPath, favoriteKeys, "favorite vocab");
    }

    public Set<String> readLearnedVocabKeys() {
        return readKeySet(learnedPath, "learned vocab");
    }

    public void saveLearnedVocabKeys(Set<String> learnedKeys) {
        saveKeySet(learnedPath, learnedKeys, "learned vocab");
    }

    private Set<String> readKeySet(String filePath, String label) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new java.util.LinkedHashSet<>();
        }
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Set<String> keys = gson.fromJson(reader, keySetType);
            return keys == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(keys);
        } catch (Exception e) {
            System.err.println("Failed to read " + label + " file: " + e.getMessage());
            return new java.util.LinkedHashSet<>();
        }
    }

    private void saveKeySet(String filePath, Set<String> keys, String label) {
        try {
            ensureUserDataDirectory();
            try (Writer writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(keys == null ? Collections.emptySet() : keys, writer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save " + label + ": ", e);
        }
    }

    private void ensureUserDataDirectory() {
        File dir = new File("user_data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
