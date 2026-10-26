package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for the image caches' sizes. */
public class PosterMemoryTest {

    @Test
    public void theBackdropCacheHoldsFourFullBackdrops() {
        assertEquals(4 * 960 * 540 * 2, PosterMemory.BACKDROP_BYTES);
    }

    @Test
    public void theResumeCacheHoldsItsTenCardsAtHalfSize() {
        assertEquals(10 * 480 * 270 * 2, PosterMemory.RESUME_BYTES);
    }
}
