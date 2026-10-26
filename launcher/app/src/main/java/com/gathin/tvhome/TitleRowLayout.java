package com.gathin.tvhome;

/** How the remote steps along a row: the nav tabs' titles and Home's Continue Watching. Plain Java, for the JVM tests. */
final class TitleRowLayout {

    /** Stay put: the row's end. */
    static final int NONE = -1;

    private TitleRowLayout() {
    }

    /** The item one step left or right, or {@link #NONE} at either end: the row does not wrap. */
    static int step(int position, int count, boolean forward) {
        if (position < 0 || position >= count) return NONE;
        int next = forward ? position + 1 : position - 1;
        return next >= 0 && next < count ? next : NONE;
    }

    /** A position to come back to, kept inside a list that may have shrunk; 0 when the list is empty. */
    static int clamp(int position, int count) {
        return count <= 0 ? 0 : Math.max(0, Math.min(position, count - 1));
    }
}
