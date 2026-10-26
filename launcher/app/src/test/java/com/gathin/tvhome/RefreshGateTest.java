package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for how often Home asks the companion app for a sync. */
public class RefreshGateTest {

    @Test
    public void theFirstReturnHomeAsks() {
        assertTrue(RefreshGate.due(0, RefreshGate.NEVER));
        assertTrue(RefreshGate.due(123_456, RefreshGate.NEVER));
    }

    @Test
    public void notAgainWithinFiveMinutes() {
        long asked = 1_000_000;
        assertFalse(RefreshGate.due(asked + 60_000, asked));
        assertFalse(RefreshGate.due(asked + RefreshGate.MIN_GAP_MS - 1, asked));
        assertTrue(RefreshGate.due(asked + RefreshGate.MIN_GAP_MS, asked));
    }

    @Test
    public void aClockThatWentBackAsks() {
        assertTrue(RefreshGate.due(10, 1_000_000));
    }
}
