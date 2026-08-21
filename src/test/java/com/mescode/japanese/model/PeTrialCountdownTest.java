package com.mescode.japanese.model;

import com.mescode.japanese.model.petrial.PeTrialCountdown;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeTrialCountdownTest {
    @Test
    void countdown_shouldFormatFortyMinutes() {
        PeTrialCountdown countdown = new PeTrialCountdown(40 * 60);

        assertEquals("40:00", countdown.getFormattedTime());
        assertFalse(countdown.isWarningTime());
    }

    @Test
    void countdown_shouldReportExpirationOnlyOnce() {
        PeTrialCountdown countdown = new PeTrialCountdown(1);

        PeTrialCountdown.TickResult first = countdown.tick();
        PeTrialCountdown.TickResult second = countdown.tick();

        assertEquals("00:00", countdown.getFormattedTime());
        assertTrue(first.expiredNow());
        assertFalse(second.expiredNow());
    }
}
