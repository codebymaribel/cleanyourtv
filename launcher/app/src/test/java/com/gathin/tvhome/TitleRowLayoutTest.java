package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for moving along the row of posters. */
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

    @Test
    public void aTitleAlwaysGetsTheSamePlaceholderColour() {
        int c = TitleRowLayout.placeholderColor("Black Swan");
        assertEquals(c, TitleRowLayout.placeholderColor("Black Swan"));
        boolean inPalette = false;
        for (int p : TitleRowLayout.PLACEHOLDER_COLORS) inPalette |= p == c;
        assertTrue(inPalette);
        TitleRowLayout.placeholderColor(null);
    }
}
