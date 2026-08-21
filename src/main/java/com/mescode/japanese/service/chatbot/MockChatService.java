package com.mescode.japanese.service.chatbot;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Deterministic chatbot used while the real AI integration is not configured.
 */
public class MockChatService implements ChatService {
    private static final long RESPONSE_DELAY_MS = 700;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "jsa-mock-chat");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public CompletableFuture<MizukiReply> reply(String message) {
        String normalizedMessage = message == null ? "" : message.trim();
        if (normalizedMessage.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Message must not be blank")
            );
        }

        CompletableFuture<MizukiReply> response = new CompletableFuture<>();
        try {
            scheduler.schedule(
                    () -> response.complete(createResponse(normalizedMessage)),
                    RESPONSE_DELAY_MS,
                    TimeUnit.MILLISECONDS
            );
        } catch (RejectedExecutionException exception) {
            response.completeExceptionally(exception);
        }
        return response;
    }

    MizukiReply createResponse(String message) {
        String text = message.toLowerCase(Locale.ROOT);

        if (containsAny(text, "xin chào", "chào", "hello", "hi ")) {
            return reply(
                    "Xin chào! Mình là Mizuki, trợ lý học tiếng Nhật của bạn. "
                            + "Bạn có thể hỏi thử về Kana, Kanji, từ vựng, ngữ pháp hoặc cách làm quiz nhé.",
                    "Chế độ mô phỏng",
                    "Kana là gì?", "Cách học Kanji", "Quiz nhanh"
            );
        }
        if (containsAny(text, "hiragana", "katakana", "kana", "bảng chữ")) {
            return reply(
                    "Kana gồm Hiragana và Katakana. Hiragana thường dùng cho từ thuần Nhật và thành phần ngữ pháp, "
                            + "còn Katakana thường dùng cho từ mượn. Đây là phản hồi mô phỏng; sau này mình có thể lấy "
                            + "đúng ký tự và bài học từ dữ liệu của ứng dụng.",
                    "Kiến thức Kana",
                    "Tra chữ あ", "Quiz Kana", "Katakana là gì?"
            );
        }
        if (containsAny(text, "kanji", "hán tự")) {
            return reply(
                    "Khi học Kanji, bạn nên ghi nhớ theo nhóm: hình dạng, âm đọc, nghĩa và một từ ví dụ. "
                            + "Bản AI thật có thể giải thích trực tiếp các Kanji đang có trong module của JSA.",
                    "Gợi ý học tập",
                    "Tra Kanji「日」", "Cách nhớ Kanji", "Quiz nhanh"
            );
        }
        if (containsAny(text, "từ vựng", "vocabulary", "vocab", "từ mới")) {
            return reply(
                    "Một cách học từ vựng hiệu quả là kết hợp Kana, Romaji, nghĩa và đặt từ vào câu ngắn. "
                            + "Ở bản tiếp theo, chatbot có thể dùng danh sách từ vựng hiện có của ứng dụng để tạo ví dụ.",
                    "Gợi ý học tập",
                    "Tra từ ありがとう", "Quiz từ vựng", "Xem tiến độ"
            );
        }
        if (containsAny(text, "ngữ pháp", "grammar", "cấu trúc")) {
            return reply(
                    "Bạn hãy gửi tên cấu trúc và một câu ví dụ. Hiện tại mình chỉ đang mô phỏng phản hồi; "
                            + "khi nối AI thật, mình sẽ giải thích cách dùng, sắc thái và lỗi thường gặp.",
                    "Chế độ mô phỏng",
                    "Kana là gì?", "Ôn từ vựng", "Quiz nhanh"
            );
        }
        if (containsAny(text, "quiz", "kiểm tra", "ôn tập", "đáp án")) {
            return reply(
                    "Bạn có thể chọn module Kana, Vocabulary, Grammar hoặc PE Trial từ menu chính để luyện tập. "
                            + "Sau này chatbot có thể giải thích đáp án sai ngay sau mỗi câu.",
                    "JSA Learning Hub",
                    "Quiz Kana", "Quiz từ vựng", "Xem tiến độ"
            );
        }
        if (containsAny(text, "cảm ơn", "thank")) {
            return reply(
                    "Không có gì! Chúc bạn học tiếng Nhật vui vẻ và tiến bộ mỗi ngày.",
                    "Mizuki",
                    "Quiz nhanh", "Tra từ mới", "Xem tiến độ"
            );
        }

        return reply(
                "Mình đã nhận được câu hỏi: “" + shorten(message, 90) + "”. "
                        + "Đây là phản hồi mô phỏng nên chưa phân tích nội dung bằng AI thật. "
                        + "Bạn hãy thử hỏi về Kana, Kanji, từ vựng, ngữ pháp hoặc quiz nhé.",
                "Chế độ mô phỏng",
                    "Kana là gì?", "Tra Kanji「日」", "Quiz nhanh"
        );
    }

    private MizukiReply reply(String message, String source, String... suggestions) {
        return new MizukiReply(message, source, List.of(suggestions));
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1).trim() + "…";
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
