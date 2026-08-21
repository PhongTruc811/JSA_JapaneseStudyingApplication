package com.mescode.japanese.model.petrial;

public final class PeTrialCountdown {
    private int remainingSeconds;
    private boolean expirationReported;

    public PeTrialCountdown(int durationSeconds) {
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("durationSeconds must not be negative");
        }
        remainingSeconds = durationSeconds;
    }

    public TickResult tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
        boolean expiredNow = remainingSeconds == 0 && !expirationReported;
        if (expiredNow) {
            expirationReported = true;
        }
        return new TickResult(remainingSeconds, expiredNow);
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getFormattedTime() {
        return formatTime(remainingSeconds);
    }

    public boolean isWarningTime() {
        return remainingSeconds <= 5 * 60;
    }

    public static String formatTime(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        return String.format("%02d:%02d", safeSeconds / 60, safeSeconds % 60);
    }

    public record TickResult(int remainingSeconds, boolean expiredNow) {
    }
}
