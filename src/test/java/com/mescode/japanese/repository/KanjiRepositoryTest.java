package com.mescode.japanese.repository;

import com.mescode.japanese.model.kanji.Kanji;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KanjiRepositoryTest {
    private final KanjiRepository repository = new KanjiRepository();

    @Test
    void getKanjis_shouldLoadAllThreeUnits() {
        List<Kanji> kanjis = repository.getKanjis();

        assertNotNull(kanjis);
        assertEquals(96, kanjis.size());
        assertTrue(kanjis.stream().anyMatch(kanji -> kanji.getUnit() == 1));
        assertTrue(kanjis.stream().anyMatch(kanji -> kanji.getUnit() == 2));
        assertTrue(kanjis.stream().anyMatch(kanji -> kanji.getUnit() == 3));
        assertTrue(kanjis.stream().allMatch(this::isValidKanji));
    }

    private boolean isValidKanji(Kanji kanji) {
        return kanji != null
                && kanji.getUnit() >= 1 && kanji.getUnit() <= 3
                && hasText(kanji.getHanViet())
                && hasText(kanji.getKanji())
                && hasText(kanji.getHiragana())
                && hasText(kanji.getMeaning());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}