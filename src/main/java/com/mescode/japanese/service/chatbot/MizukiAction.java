package com.mescode.japanese.service.chatbot;

/**
 * Structured side effect requested by an offline Mizuki reply. The UI owns
 * execution so the chatbot service remains independent from Swing and audio.
 */
public record MizukiAction(Type type, int value) {
    public MizukiAction {
        type = type == null ? Type.NONE : type;
    }

    public static MizukiAction none() {
        return new MizukiAction(Type.NONE, 0);
    }

    public static MizukiAction of(Type type) {
        return new MizukiAction(type, 0);
    }

    public boolean isMusicAction() {
        return type != Type.NONE;
    }

    public enum Type {
        NONE,
        MUSIC_PLAY,
        MUSIC_PAUSE,
        MUSIC_NEXT,
        MUSIC_PREVIOUS,
        MUSIC_SEEK_ABSOLUTE_SECONDS,
        MUSIC_SEEK_RELATIVE_SECONDS,
        MUSIC_SET_VOLUME_PERCENT,
        MUSIC_CHANGE_VOLUME_PERCENT,
        MUSIC_MUTE,
        MUSIC_UNMUTE,
        MUSIC_SHUFFLE_ON,
        MUSIC_SHUFFLE_OFF,
        MUSIC_REPEAT_OFF,
        MUSIC_REPEAT_ALL,
        MUSIC_REPEAT_ONE
    }
}
