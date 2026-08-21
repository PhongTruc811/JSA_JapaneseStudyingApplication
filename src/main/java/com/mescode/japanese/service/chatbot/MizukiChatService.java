package com.mescode.japanese.service.chatbot;

import com.mescode.japanese.model.Kanji;
import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.vocab.Vocabulary;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Offline tutor that answers from the learning data already bundled with JSA.
 * It intentionally has no network or API-key dependency.
 */
public class MizukiChatService implements ChatService {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern CLOCK_POSITION = Pattern.compile("(\\d{1,2})\\s*:\\s*(\\d{1,2})");
    private static final Pattern NUMBER = Pattern.compile("(\\d{1,3})");

    private final List<KanaEntry> kanaEntries;
    private final Supplier<List<Vocabulary>> vocabularySupplier;
    private final List<Kanji> kanjis;
    private final IntSupplier learnedCountSupplier;
    private final IntSupplier favoriteCountSupplier;
    private final ScheduledExecutorService scheduler;
    private final Random random = new Random();
    private final Map<CompletableFuture<MizukiReply>, ScheduledFuture<?>> pendingRequests = new HashMap<>();

    private PendingQuiz pendingQuiz;
    private Vocabulary lastVocabulary;
    private List<Vocabulary> vocabularies = List.of();
    private int turnCount;
    private long sessionGeneration;
    private boolean closed;

    public MizukiChatService(List<Kana> hiragana,
                             List<Kana> katakana,
                             Supplier<List<Vocabulary>> vocabularySupplier,
                             List<Kanji> kanjis,
                             IntSupplier learnedCountSupplier,
                             IntSupplier favoriteCountSupplier) {
        this.kanaEntries = new ArrayList<>();
        addKana(hiragana, "Hiragana");
        addKana(katakana, "Katakana");
        this.vocabularySupplier = vocabularySupplier == null ? List::of : vocabularySupplier;
        refreshVocabularySnapshot();
        this.kanjis = safeCopy(kanjis);
        this.learnedCountSupplier = learnedCountSupplier == null ? () -> 0 : learnedCountSupplier;
        this.favoriteCountSupplier = favoriteCountSupplier == null ? () -> 0 : favoriteCountSupplier;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "mizuki-local-tutor");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public CompletableFuture<MizukiReply> reply(String message) {
        String cleanMessage = message == null ? "" : message.trim();
        if (cleanMessage.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Message must not be blank")
            );
        }

