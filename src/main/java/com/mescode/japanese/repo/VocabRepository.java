package com.mescode.japanese.repo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.vocab.Vocabulary;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VocabRepository {

    private final Type type = new TypeToken<List<Vocabulary>>(){}.getType();
    private final Type keySetType = new TypeToken<Set<String>>(){}.getType();
    private final Gson gson = new Gson();
    private final String customPath = "user_data" + System.getProperty("file.separator") + "user_vocab.json";
    private final String favoritesPath = "user_data" + System.getProperty("file.separator") + "favorite_vocab.json";
    private final String learnedPath = "user_data" + System.getProperty("file.separator") + "learned_vocab.json";

    /**
     * Load built-in vocabs and merge with any user-added vocabs from user_data/user_vocab.json
     */
    public List<Vocabulary> getVocabs() {
        List<Vocabulary> base = readVocabFromJson("/data/vocab.json");
        List<Vocabulary> custom = readCustomVocabs();
        if (custom != null && !custom.isEmpty()) {
            base.addAll(custom);
        }
        return base;
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
        File f = new File(customPath);
        if (!f.exists()) {
            return Collections.emptyList();
        }
        try (Reader r = new FileReader(f, StandardCharsets.UTF_8)) {
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
