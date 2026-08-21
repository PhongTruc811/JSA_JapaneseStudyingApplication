package com.mescode.japanese.service.music;

import java.util.List;

public record PlaylistLoadResult(List<MusicTrack> tracks, List<String> warnings) {
    public PlaylistLoadResult {
        tracks = tracks == null ? List.of() : List.copyOf(tracks);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
