package com.mescode.japanese.service.music;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

public final class PlaylistLoader {
    public static final String DEFAULT_PLAYLIST = "/music/playlist.json";

    private PlaylistLoader() {
    }

    public static PlaylistLoadResult loadBundled() {
        InputStream input = PlaylistLoader.class.getResourceAsStream(DEFAULT_PLAYLIST);
        if (input == null) {
            return new PlaylistLoadResult(
                    List.of(),
                    List.of("Không tìm thấy " + DEFAULT_PLAYLIST)
            );
        }
        try (input) {
            return load(input, resource -> PlaylistLoader.class.getResource(resource) != null);
        } catch (IOException exception) {
            return new PlaylistLoadResult(
                    List.of(),
                    List.of("Không thể đóng playlist: " + exception.getMessage())
            );
        }
    }

    static PlaylistLoadResult load(InputStream input, Predicate<String> resourceExists) {
        if (input == null) {
            return new PlaylistLoadResult(List.of(), List.of("Playlist rỗng"));
        }
        Predicate<String> exists = resourceExists == null ? resource -> true : resourceExists;
        Manifest manifest;
        try {
            manifest = new ObjectMapper().readValue(input, Manifest.class);
        } catch (IOException | RuntimeException exception) {
            return new PlaylistLoadResult(
                    List.of(),
                    List.of("Playlist không hợp lệ: " + exception.getMessage())
            );
        }

        List<MusicTrack> tracks = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        List<ManifestTrack> entries = manifest == null || manifest.tracks() == null
                ? List.of()
                : manifest.tracks();
        for (int index = 0; index < entries.size(); index++) {
            ManifestTrack entry = entries.get(index);
            if (entry == null) {
                warnings.add("Bỏ qua mục nhạc rỗng #" + (index + 1));
                continue;
            }
            MusicTrack track = new MusicTrack(
                    entry.id(), entry.title(), entry.artist(), entry.resource()
            );
            String label = track.id().isBlank() ? "#" + (index + 1) : track.id();
            if (track.id().isBlank() || track.title().isBlank() || track.resource().isBlank()) {
                warnings.add("Bỏ qua " + label + ": thiếu id, title hoặc resource");
                continue;
            }
            String normalizedId = track.id().toLowerCase(Locale.ROOT);
            if (!ids.add(normalizedId)) {
                warnings.add("Bỏ qua " + label + ": id bị trùng");
                continue;
            }
            if (!track.format().equals("wav") && !track.format().equals("mp3")) {
                warnings.add("Bỏ qua " + label + ": chỉ hỗ trợ WAV hoặc MP3");
                continue;
            }
            if (!exists.test(track.resource())) {
                warnings.add("Bỏ qua " + label + ": không tìm thấy " + track.resource());
                continue;
            }
            tracks.add(track);
        }
        return new PlaylistLoadResult(tracks, warnings);
    }

    private record Manifest(List<ManifestTrack> tracks) {
    }

    private record ManifestTrack(
            String id,
            String title,
            String artist,
            String resource
    ) {
    }
}
