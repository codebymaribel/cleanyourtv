package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for where the page indicator's dashes sit: 12 px dashes, the active one 28 px, 6 px gaps. */
public class DashLayoutTest {

    private static final float EPS = 0.001f;

    @Test
    public void allTheDashesAndGapsMakeTheWidth() {
        assertEquals(19 * (12 + 6) + 28, DashLayout.totalWidth(20, 12, 28, 6), EPS);
        assertEquals(28, DashLayout.totalWidth(1, 12, 28, 6), EPS);
        assertEquals(0, DashLayout.totalWidth(0, 12, 28, 6), EPS);
    }

    @Test
    public void dashesBeforeTheActiveOneSitAtTheirPlace() {
        assertEquals(0, DashLayout.left(0, 3, 12, 28, 6), EPS);
        assertEquals(2 * 18, DashLayout.left(2, 3, 12, 28, 6), EPS);
        assertEquals(3 * 18, DashLayout.left(3, 3, 12, 28, 6), EPS);
    }

    @Test
    public void dashesAfterTheActiveOneMoveRightByItsExtraLength() {
        assertEquals(4 * 18 + 16, DashLayout.left(4, 3, 12, 28, 6), EPS);
        // The last dash ends exactly at the total width.
        float lastLeft = DashLayout.left(19, 3, 12, 28, 6);
        assertEquals(DashLayout.totalWidth(20, 12, 28, 6), lastLeft + DashLayout.width(19, 3, 12, 28), EPS);
    }

    @Test
    public void onlyTheActiveDashIsLong() {
        assertEquals(28, DashLayout.width(3, 3, 12, 28), EPS);
        assertEquals(12, DashLayout.width(4, 3, 12, 28), EPS);
    }
}
