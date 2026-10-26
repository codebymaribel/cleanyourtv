package com.gathin.tvhome;

/** How the remote moves along a nav tab's row of posters. Plain Java, so the JVM tests can check it without a TV. */
final class TitleRowLayout {

    /** Stay on the card: the row's end. */
    static final int NONE = -1;

    /** Dark backgrounds for cards without a poster yet, picked by title so a card keeps its colour. */
    static final int[] PLACEHOLDER_COLORS = {0xFF1B2A4A, 0xFF2A1B4A, 0xFF1B4A42, 0xFF4A2A1B, 0xFF3A1B2E, 0xFF22303A};

    private TitleRowLayout() {
    }

    /** The card one step left or right, or {@link #NONE} at either end: the row does not wrap. */
    static int step(int position, int count, boolean forward) {
        if (position < 0 || position >= count) return NONE;
        int next = forward ? position + 1 : position - 1;
        return next >= 0 && next < count ? next : NONE;
    }

    /** A position to come back to, kept inside a list that may have shrunk; 0 when the list is empty. */
    static int clamp(int position, int count) {
        return count <= 0 ? 0 : Math.max(0, Math.min(position, count - 1));
    }

    /** The placeholder colour for a title: the same title always gets the same colour. */
    static int placeholderColor(String title) {
        int h = title == null ? 0 : title.hashCode();
        return PLACEHOLDER_COLORS[Math.floorMod(h, PLACEHOLDER_COLORS.length)];
    }
}
