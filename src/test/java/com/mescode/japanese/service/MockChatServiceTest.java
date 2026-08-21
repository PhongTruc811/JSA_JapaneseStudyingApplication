package com.mescode.japanese.service.chatbot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockChatServiceTest {
    private final MockChatService service = new MockChatService();

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void createResponse_shouldRecognizeKanaTopic() {
        MizukiReply response = service.createResponse("Hiragana và Katakana khác nhau thế nào?");

        assertTrue(response.message().contains("Kana"));
        assertTrue(response.message().contains("Hiragana"));
        assertTrue(response.message().contains("Katakana"));
    }

    @Test
    void createResponse_shouldUseFallbackForUnknownTopic() {
        MizukiReply response = service.createResponse("Hãy giải thích câu này cho mình");

        assertTrue(response.message().contains("phản hồi mô phỏng"));
        assertTrue(response.message().contains("Hãy giải thích câu này cho mình"));
    }

    @Test
    void reply_shouldRejectBlankMessage() {
        assertThrows(
                CompletionException.class,
                () -> service.reply("   ").join()
        );
    }
}
