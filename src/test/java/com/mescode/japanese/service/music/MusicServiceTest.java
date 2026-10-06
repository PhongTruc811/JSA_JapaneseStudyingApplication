package com.mescode.japanese.service.music;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Playlist và trạng thái nhạc không cần thiết bị âm thanh")
class MusicServiceTest {
    @Test
    @DisplayName("Playlist giữ WAV/MP3 hợp lệ và bỏ mục trùng, thiếu hoặc sai định dạng")
    void validatesPlaylistEntries() {
        var json = """
                {"tracks":[
                  {"id":"one","title":"Một","resource":"music/one.wav"},
                  {"id":"two","title":"Hai","resource":"/music/two.mp3"},
                  {"id":"ONE","title":"Trùng","resource":"/duplicate.wav"},
                  {"id":"bad","title":"Sai","resource":"/bad.flac"},
                  {"id":"missing","title":"Thiếu","resource":"/missing.mp3"}
                ]}
                """;
        var result = PlaylistLoader.load(stream(json), resource -> !resource.contains("missing"));
        assertEquals(List.of("one", "two"), result.tracks().stream().map(MusicTrack::id).toList());
        assertEquals("/music/one.wav", result.tracks().getFirst().resource());
        assertEquals(3, result.warnings().size());
    }

    @Test
    @DisplayName("Playlist hỏng báo cảnh báo và trả danh sách rỗng")
    void handlesMalformedPlaylist() {
        var result = PlaylistLoader.load(stream("{broken"), resource -> true);
        assertTrue(result.tracks().isEmpty());
        assertFalse(result.warnings().isEmpty());
    }

    @Test
    @DisplayName("Thay đổi trạng thái nhạc không tự phát âm thanh")
    void changesStateWithoutStartingPlayback() {
        var player = new JavaSoundMusicPlayer(List.of(
                new MusicTrack("one", "Một", "Mizuki", "/missing-one.wav"),
                new MusicTrack("two", "Hai", "Mizuki", "/missing-two.mp3")));
        try {
            assertEquals(PlaybackStatus.STOPPED, player.snapshot().status());
            player.setVolume(0.6f);
            player.setMuted(true);
            player.setRepeatMode(RepeatMode.ONE);
            player.next();
            var state = player.snapshot();
            assertEquals("two", state.track().id());
            assertEquals(0.6f, state.volume(), 0.001f);
            assertTrue(state.muted());
            assertEquals(RepeatMode.ONE, state.repeatMode());
            assertFalse(state.playing());
        } finally {
            player.close();
        }
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
