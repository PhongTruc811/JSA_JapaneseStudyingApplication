package com.mescode.japanese.repo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mescode.japanese.model.Kanji;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class KanjiRepository {
    private static final String KANJI_RESOURCE = "/data/kanji.json";
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<Kanji>>() { }.getType();

    public List<Kanji> getKanjis() {
        try (InputStream input = KanjiRepository.class.getResourceAsStream(KANJI_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Kanji data file not found: " + KANJI_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                List<Kanji> kanjis = gson.fromJson(reader, listType);
                return kanjis == null ? Collections.emptyList() : kanjis;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load Kanji data", exception);
        }
    }
}