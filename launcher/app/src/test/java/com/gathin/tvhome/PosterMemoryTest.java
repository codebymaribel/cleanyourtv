package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for the poster cache's memory budget. */
public class PosterMemoryTest {

    private static final long MB = 1024 * 1024;

    @Test
    public void aSmallHeapGetsAnEighth() {
        assertEquals((int) (64 * MB / 8), PosterMemory.budget(64 * MB));
    }

    @Test
    public void aLargeHeapIsCappedAt16Mb() {
        assertEquals(PosterMemory.MAX_BYTES, PosterMemory.budget(512 * MB));
    }

    @Test
    public void theBackdropCacheHoldsFourFullBackdrops() {
        assertEquals(4 * 960 * 540 * 2, PosterMemory.BACKDROP_BYTES);
    }

    @Test
    public void nonsenseGivesNoCache() {
        assertEquals(0, PosterMemory.budget(-1));
    }
}
