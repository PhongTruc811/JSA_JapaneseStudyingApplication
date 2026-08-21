package com.mescode.japanese.service.music;

public enum RepeatMode {
    OFF,
    ALL,
    ONE;

    public RepeatMode next() {
        return switch (this) {
            case OFF -> ALL;
            case ALL -> ONE;
            case ONE -> OFF;
        };
    }
}
