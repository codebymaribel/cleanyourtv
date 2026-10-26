package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for stepping along a row. */
public class TitleRowLayoutTest {

    @Test
    public void leftAndRightMoveOneCard() {
        assertEquals(4, TitleRowLayout.step(3, 20, true));
        assertEquals(2, TitleRowLayout.step(3, 20, false));
    }

    @Test
    public void theRowStopsAtItsEnds() {
        assertEquals(TitleRowLayout.NONE, TitleRowLayout.step(0, 20, false));
        assertEquals(TitleRowLayout.NONE, TitleRowLayout.step(19, 20, true));
    }

    @Test
    public void anEmptyRowOrAStrayPositionGoesNowhere() {
        assertEquals(TitleRowLayout.NONE, TitleRowLayout.step(0, 0, true));
        assertEquals(TitleRowLayout.NONE, TitleRowLayout.step(25, 20, false));
        assertEquals(TitleRowLayout.NONE, TitleRowLayout.step(-1, 20, true));
    }

    @Test
    public void aRememberedPositionStaysInsideTheList() {
        assertEquals(2, TitleRowLayout.clamp(2, 5));
        assertEquals(4, TitleRowLayout.clamp(9, 5));
        assertEquals(0, TitleRowLayout.clamp(-3, 5));
        assertEquals(0, TitleRowLayout.clamp(3, 0));
    }
}