        CompletableFuture<MizukiReply> response = new CompletableFuture<>();
        long delay = Math.min(950, 420L + cleanMessage.length() * 9L);
        long requestGeneration;
        synchronized (this) {
            if (closed) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("Local tutor is already closed")
                );
            }
            requestGeneration = sessionGeneration;
            try {
                ScheduledFuture<?> task = scheduler.schedule(
                        () -> completeScheduledResponse(response, cleanMessage, requestGeneration),
                        delay,
                        TimeUnit.MILLISECONDS
                );
                pendingRequests.put(response, task);
            } catch (RejectedExecutionException exception) {
                response.completeExceptionally(exception);
            }
        }
        response.whenComplete((reply, error) -> cancelScheduledTaskIfNeeded(response));
        return response;
    }

    private void completeScheduledResponse(CompletableFuture<MizukiReply> response,
                                           String message,
                                           long requestGeneration) {
        synchronized (this) {
            pendingRequests.remove(response);
            if (closed || requestGeneration != sessionGeneration || response.isDone()) {
                response.cancel(false);
                return;
            }
            try {
                response.complete(createResponse(message));
            } catch (Throwable error) {
                response.completeExceptionally(error);
            }
        }
    }

    private void cancelScheduledTaskIfNeeded(CompletableFuture<MizukiReply> response) {
        synchronized (this) {
            ScheduledFuture<?> task = pendingRequests.remove(response);
            if (response.isCancelled() && task != null) {
                task.cancel(false);
            }
        }
    }

    synchronized MizukiReply createResponse(String message) {
        refreshVocabularySnapshot();
        turnCount++;
        String normalized = normalize(message);
        String command = normalizeCommand(normalized);

        MizukiReply musicReply = createMusicReply(normalized);
        if (musicReply != null) {
            return musicReply;
        }

        if (pendingQuiz != null && equalsAny(
                command, "bo qua", "skip", "dung", "dung quiz", "thoat", "thoat quiz"
        )) {
            pendingQuiz = null;
            return new MizukiReply(
                    "Đã dừng câu quiz hiện tại. Bạn muốn tra cứu hay luyện nội dung nào tiếp theo?",
                    "Mizuki",
                    List.of("Tra Kana", "Tra từ vựng", "Xem tiến độ")
            );
        }

        if (pendingQuiz != null) {
            if (!looksLikeNewQuestion(normalized)) {
                return evaluateQuizAnswer(message);
            }
            pendingQuiz = null;
        }

        // Đoạn reply của Mizuki  đầu tiên khi gui tin nhắn chào hỏi (hi, chao ban,
        if (isGreeting(command)) {
            return new MizukiReply(
                    "こんにちは! Mình là Mizuki — người bạn học tiếng Nhật của bạn. "
                            + "Mình có thể tra trực tiếp "
                            + kanaEntries.size() + " Kana, " + vocabularies.size() + " từ vựng và "
                            + kanjis.size() + " Kanji đang có trong ứng dụng, hoàn toàn offline. "
                            + "Nếu chưa biết bắt đầu từ đâu, hãy hỏi “Cách dùng Mizuki” nhé.",
                    "JSA Knowledge • Offline",
                    List.of("Cách dùng Mizuki", "Tra từ ありがとう", "Tạo quiz nhanh")
            );
        }

        if (containsAny(normalized, "tien do", "da hoc", "yeu thich", "thong ke")) {
            return progressReply();
        }

        if (containsAny(normalized, "quiz", "do minh", "do lai", "kiem tra nhanh", "cau hoi nhanh")) {
            return createVocabularyQuiz(normalized.contains("tu nay"));
        }
        if (isCapabilityQuestion(command)) {
            return capabilityReply();
        }
        if (equalsAny(command, "cam on", "cam on ban", "thanks", "thank you")) {
            return new MizukiReply(
                    "どういたしまして! Không có gì. Mình luôn sẵn sàng đồng hành cùng bạn trong JSA.",
                    "mizuki san",
                    List.of("Cho mình một quiz", "Tra từ mới", "Xem tiến độ")
            );
        }

        boolean asksKanji = containsAny(normalized, "kanji", "han tu", "han viet");
        boolean asksVocabulary = containsAny(
                normalized, "tu vung", "vocabulary", "vocab", "tra tu", "tu nay", "nghia cua tu"
        );
        boolean asksKana = containsAny(normalized, "kana", "hiragana", "katakana", "bang chu", "chu nay");

        if (asksKanji) {
            if (containsAny(normalized, "cach hoc kanji", "hoc kanji the nao", "nho kanji")) {
                return kanjiStudyReply();
            }
            Kanji kanji = findKanji(message, normalized);
            if (kanji != null) {
                return kanjiReply(kanji);
            }
            return new MizukiReply(
                    "Kho Kanji JSA hiện có " + kanjis.size() + " chữ. "
                            + "Hãy gửi một Kanji, âm Hán Việt hoặc cách đọc Hiragana để mình tra cứu.",
                    "Dữ liệu Kanji JSA",
                    List.of("Tra Kanji「日」", "Cách học Kanji", "Cho mình một quiz")
            );
        }
        if (asksVocabulary) {
            if (normalized.contains("tu nay") && lastVocabulary != null) {
                return vocabularyReply(lastVocabulary);
            }
            Vocabulary vocabulary = findVocabulary(message, normalized);
            if (vocabulary != null) {
                return vocabularyReply(vocabulary);
            }
            return new MizukiReply(
                    "Kho từ vựng JSA hiện có " + vocabularies.size() + " mục. "
                            + "Bạn có thể gửi Kana, Kanji, Romaji hoặc nghĩa tiếng Việt để mình tra.",
                    "Dữ liệu Vocabulary JSA",
                    List.of("Tra từ ありがとう", "Cho mình một quiz", "Xem tiến độ")
            );
        }
        if (asksKana) {
            KanaEntry kana = findKana(message, normalized);
            if (kana != null) {
                return kanaReply(kana);
            }
            return new MizukiReply(
                    "Kana là hệ chữ âm tiết của tiếng Nhật, gồm Hiragana và Katakana. "
                            + "Dữ liệu JSA hiện có " + kanaEntries.size() + " ký tự để bạn tra cứu và luyện tập.",
                    "Dữ liệu Kana JSA",
                    List.of("Tra chữ あ", "Hiragana khác Katakana?", "Cho mình một quiz")
            );
        }

        if (countCjk(message) == 1) {
            Kanji directKanji = findKanji(message, normalized);
            if (directKanji != null) {
                return kanjiReply(directKanji);
            }
        }
        Vocabulary vocabulary = findVocabulary(message, normalized);
        if (vocabulary != null) {
            return vocabularyReply(vocabulary);
        }
        KanaEntry kana = findKanaByJapaneseCharacter(message);
        if (kana != null) {
            return kanaReply(kana);
        }
        Kanji fallbackKanji = findKanji(message, normalized);
        if (fallbackKanji != null) {
            return kanjiReply(fallbackKanji);
        }

        return new MizukiReply(
                "Mình chưa tìm thấy nội dung khớp chính xác trong dữ liệu local. "
                        + "Hãy thử gửi một Kana, Kanji, từ vựng cụ thể hoặc yêu cầu mình tạo quiz nhanh.",
                "JSA Knowledge • Không tìm thấy",
                List.of("Cách dùng Mizuki", "Tra chữ あ", "Tạo quiz nhanh")
        );
    }

    //  Reply của mizuki khi hỏi về cách sử dụng
    private MizukiReply capabilityReply() {
        return new MizukiReply(
                "Rất vui được hướng dẫn bạn dùng Mizuki ✨\n\n"
                        + "Bạn có thể nhập câu hỏi trực tiếp hoặc chạm vào các gợi ý bên dưới:\n"
                        + "• Kana: “Tra chữ あ” hoặc “きゃ đọc thế nào?”\n"
                        + "• Từ vựng: tra bằng Kana, Kanji, Romaji hay nghĩa tiếng Việt, "
                        + "ví dụ “Tra từ ありがとう”\n"
                        + "• Kanji: gửi chữ, âm Hán Việt hoặc cách đọc, "
                        + "ví dụ “Tra Kanji 日”\n"
                        + "• Luyện tập: nói “Tạo quiz nhanh”, trả lời ngay trong ô chat; "
                        + "gõ “Bỏ qua” hoặc “Dừng quiz” để kết thúc\n"
                        + "• Tiến độ: hỏi “Xem tiến độ” để xem số từ đã học và đã yêu thích\n\n"
                        + "• Âm nhạc: nói “Phát nhạc”, “Bài tiếp theo”, “Âm lượng 35%”, "
                        + "hoặc mở nút ♫ để dùng trình phát đầy đủ\n\n"
                        + "Mẹo sử dụng giao diện:\n"
                        + "• Enter để gửi, Shift+Enter để xuống dòng\n"
                        + "• Nút “Mới” bắt đầu phiên trò chuyện mới; phím Esc hoặc nút × để thu nhỏ\n"
                        + "• Giữ và kéo avatar Mizuki để đặt em ấy ở vị trí bạn muốn\n\n"
                        + "Mizuki chỉ đọc dữ liệu học có sẵn trong JSA và hoạt động offline, "
                        + "nên nội dung trò chuyện được giữ riêng trên thiết bị.",
                "Mizuki • Hướng dẫn sử dụng • " + turnCount + " lượt trong phiên",
                List.of("Phát nhạc", "Tra chữ あ", "Tra từ ありがとう", "Tạo quiz nhanh")
        );
    }

    private MizukiReply kanjiStudyReply() {
        return new MizukiReply(
                "Cách học Kanji hiệu quả theo dữ liệu JSA:\n"
                        + "• Nhìn chữ và gắn với âm Hán Việt\n"
                        + "• Đọc Hiragana thành tiếng\n"
                        + "• Đặt chữ vào một từ vựng cụ thể\n"
                        + "• Ôn lại theo nhịp 1 ngày, 3 ngày và 7 ngày\n"
                        + "Bạn có thể gửi một Kanji ngay bây giờ để mình tách đủ bốn lớp thông tin.",
                "Mizuki • Lộ trình Kanji",
                List.of("Tra Kanji「日」", "Cho mình một quiz", "Xem tiến độ")
        );
    }

    private MizukiReply progressReply() {
        int learned = Math.max(0, learnedCountSupplier.getAsInt());
        int favorites = Math.max(0, favoriteCountSupplier.getAsInt());
        int total = vocabularies.size();
        int percentage = total == 0 ? 0 : Math.min(100, Math.round(learned * 100f / total));
        return new MizukiReply(
                "Tiến độ từ vựng của bạn\n"
                        + "• Đã học: " + learned + "/" + total + " từ (" + percentage + "%)\n"
                        + "• Đã yêu thích: " + favorites + " từ\n"
                        + progressEncouragement(percentage),
                "Tiến độ cá nhân • Local",
                List.of("Cho mình một quiz", "Ôn từ vựng", "Tra từ mới")
        );
    }

    private String progressEncouragement(int percentage) {
        if (percentage >= 80) {
            return "Bạn đang ở chặng nước rút—hãy dùng quiz để củng cố các từ còn lại.";
        }
        if (percentage >= 40) {
            return "Nhịp học rất tốt. Một mini quiz lúc này sẽ giúp khóa kiến thức lâu hơn.";
        }
        return "Hãy bắt đầu đều đặn với vài từ mỗi ngày; tiến bộ nhỏ vẫn là tiến bộ.";
    }

    private MizukiReply kanaReply(KanaEntry entry) {
        String group = entry.kana().getType() == null
                ? "Chưa phân nhóm"
                : friendlyKanaGroup(entry.kana().getType().name());
        String related = kanaEntries.stream()
                .filter(value -> value != entry
                        && normalize(value.kana().getRomaji()).equals(normalize(entry.kana().getRomaji())))
                .map(value -> safe(value.kana().getKana()) + " (" + value.script() + ")")
                .distinct()
                .limit(3)
                .reduce((left, right) -> left + ", " + right)
                .map(value -> "\n• Cùng Romaji: " + value)
                .orElse("");
        return new MizukiReply(
                "「" + safe(entry.kana().getKana()) + "」 đọc là “" + safe(entry.kana().getRomaji()) + "”.\n"
                        + "• Bảng chữ: " + entry.script() + "\n"
                        + "• Nhóm âm: " + group + related + "\n"
                        + "Mẹo: đọc thành tiếng rồi viết lại ký tự này 3 lần để ghi nhớ cả âm và hình.",
                "Dữ liệu Kana JSA • " + entry.script(),
                List.of("Cho mình một quiz", "Tra chữ khác", entry.script().equals("Hiragana")
                        ? "Katakana là gì?" : "Hiragana là gì?")
        );
    }

    private MizukiReply vocabularyReply(Vocabulary vocabulary) {
        lastVocabulary = vocabulary;
        List<Vocabulary> alternatives = vocabularies.stream()
                .filter(value -> value != null
                        && hasText(vocabulary.getKana())
                        && normalize(value.getKana()).equals(normalize(vocabulary.getKana())))
                .limit(4)
                .toList();
        if (alternatives.size() > 1) {
            StringBuilder choices = new StringBuilder("Mình tìm thấy ")
                    .append(alternatives.size())
                    .append(" cách viết/nghĩa cho 「")
                    .append(safe(vocabulary.getKana()))
                    .append("」:\n");
            for (int index = 0; index < alternatives.size(); index++) {
                Vocabulary option = alternatives.get(index);
                choices.append(index + 1)
                        .append(". ")
                        .append(hasText(option.getKanji()) ? option.getKanji() : option.getKana())
                        .append(" • ")
                        .append(safe(option.getMeaning()))
                        .append(" • ")
                        .append(safe(option.getRomaji()));
                if (index < alternatives.size() - 1) {
                    choices.append("\n");
                }
            }
            return new MizukiReply(
                    choices.toString(),
                    "Dữ liệu Vocabulary JSA • " + alternatives.size() + " kết quả",
                    List.of("Đố mình từ này", "Tra từ khác", "Xem tiến độ")
            );
        }

        String display = hasText(vocabulary.getKanji())
                ? "「" + vocabulary.getKanji() + "」（" + safe(vocabulary.getKana()) + "）"
                : "「" + safe(vocabulary.getKana()) + "」";
        StringBuilder text = new StringBuilder(display)
                .append("\n• Romaji: ").append(safe(vocabulary.getRomaji()))
                .append("\n• Nghĩa: ").append(safe(vocabulary.getMeaning()));
        if (hasText(vocabulary.getExample())) {
            text.append("\n• Ví dụ: ").append(vocabulary.getExample().trim());
        }
        if (vocabulary.getLesson() != null) {
            text.append("\n• Bài học: ").append(vocabulary.getLesson());
        }
        return new MizukiReply(
                text.toString(),
                vocabulary.getLesson() == null
                        ? "Dữ liệu Vocabulary JSA"
                        : "Dữ liệu Vocabulary JSA • Bài " + vocabulary.getLesson(),
                List.of("Đố mình từ này", "Tra từ khác", "Xem tiến độ")
        );
    }

    private MizukiReply kanjiReply(Kanji kanji) {
        List<Kanji> alternatives = kanjis.stream()
                .filter(value -> value != null
                        && hasText(kanji.getKanji())
                        && kanji.getKanji().equals(value.getKanji()))
                .limit(4)
                .toList();
        if (alternatives.size() > 1) {
            StringBuilder choices = new StringBuilder("Kanji 「")
                    .append(kanji.getKanji())
                    .append("」 xuất hiện ở nhiều Unit:\n");
            for (int index = 0; index < alternatives.size(); index++) {
                Kanji option = alternatives.get(index);
                choices.append("• Unit ")
                        .append(option.getUnit())
                        .append(": ")
                        .append(safe(option.getHanViet()))
                        .append(" • ")
                        .append(safe(option.getHiragana()))
                        .append(" • ")
                        .append(safe(option.getMeaning()));
                if (index < alternatives.size() - 1) {
                    choices.append("\n");
                }
            }
            return new MizukiReply(
                    choices.toString(),
                    "Dữ liệu Kanji JSA • " + alternatives.size() + " kết quả",
                    List.of("Tra Kanji khác", "Cho mình một quiz", "Cách học Kanji")
            );
        }
        return new MizukiReply(
                "Kanji 「" + safe(kanji.getKanji()) + "」\n"
                        + "• Hán Việt: " + safe(kanji.getHanViet()) + "\n"
                        + "• Cách đọc: " + safe(kanji.getHiragana()) + "\n"
                        + "• Nghĩa: " + safe(kanji.getMeaning()) + "\n"
                        + "• Unit: " + kanji.getUnit(),
                "Dữ liệu Kanji JSA • Unit " + kanji.getUnit(),
                List.of("Tra Kanji khác", "Cho mình một quiz", "Cách học Kanji")
        );
    }

    private MizukiReply createVocabularyQuiz(boolean useLastVocabulary) {
        if (vocabularies.isEmpty()) {
            return new MizukiReply(
                    "Chưa có dữ liệu từ vựng để tạo quiz.",
                    "JSA Mini Quiz",
                    List.of("Tra Kana", "Tra Kanji", "Cách dùng Mizuki")
            );
        }

        Vocabulary vocabulary = useLastVocabulary && lastVocabulary != null
                ? lastVocabulary
                : randomVocabulary();
        if (vocabulary == null) {
            return new MizukiReply(
                    "Kho từ hiện tại chưa có mục đủ Kana và Romaji để tạo quiz.",
                    "JSA Mini Quiz",
                    List.of("Tra từ vựng", "Tra Kana", "Xem tiến độ")
            );
        }
        lastVocabulary = vocabulary;
        String expected = normalizeQuizAnswer(vocabulary.getRomaji());
        if (expected.isBlank()) {
            return new MizukiReply(
                    "Từ vừa chọn chưa có Romaji. Hãy thử một câu khác nhé.",
                    "JSA Mini Quiz",
                    List.of("Quiz khác", "Tra từ vựng", "Xem tiến độ")
            );
        }
        pendingQuiz = new PendingQuiz(vocabulary, expected);
        String display = hasText(vocabulary.getKanji())
                ? vocabulary.getKanji() + "（" + safe(vocabulary.getKana()) + "）"
                : safe(vocabulary.getKana());
        return new MizukiReply(
                "MINI QUIZ\nĐọc 「" + display + "」 bằng Romaji là gì?\n"
                        + "Nhập đáp án ngay bên dưới—mình sẽ chấm tức thì.",
                "JSA Mini Quiz • 1 câu",
                List.of("Bỏ qua", "Gợi ý", "Dừng quiz")
        );
    }

    private MizukiReply evaluateQuizAnswer(String message) {
        PendingQuiz quiz = pendingQuiz;
        String normalizedAnswer = normalize(message);
        String answer = normalizeQuizAnswer(message);
        if (containsAny(normalizedAnswer, "goi y", "hint")) {
            String expected = quiz.expected();
            String hint = expected.length() <= 1
                    ? expected
                    : expected.charAt(0) + "…" + expected.charAt(expected.length() - 1);
            return new MizukiReply(
                    "Gợi ý: đáp án Romaji có " + expected.length() + " ký tự và có dạng “" + hint + "”.",
                    "JSA Mini Quiz • Gợi ý",
                    List.of("Bỏ qua", "Quiz khác", "Dừng quiz")
            );
        }
        pendingQuiz = null;
        Vocabulary vocabulary = quiz.vocabulary();
        if (answer.equals(quiz.expected())) {
            return new MizukiReply(
                    "正解! Chính xác 🎉\n"
                            + "「" + safe(vocabulary.getKana()) + "」 = "
                            + safe(vocabulary.getRomaji()) + " = " + safe(vocabulary.getMeaning()),
                    "JSA Mini Quiz • Chính xác",
                    List.of("Quiz khác", "Tra từ này", "Xem tiến độ")
            );
        }
        return new MizukiReply(
                "Chưa đúng lần này.\nĐáp án là “" + safe(vocabulary.getRomaji()) + "”. "
                        + "「" + safe(vocabulary.getKana()) + "」 có nghĩa là “"
                        + safe(vocabulary.getMeaning()) + "”.",
                "JSA Mini Quiz • Cần ôn lại",
                List.of("Quiz khác", "Đố lại từ này", "Tra từ khác")
        );
    }

    private Vocabulary randomVocabulary() {
        List<Vocabulary> candidates = vocabularies.stream()
                .filter(value -> value != null && hasText(value.getKana()) && hasText(value.getRomaji()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private KanaEntry findKana(String rawMessage, String normalizedMessage) {
        KanaEntry direct = findKanaByJapaneseCharacter(rawMessage);
        if (direct != null) {
            return direct;
        }
        Set<String> tokens = tokens(normalizedMessage);
        for (KanaEntry entry : kanaEntries) {
            if (entry.kana() != null && tokens.contains(normalize(entry.kana().getRomaji()))) {
                return entry;
            }
        }
        return null;
    }

    private KanaEntry findKanaByJapaneseCharacter(String rawMessage) {
        KanaEntry bestMatch = null;
        int bestLength = -1;
        for (KanaEntry entry : kanaEntries) {
            if (entry.kana() != null && hasText(entry.kana().getKana())
                    && rawMessage.contains(entry.kana().getKana())) {
                int length = entry.kana().getKana().length();
                if (length > bestLength) {
                    bestMatch = entry;
                    bestLength = length;
                }
            }
        }
        return bestMatch;
    }

    private Vocabulary findVocabulary(String rawMessage, String normalizedMessage) {
        Set<String> tokens = tokens(normalizedMessage);
        Vocabulary bestMatch = null;
        int bestScore = -1;
        for (Vocabulary vocabulary : vocabularies) {
            if (vocabulary == null) {
                continue;
            }
            int score = Math.max(
                    japaneseMatchScore(rawMessage, vocabulary.getKanji()),
                    japaneseMatchScore(rawMessage, vocabulary.getKana())
            );
            score = Math.max(score,
                    searchTextScore(normalizedMessage, tokens, vocabulary.getRomaji(), 2));
            score = Math.max(score,
                    searchTextScore(normalizedMessage, tokens, vocabulary.getMeaning(), 3));
            if (score > bestScore) {
                bestMatch = vocabulary;
                bestScore = score;
            }
        }
        return bestScore > 0 ? bestMatch : null;
    }

    private Kanji findKanji(String rawMessage, String normalizedMessage) {
        Set<String> tokens = tokens(normalizedMessage);
        Kanji bestMatch = null;
        int bestScore = -1;
        for (Kanji kanji : kanjis) {
            if (kanji == null) {
                continue;
            }
            int score = Math.max(
                    japaneseMatchScore(rawMessage, kanji.getKanji()),
                    japaneseMatchScore(rawMessage, kanji.getHiragana())
            );
            score = Math.max(score,
                    searchTextScore(normalizedMessage, tokens, kanji.getHanViet(), 2));
            score = Math.max(score,
                    searchTextScore(normalizedMessage, tokens, kanji.getMeaning(), 3));
            if (score > bestScore) {
                bestMatch = kanji;
                bestScore = score;
            }
        }
        return bestScore > 0 ? bestMatch : null;
    }

    private int japaneseMatchScore(String message, String field) {
        if (!hasText(field) || !containsJapanese(field)) {
            return 0;
        }
        String cleanField = field.trim();
        if (!message.contains(cleanField)) {
            return 0;
        }
        String strippedMessage = message.replaceAll("[\\s「」『』\"'?!？。,.]", "");
        int exactBonus = strippedMessage.equals(cleanField) ? 10_000 : 0;
        return exactBonus + 5_000 + cleanField.length() * 100;
    }

    private int searchTextScore(String normalizedMessage,
                                Set<String> messageTokens,
                                String field,
                                int minimumLength) {
        String normalizedField = normalize(field);
        if (normalizedField.length() < minimumLength) {
            return 0;
        }
        if (normalizedMessage.equals(normalizedField)) {
            return 8_000 + normalizedField.length();
        }
        if (messageTokens.contains(normalizedField)) {
            return 1_500 + normalizedField.length();
        }
        if (normalizedField.contains(" ") && normalizedMessage.contains(normalizedField)) {
            return 900 + normalizedField.length();
        }
        return 0;
    }

    private boolean looksLikeNewQuestion(String normalizedMessage) {
        return startsWithAny(
                normalizedMessage,
                "tra ", "xem ", "cho minh ", "kana", "hiragana", "katakana",
                "kanji", "tu vung", "quiz", "do lai", "giai thich", "ban lam duoc gi", "tien do"
        );
    }

    private String friendlyKanaGroup(String type) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "gojuuon" -> "Gojuon (âm cơ bản)";
            case "dakuon" -> "Dakuon (âm đục)";
            case "handakuon" -> "Handakuon (âm bán đục)";
            case "youon" -> "Youon (âm ghép)";
            default -> type;
        };
    }

    private void addKana(List<Kana> source, String script) {
        if (source == null) {
            return;
        }
        for (Kana kana : source) {
            if (kana != null) {
                kanaEntries.add(new KanaEntry(kana, script));
            }
        }
    }

    private <T> List<T> safeCopy(List<T> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private void refreshVocabularySnapshot() {
        try {
            this.vocabularies = safeCopy(vocabularySupplier.get());
        } catch (RuntimeException ignored) {
            // Keep the last known-good snapshot if user data is being updated concurrently.
        }
    }

    private Set<String> tokens(String normalized) {
        Set<String> result = new HashSet<>();
        for (String token : TOKEN_SPLIT.split(normalized)) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }
        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed)
                .replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .trim();
    }

    private String normalizeQuizAnswer(String value) {
        return normalize(value).replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    private String normalizeCommand(String normalized) {
        return normalized
                .replaceAll("[\\p{P}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isGreeting(String command) {
        return equalsAny(
                command,
                "hi", "hey", "hello", "xin chao", "chao", "chao ban", "xin chao ban"
        );
    }

    private boolean isCapabilityQuestion(String command) {
        return equalsAny(
                command,
                "help", "how to use", "huong dan", "huong dan su dung",
                "cach dung", "cach su dung", "cach dung mizuki", "cach su dung mizuki",
                "ban lam duoc gi", "ban giup gi", "tro giup"
        ) || containsAny(
                command,
                "lam duoc gi", "co the giup gi", "dung mizuki", "su dung mizuki",
                "dung chatbot", "su dung chatbot", "dung chat bot", "su dung chat bot",
                "bat dau the nao"
        );
    }

    private MizukiReply createMusicReply(String command) {
        if (command.isBlank()) {
            return null;
        }
        MizukiAction action = null;
        String message = null;

        if (containsAny(command, "am luong", "volume")) {
            int percent = firstNumber(command, -1);
            if (percent >= 0) {
                percent = Math.max(0, Math.min(100, percent));
                action = new MizukiAction(
                        MizukiAction.Type.MUSIC_SET_VOLUME_PERCENT,
                        percent
                );
                message = "Mình sẽ đặt âm lượng nhạc ở mức " + percent + "% nhé ♪";
            } else if (containsAny(command, "tang", "to hon", "lon hon", "up")) {
                action = new MizukiAction(MizukiAction.Type.MUSIC_CHANGE_VOLUME_PERCENT, 10);
                message = "Mình tăng âm lượng nhạc lên một chút nhé ♪";
            } else if (containsAny(command, "giam", "nho hon", "down")) {
                action = new MizukiAction(MizukiAction.Type.MUSIC_CHANGE_VOLUME_PERCENT, -10);
                message = "Mình giảm âm lượng nhạc xuống một chút nhé ♪";
            }
        }

        if (action == null && containsAny(command, "bo tat tieng", "unmute", "bat tieng")) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_UNMUTE);
            message = "Đã bật lại tiếng cho playlist của Mizuki ♪";
        } else if (action == null && containsAny(command, "tat tieng", "mute")) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_MUTE);
            message = "Mình sẽ tắt tiếng nhạc, trạng thái bài vẫn được giữ nguyên.";
        }

        if (action == null && containsAny(command, "tua", "seek")) {
            int seconds = parseSeekSeconds(command);
            if (seconds >= 0) {
                action = new MizukiAction(
                        MizukiAction.Type.MUSIC_SEEK_ABSOLUTE_SECONDS,
                        seconds
                );
                message = "Mình sẽ tua nhạc tới " + formatClock(seconds) + " nhé ♪";
            }
        }

        if (action == null && containsAny(
                command, "bai tiep", "bai sau", "chuyen bai", "next song", "next track"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_NEXT);
            message = "Đổi sang bài tiếp theo thôi ♪";
        }
        if (action == null && containsAny(
                command, "bai truoc", "previous song", "previous track", "quay lai bai"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_PREVIOUS);
            message = "Mình quay lại bài trước nhé ♪";
        }

        if (action == null && containsAny(command, "shuffle", "ngau nhien")) {
            boolean disable = containsAny(command, "tat", "dung", "khong");
            action = MizukiAction.of(disable
                    ? MizukiAction.Type.MUSIC_SHUFFLE_OFF
                    : MizukiAction.Type.MUSIC_SHUFFLE_ON);
            message = disable
                    ? "Đã tắt phát ngẫu nhiên."
                    : "Đã bật phát ngẫu nhiên cho playlist ♪";
        }

        if (action == null && containsAny(command, "lap bai", "repeat one")) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_REPEAT_ONE);
            message = "Mizuki sẽ lặp lại bài hiện tại ♪";
        } else if (action == null && containsAny(
                command, "lap playlist", "lap danh sach", "repeat all"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_REPEAT_ALL);
            message = "Mizuki sẽ lặp lại toàn bộ playlist ♪";
        } else if (action == null && containsAny(
                command, "tat lap", "khong lap", "repeat off"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_REPEAT_OFF);
            message = "Đã tắt chế độ lặp.";
        }

        boolean mentionsMusic = containsAny(
                command, "nhac", "music", "playlist", "bai hat", "ca khuc"
        );
        if (action == null && mentionsMusic && containsAny(
                command, "tam dung", "dung nhac", "pause", "ngung"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_PAUSE);
            message = "Mình tạm dừng nhạc nhé. Khi muốn nghe tiếp, cứ bảo “phát nhạc” ♪";
        }
        if (action == null && mentionsMusic && containsAny(
                command, "phat", "bat", "mo", "tiep tuc", "play"
        )) {
            action = MizukiAction.of(MizukiAction.Type.MUSIC_PLAY);
            message = "Mizuki đã nhận lệnh phát playlist học tập rồi ♪";
        }

        if (action == null) {
            return null;
        }
        return new MizukiReply(
                message,
                "MIZUKI MUSIC • OFFLINE",
                List.of("Bài tiếp theo", "Âm lượng 35%", "Bật shuffle"),
                action
        );
    }

    private int parseSeekSeconds(String command) {
        java.util.regex.Matcher clock = CLOCK_POSITION.matcher(command);
        if (clock.find()) {
            int minutes = Integer.parseInt(clock.group(1));
            int seconds = Math.min(59, Integer.parseInt(clock.group(2)));
            return minutes * 60 + seconds;
        }
        int value = firstNumber(command, -1);
        if (value < 0) {
            return -1;
        }
        return containsAny(command, "phut", "minute") ? value * 60 : value;
    }

    private int firstNumber(String value, int fallback) {
        java.util.regex.Matcher matcher = NUMBER.matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private String formatClock(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    private boolean containsJapanese(String value) {
        for (int index = 0; index < value.length(); index++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(value.charAt(index));
            if (block == Character.UnicodeBlock.HIRAGANA
                    || block == Character.UnicodeBlock.KATAKANA
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    private int countCjk(String value) {
        int count = 0;
        for (int index = 0; index < value.length(); index++) {
            if (Character.UnicodeBlock.of(value.charAt(index))
                    == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAny(String text, String... fragments) {
        for (String fragment : fragments) {
            if (text.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private boolean startsWithAny(String text, String... fragments) {
        for (String fragment : fragments) {
            if (text.startsWith(fragment)) {
                return true;
            }
        }
        return false;
    }

    private boolean equalsAny(String text, String... values) {
        for (String value : values) {
            if (text.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return hasText(value) ? value.trim() : "Chưa có dữ liệu";
    }

    @Override
    public synchronized void resetSession() {
        sessionGeneration++;
        cancelPendingRequests();
        pendingQuiz = null;
        lastVocabulary = null;
        turnCount = 0;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            sessionGeneration++;
            cancelPendingRequests();
            pendingQuiz = null;
            lastVocabulary = null;
        }
        scheduler.shutdownNow();
    }

    private void cancelPendingRequests() {
        List<Map.Entry<CompletableFuture<MizukiReply>, ScheduledFuture<?>>> requests =
                new ArrayList<>(pendingRequests.entrySet());
        pendingRequests.clear();
        for (Map.Entry<CompletableFuture<MizukiReply>, ScheduledFuture<?>> request : requests) {
            request.getValue().cancel(false);
            request.getKey().cancel(false);
        }
    }

    private record KanaEntry(Kana kana, String script) {
    }

    private record PendingQuiz(Vocabulary vocabulary, String expected) {
    }
}
