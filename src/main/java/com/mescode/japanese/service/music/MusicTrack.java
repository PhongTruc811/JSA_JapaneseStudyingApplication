package com.mescode.japanese.service.music;

public record MusicTrack(
        String id,
        String title,
        String artist,
        String resource
) {
    public MusicTrack {
        id = clean(id);
        title = clean(title);
        artist = clean(artist);
        resource = normalizeResource(resource);
    }

    public String format() {
        int dot = resource.lastIndexOf('.');
        return dot < 0 ? "" : resource.substring(dot + 1).toLowerCase();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeResource(String value) {
        String clean = clean(value).replace('\\', '/');
        if (clean.isEmpty() || clean.startsWith("/")) {
            return clean;
        }
        return "/" + clean;
    }
}
