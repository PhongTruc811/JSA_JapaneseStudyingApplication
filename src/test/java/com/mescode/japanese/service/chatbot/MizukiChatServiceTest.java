package com.mescode.japanese.service.chatbot;

import com.mescode.japanese.model.kanji.Kanji;
import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaType;
import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MizukiChatServiceTest {
    private final List<Vocabulary> vocabularies = new ArrayList<>(List.of(
            new Vocabulary("にほん", "日本", "Nhật Bản", "nihon", "", 1),
            new Vocabulary("あつい", "暑い", "nóng do thời tiết", "atsui", "", 2),
            new Vocabulary("あつい", "熱い", "nóng khi chạm vào", "atsui", "", 2),
            new Vocabulary("こんにちは", null, "xin chào", "konnichiwa", "", 1)
    ));

    private final MizukiChatService service = new MizukiChatService(
            List.of(
                    new Kana("き", "ki", KanaType.gojuuon),
                    new Kana("きゃ", "kya", KanaType.youon)
            ),
            List.of(new Kana("キ", "ki", KanaType.gojuuon)),
            () -> vocabularies,
            List.of(new Kanji(1, "NHẬT", "日", "にち", "mặt trời; ngày")),
            () -> 1,
            () -> 2
    );

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void hiraganaQuestion_shouldNotBeMistakenForGreeting() {
        MizukiReply reply = service.createResponse("Hiragana là gì?");

        assertTrue(reply.message().contains("Kana"));
        assertFalse(reply.message().contains("こんにちは"));
    }

    @Test
    void lookup_shouldPreferLongestKanaMatch() {
        MizukiReply reply = service.createResponse("きゃ đọc thế nào?");

        assertTrue(reply.message().contains("「きゃ」"));
        assertTrue(reply.message().contains("kya"));
    }

    @Test
    void lookup_shouldExposeAmbiguousVocabulary() {
        MizukiReply reply = service.createResponse("あつい nghĩa là gì?");

        assertTrue(reply.message().contains("2 cách viết/nghĩa"));
        assertTrue(reply.message().contains("暑い"));
        assertTrue(reply.message().contains("熱い"));
    }

    @Test
    void stopQuiz_shouldNotBeGradedAsAnswer() {
        service.createResponse("Cho mình một quiz");
        MizukiReply reply = service.createResponse("Dừng quiz");

        assertTrue(reply.message().contains("Đã dừng"));
    }

    @Test
    void vocabularySupplier_shouldExposeNewEntries() {
        vocabularies.add(new Vocabulary("せんせい", "先生", "giáo viên", "sensei", "", 3));

        MizukiReply reply = service.createResponse("Tra từ 先生");

        assertTrue(reply.message().contains("giáo viên"));
    }

    @Test
    void resetSession_shouldCancelScheduledReply() {
        CompletableFuture<MizukiReply> pending = service.reply("Cho mình một quiz");

        service.resetSession();

        assertTrue(pending.isCancelled());
    }

    @Test
    void explicitLookup_shouldClearStaleQuizState() {
        service.createResponse("Cho mình một quiz");

        MizukiReply lookup = service.createResponse("Tra từ 日本");
        MizukiReply next = service.createResponse("nihon");

        assertTrue(lookup.message().contains("Nhật Bản"));
        assertFalse(next.sourceLabel().contains("Mini Quiz"));
    }

    @Test
    void vocabularyLookup_shouldNotBeMistakenForGreeting() {
        MizukiReply reply = service.createResponse("Tra từ xin chào");

        assertTrue(reply.message().contains("konnichiwa"));
    }

    @Test
    void quizAnswer_shouldAcceptTrailingPunctuation() {
        service.createResponse("Tra từ 日本");
        service.createResponse("Đố mình từ này");

        MizukiReply reply = service.createResponse("nihon?");

        assertTrue(reply.message().contains("Chính xác"));
    }

    @Test
    void usageQuestion_shouldReturnCompleteMizukiGuide() {
        MizukiReply reply = service.createResponse(
                "Mizuki ơi, hướng dẫn mình cách sử dụng chatbot với"
        );

        assertTrue(reply.message().contains("Enter để gửi"));
        assertTrue(reply.message().contains("Giữ và kéo avatar Mizuki"));
        assertTrue(reply.message().contains("Tạo quiz nhanh"));
        assertTrue(reply.message().contains("hoạt động offline"));
        assertTrue(reply.sourceLabel().contains("Mizuki"));
        assertTrue(reply.suggestions().contains("Tra chữ あ"));
    }

    @Test
    void greeting_shouldIntroduceMizuki() {
        MizukiReply reply = service.createResponse("Xin chào");

        assertTrue(reply.message().contains("Mình là Mizuki"));
        assertTrue(reply.suggestions().contains("Cách dùng Mizuki"));
    }
}
