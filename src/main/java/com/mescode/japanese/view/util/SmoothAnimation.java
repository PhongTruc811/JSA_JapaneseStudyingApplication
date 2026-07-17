package com.mescode.japanese.view.util;

import javax.swing.*;

/**
 * Utility for smoothly interpolating a float value over time using a Swing Timer.
 * Useful for hover/press animations without external libraries.
 */
public class SmoothAnimation {

    private final Timer timer;
    private float current;
    private float target;
    private final float speed; // interpolation factor per tick (0.0 - 1.0)
    private Runnable onUpdate;

    public SmoothAnimation(float initial, float speed, int delayMs) {
        this.current = initial;
        this.target = initial;
        this.speed = Math.max(0.01f, Math.min(1.0f, speed));
        this.timer = new Timer(delayMs, e -> tick());
    }

    public SmoothAnimation(float initial) {
        this(initial, 0.15f, 16);
    }

    private void tick() {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            current = target;
            timer.stop();
        } else {
            current += diff * speed;
        }
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public void setTarget(float target) {
        this.target = target;
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public float getCurrent() {
        return current;
    }

    public void snapTo(float value) {
        this.target = value;
        this.current = value;
        timer.stop();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void stop() {
        timer.stop();
    }
}
