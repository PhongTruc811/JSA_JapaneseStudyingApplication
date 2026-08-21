package com.mescode.japanese.service.chatbot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MizukiMusicCommandTest {
    private MizukiChatService service;

    @BeforeEach
    void setUp() {
        service = new MizukiChatService(
                List.of(),
                List.of(),
                List::of,
                List.of(),
                () -> 0,
                () -> 0
        );
    }

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void shouldRecognizeCorePlaybackCommandsWithOrWithoutAccents() {
        assertAction("phát nhạc", MizukiAction.Type.MUSIC_PLAY, 0);
        assertAction("tam dung nhac", MizukiAction.Type.MUSIC_PAUSE, 0);
        assertAction("bài tiếp theo", MizukiAction.Type.MUSIC_NEXT, 0);
        assertAction("quay lại bài trước", MizukiAction.Type.MUSIC_PREVIOUS, 0);
    }

    @Test
    void shouldParseSeekAndVolumeValues() {
        assertAction("tua nhạc tới 1:25", MizukiAction.Type.MUSIC_SEEK_ABSOLUTE_SECONDS, 85);
        assertAction("âm lượng 150", MizukiAction.Type.MUSIC_SET_VOLUME_PERCENT, 100);
        assertAction("giảm âm lượng", MizukiAction.Type.MUSIC_CHANGE_VOLUME_PERCENT, -10);
    }

    @Test
    void shouldRecognizeShuffleAndRepeatModes() {
        assertAction("bật shuffle", MizukiAction.Type.MUSIC_SHUFFLE_ON, 0);
        assertAction("tắt ngẫu nhiên", MizukiAction.Type.MUSIC_SHUFFLE_OFF, 0);
        assertAction("lặp bài này", MizukiAction.Type.MUSIC_REPEAT_ONE, 0);
        assertAction("lặp playlist", MizukiAction.Type.MUSIC_REPEAT_ALL, 0);
        assertAction("tắt lặp", MizukiAction.Type.MUSIC_REPEAT_OFF, 0);
    }

    private void assertAction(String message, MizukiAction.Type type, int value) {
        MizukiReply reply = service.createResponse(message);
        assertEquals(type, reply.action().type(), message);
        assertEquals(value, reply.action().value(), message);
    }
}
