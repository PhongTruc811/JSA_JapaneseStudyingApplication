package com.mescode.japanese.service;

import com.mescode.japanese.model.Kana;
import com.mescode.japanese.model.KanaType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class KanaServiceTest {

    @Test
    void getRandomKana_whenPreviousIsNull_shouldReturnKanaFromList() {
        List<Kana> kanaList = sampleKanaList();
        KanaService service = new KanaService(kanaList, new SequenceRandom(1), false);

        Kana result = service.getRandomKana(null);

        assertNotNull(result);
        assertSame(kanaList.get(1), result);
    }

    @Test
    void getRandomKana_shouldNotReturnPreviousKana() {
        List<Kana> kanaList = sampleKanaList();
        KanaService service = new KanaService(kanaList, new SequenceRandom(0, 1), false);

        Kana previous = kanaList.get(0);
        Kana result = service.getRandomKana(previous);

        assertNotNull(result);
        assertNotNull(previous);
        assertNotNull(result.getKana());
        assertNotNull(previous.getKana());
        assertEquals("い", result.getKana());
    }

    @Test
    void getRandomGojuon_whenPreviousIsNull_shouldReturnGojuonKana() {
        List<Kana> kanaList = sampleKanaList();
        KanaService service = new KanaService(kanaList, new SequenceRandom(2), false);

        Kana result = service.getRandomGojuon(null);

        assertNotNull(result);
        assertEquals(KanaType.gojuuon, result.getType());
        assertEquals("う", result.getKana());
    }

    @Test
    void getRandomGojuon_shouldNotReturnPreviousKana() {
        List<Kana> kanaList = sampleKanaList();
        KanaService service = new KanaService(kanaList, new SequenceRandom(0, 1), false);

        Kana previous = kanaList.get(0);
        Kana result = service.getRandomGojuon(previous);

        assertNotNull(result);
        assertEquals(KanaType.gojuuon, result.getType());
        assertEquals("い", result.getKana());
    }

    private List<Kana> sampleKanaList() {
        return List.of(
                new Kana("あ", "a", KanaType.gojuuon),
                new Kana("い", "i", KanaType.gojuuon),
                new Kana("う", "u", KanaType.gojuuon)
        );
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int index = 0;

        private SequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = index < values.length ? values[index++] : 0;
            return Math.floorMod(value, bound);
        }
    }
}
