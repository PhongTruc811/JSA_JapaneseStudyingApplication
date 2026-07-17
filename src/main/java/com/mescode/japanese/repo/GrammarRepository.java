package com.mescode.japanese.repo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.GrammarQuestion;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class GrammarRepository {
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<GrammarQuestion>>(){}.getType();
    private final String userScorePath = "user_data" + File.separator + "grammar_score.json";

    public List<GrammarQuestion> loadQuestions() {
        try (InputStream is = GrammarRepository.class.getResourceAsStream("/data/grammar.json")) {
            if (is == null) return Collections.emptyList();
            try (Reader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return gson.fromJson(r, listType);
            }
        } catch (Exception e) {
            System.err.println("Failed to load grammar questions: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void saveLastScore(int score) {
        try {
            File dir = new File("user_data");
            if (!dir.exists()) dir.mkdirs();
            try (Writer w = new FileWriter(userScorePath, StandardCharsets.UTF_8)) {
                gson.toJson(Collections.singletonMap("lastScore", score), w);
            }
        } catch (Exception e) {
            System.err.println("Failed to save grammar score: " + e.getMessage());
        }
    }

    public int loadLastScore() {
        File f = new File(userScorePath);
        if (!f.exists()) return 0;
        try (Reader r = new FileReader(f, StandardCharsets.UTF_8)) {
            java.util.Map map = gson.fromJson(r, java.util.Map.class);
            if (map != null && map.get("lastScore") instanceof Number) {
                return ((Number) map.get("lastScore")).intValue();
            }
        } catch (Exception e) {
            System.err.println("Failed to read grammar score: " + e.getMessage());
        }
        return 0;
    }
}
