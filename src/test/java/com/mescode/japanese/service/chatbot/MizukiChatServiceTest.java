package com.mescode.japanese.service.chatbot;

import com.mescode.japanese.model.kana.Kana;
import com.mescode.japanese.model.kana.KanaType;
import com.mescode.japanese.model.vocab.Vocabulary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mizuki tra cứu và điều khiển ngoại tuyến")
class MizukiChatServiceTest {
    private final MizukiChatService service = new MizukiChatService(
            List.of(new Kana("き", "ki", KanaType.gojuuon), new Kana("きゃ", "kya", KanaType.youon)),
            List.of(new Kana("キ", "ki", KanaType.gojuuon)),
            () -> List.of(new Vocabulary("にほん", "日本", "Nhật Bản", "nihon", "", 1)),
            List.of(), () -> 0, () -> 0);

    @AfterEach
    void closeService() { service.close(); }

    @Test
    @DisplayName("Tra kana ghép ưu tiên chuỗi dài nhất")
    void looksUpCombinedKana() {
        var reply = service.createResponse("きゃ đọc thế nào?");
        assertTrue(reply.message().contains("きゃ"));
        assertTrue(reply.message().contains("kya"));
    }

    @Test
    @DisplayName("Tra từ dùng dữ liệu học và mini quiz chấp nhận dấu câu cuối")
    void looksUpVocabularyAndGradesQuiz() {
        assertTrue(service.createResponse("Tra từ 日本").message().contains("Nhật Bản"));
        service.createResponse("Đố mình từ này");
        assertTrue(service.createResponse("nihon?").message().contains("Chính xác"));
    }

    @Test
    @DisplayName("Dừng quiz và đặt lại phiên xóa trạng thái câu hỏi cũ")
    void clearsQuizState() {
        service.createResponse("Cho mình một quiz");
        assertTrue(service.createResponse("Dừng quiz").message().contains("Đã dừng"));
        service.createResponse("Cho mình một quiz");
        service.resetSession();
        assertFalse(service.createResponse("nihon").sourceLabel().contains("Mini Quiz"));
    }

    @Test
    @DisplayName("Nhận lệnh nhạc không dấu, thời điểm tua và giới hạn âm lượng")
    void parsesMusicCommands() {
        assertEquals(MizukiAction.Type.MUSIC_PAUSE, service.createResponse("tam dung nhac").action().type());
        var seek = service.createResponse("tua nhạc tới 1:25").action();
        assertEquals(MizukiAction.Type.MUSIC_SEEK_ABSOLUTE_SECONDS, seek.type());
        assertEquals(85, seek.value());
        var volume = service.createResponse("âm lượng 150").action();
        assertEquals(MizukiAction.Type.MUSIC_SET_VOLUME_PERCENT, volume.type());
        assertEquals(100, volume.value());
    }
}
