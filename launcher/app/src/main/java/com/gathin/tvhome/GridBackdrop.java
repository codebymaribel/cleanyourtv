package com.gathin.tvhome;

/**
 * Geometry and colours for the grid backdrop (config.xml, grid_backdrop_color): a radial fade from a dark colour
 * to the background colour, with a faint 3 x 3 grid over it. Plain Java, no Android types, so the JVM tests can
 * check it without a TV; {@link GridBackdropDrawable} does the drawing.
 */
final class GridBackdrop {

    /** The grid splits the screen evenly into this many columns and rows; the lines run between them. */
    static final int COLUMNS = 3;
    static final int ROWS = 3;
    /** White at about 7 percent: faint enough that tiles and text stay the brightest things on screen. */
    static final int LINE_COLOR = 0x12FFFFFF;
    /** Centre of the fade, as fractions of the width and height: a little above the middle of the screen. */
    static final float CENTER_X = 0.5f;
    static final float CENTER_Y = 0.38f;
    /** The fade reaches the background colour at this fraction of the screen's diagonal. */
    static final float RADIUS = 0.6f;
    /** Gradient stops for {@link #colors}: the centre colour holds briefly, then fades to the background. */
    static final float[] STOPS = {0f, 0.35f, 1f};

    private GridBackdrop() {
    }

    /** The fade's radius in pixels. */
    static float radius(int width, int height) {
        return (float) Math.hypot(width, height) * RADIUS;
    }

    /** The three gradient colours, opaque: the centre colour, a halfway mix, and the background. */
    static int[] colors(int center, int background) {
        return new int[]{0xFF000000 | center, mix(center, background, 0.5f), 0xFF000000 | background};
    }

    /** Mixes two colours channel by channel; t = 0 gives a, t = 1 gives b. Always opaque. */
    static int mix(int a, int b, float t) {
        int r = channel(a >> 16, b >> 16, t);
        int g = channel(a >> 8, b >> 8, t);
        int bl = channel(a, b, t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static int channel(int a, int b, float t) {
        a &= 0xFF;
        b &= 0xFF;
        return Math.round(a + (b - a) * t);
    }

    /**
     * Endpoints for {@code Canvas.drawLines}, four floats per line: the vertical lines first, then the horizontal
     * ones. Only the lines between cells are drawn (the screen's edges close the outer ones), so a 3 x 3 grid is
     * two lines each way, at the thirds. Each line sits on a pixel's centre so a one-pixel line stays crisp
     * without antialiasing. Empty when there is nothing to draw.
     */
    static float[] lines(int width, int height) {
        if (width <= 0 || height <= 0) return new float[0];
        float[] out = new float[(COLUMNS - 1 + ROWS - 1) * 4];
        int i = 0;
        for (int c = 1; c < COLUMNS; c++) {
            float x = snap(width * c / (float) COLUMNS);
            out[i++] = x;
            out[i++] = 0f;
            out[i++] = x;
            out[i++] = height;
        }
        for (int r = 1; r < ROWS; r++) {
            float y = snap(height * r / (float) ROWS);
            out[i++] = 0f;
            out[i++] = y;
            out[i++] = width;
            out[i++] = y;
        }
        return out;
    }

    /** The centre of the pixel a position falls in. */
    static float snap(float position) {
        return (float) Math.floor(position) + 0.5f;
    }
}
