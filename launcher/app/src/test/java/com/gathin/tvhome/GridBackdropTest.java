package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for the grid backdrop's geometry and colours. */
public class GridBackdropTest {

    private static final float EPS = 0.001f;
    /** Index of the first horizontal line in {@link GridBackdrop#lines}: the vertical lines come first. */
    private static final int FIRST_ROW = (GridBackdrop.COLUMNS - 1) * 4;

    @Test
    public void aThreeByThreeGridIsTwoLinesEachWay() {
        assertEquals(3, GridBackdrop.COLUMNS);
        assertEquals(3, GridBackdrop.ROWS);
        assertEquals((2 + 2) * 4, GridBackdrop.lines(1920, 1080).length);
    }

    @Test
    public void theLinesSplitAFullHdScreenIntoThirds() {
        float[] lines = GridBackdrop.lines(1920, 1080);
        assertEquals(640.5f, lines[0], EPS);
        assertEquals(1280.5f, lines[4], EPS);
        assertEquals(360.5f, lines[FIRST_ROW + 1], EPS);
        assertEquals(720.5f, lines[FIRST_ROW + 5], EPS);
    }

    @Test
    public void verticalLinesSpanTheHeightAndHorizontalLinesTheWidth() {
        float[] lines = GridBackdrop.lines(1920, 1080);
        assertEquals(0f, lines[1], EPS);
        assertEquals(1080f, lines[3], EPS);
        assertEquals(lines[0], lines[2], EPS);
        assertEquals(0f, lines[FIRST_ROW], EPS);
        assertEquals(1920f, lines[FIRST_ROW + 2], EPS);
        assertEquals(lines[FIRST_ROW + 1], lines[FIRST_ROW + 3], EPS);
    }

    @Test
    public void the720pThirdsSnapToPixelCentres() {
        // 1280 / 3 is 426.67 and 853.33: each line lands on the centre of the pixel it falls in.
        float[] lines = GridBackdrop.lines(1280, 720);
        assertEquals(426.5f, lines[0], EPS);
        assertEquals(853.5f, lines[4], EPS);
        assertEquals(240.5f, lines[FIRST_ROW + 1], EPS);
        assertEquals(480.5f, lines[FIRST_ROW + 5], EPS);
    }

    @Test
    public void anEmptySizeDrawsNoLines() {
        assertEquals(0, GridBackdrop.lines(0, 1080).length);
        assertEquals(0, GridBackdrop.lines(1920, 0).length);
        assertEquals(0, GridBackdrop.lines(-1, -1).length);
    }

    @Test
    public void theFadeGoesFromTheCentreColourToTheBackground() {
        int[] colors = GridBackdrop.colors(0x0B1020, 0x000000);
        assertEquals(0xFF0B1020, colors[0]);
        assertEquals(0xFF060810, colors[1]);
        assertEquals(0xFF000000, colors[2]);
        assertEquals(colors.length, GridBackdrop.STOPS.length);
    }

    @Test
    public void mixIsOpaqueAndHitsBothEnds() {
        assertEquals(0xFF0B1020, GridBackdrop.mix(0x0B1020, 0xFFFFFF, 0f));
        assertEquals(0xFFFFFFFF, GridBackdrop.mix(0x0B1020, 0xFFFFFF, 1f));
    }

    @Test
    public void theFadeReachesPastTheCorners() {
        // Centred a little above the middle, the radius must cover the farthest corner so no edge shows a seam.
        float r = GridBackdrop.radius(1920, 1080);
        double farthest = Math.hypot(1920 * GridBackdrop.CENTER_X, 1080 * (1 - GridBackdrop.CENTER_Y));
        assertTrue(r > farthest);
    }
}
