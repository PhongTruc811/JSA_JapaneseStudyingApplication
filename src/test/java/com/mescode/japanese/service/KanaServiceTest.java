package com.mescode.japanese.service;

import com.mescode.japanese.model.kana.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chọn câu hỏi Kana")
class KanaServiceTest {
    @Test
    @DisplayName("Không lặp lại kana vừa hỏi khi còn lựa chọn khác")
    void avoidsPreviousKana() {
        var first = new Kana("あ", "a", KanaType.gojuuon);
        var second = new Kana("い", "i", KanaType.gojuuon);
        var service = new KanaService(List.of(first, second), new Random(12), false);
        assertSame(second, service.getRandomKana(first));
    }

    @Test
    @DisplayName("Nhóm chỉ có một kana vẫn trả lời được nhiều lượt")
    void supportsSingleKana() {
        var kana = new Kana("ん", "n", KanaType.gojuuon);
        var service = new KanaService(List.of(kana), new Random(1), false);
        assertSame(kana, service.getRandomKana(null));
        assertSame(kana, service.getRandomKana(kana));
    }

    @Test
    @DisplayName("Lọc đúng nhóm và báo lỗi khi nhóm không có dữ liệu")
    void filtersSelectedGroup() {
        var plain = new Kana("あ", "a", KanaType.gojuuon);
        var combined = new Kana("きゃ", "kya", KanaType.youon);
        var service = new KanaService(List.of(plain, combined), KanaQuizGroup.YOUON);
        assertSame(combined, service.getRandomKana(null));
        assertThrows(IllegalArgumentException.class,
                () -> new KanaService(List.of(plain), KanaQuizGroup.YOUON));
        assertTrue(KanaQuizGroup.DAKUON.matches(KanaType.handakuon));
        assertFalse(KanaQuizGroup.GOJUUON.matches(KanaType.youon));
    }
}
