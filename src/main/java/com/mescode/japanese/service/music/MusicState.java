package com.mescode.japanese.service.music;

public record MusicState(
        MusicTrack track,
        int trackIndex,
        int trackCount,
        PlaybackStatus status,
        long positionMillis,
        long durationMillis,
        float volume,
        boolean muted,
        boolean shuffle,
        RepeatMode repeatMode,
        String errorMessage
) {
    public MusicState {
        status = status == null ? PlaybackStatus.UNAVAILABLE : status;
        repeatMode = repeatMode == null ? RepeatMode.ALL : repeatMode;
        positionMillis = Math.max(0L, positionMillis);
        durationMillis = Math.max(0L, durationMillis);
        volume = Math.max(0f, Math.min(1f, volume));
        errorMessage = errorMessage == null ? "" : errorMessage.trim();
    }

    public boolean available() {
        return track != null
                && trackCount > 0
                && status != PlaybackStatus.UNAVAILABLE
                && status != PlaybackStatus.CLOSED;
    }

    public boolean playing() {
        return status == PlaybackStatus.PLAYING || status == PlaybackStatus.LOADING;
    }
}
