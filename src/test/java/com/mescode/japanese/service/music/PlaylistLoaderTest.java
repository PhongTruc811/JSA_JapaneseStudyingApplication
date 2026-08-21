package com.mescode.japanese.service.music;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaylistLoaderTest {

    @Test
    void load_shouldKeepValidWavAndMp3Entries() {
        PlaylistLoadResult result = load("""
                {
                  "tracks": [
                    {
                      "id": "study-one",
                      "title": "Study One",
                      "artist": "Artist",
                      "resource": "/music/tracks/one.wav"
                    },
                    {
                      "id": "study-two",
                      "title": "Study Two",
                      "artist": "Artist",
                      "resource": "music/tracks/two.mp3"
                    }
                  ]
                }
                """, resource -> true);

        assertEquals(2, result.tracks().size());
        assertEquals("/music/tracks/two.mp3", result.tracks().get(1).resource());
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void load_shouldSkipDuplicateUnsupportedAndMissingEntries() {
        PlaylistLoadResult result = load("""
                {
                  "tracks": [
                    {"id":"same","title":"One","resource":"/one.wav"},
                    {"id":"SAME","title":"Duplicate","resource":"/two.wav"},
                    {"id":"flac","title":"Unsupported","resource":"/three.flac"},
                    {"id":"missing","title":"Missing","resource":"/missing.mp3"}
                  ]
                }
                """, resource -> !resource.contains("missing"));

        assertEquals(1, result.tracks().size());
        assertEquals(3, result.warnings().size());
    }

    @Test
    void load_shouldReturnWarningForMalformedJson() {
        PlaylistLoadResult result = load("{not-json", resource -> true);

        assertTrue(result.tracks().isEmpty());
        assertFalse(result.warnings().isEmpty());
    }

    private PlaylistLoadResult load(String json, java.util.function.Predicate<String> exists) {
        return PlaylistLoader.load(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                exists
        );
    }
}
