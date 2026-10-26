package com.gathin.tvhome;

/** How much memory the carousel's poster cache may use. Plain Java, so the JVM tests can check it. */
final class PosterMemory {

    /** Never more than 16 MB: about 28 posters, more than a tab shows, on a TV with little memory to spare. */
    static final int MAX_BYTES = 16 * 1024 * 1024;

    private PosterMemory() {
    }

    /** An eighth of the app's heap, capped at {@link #MAX_BYTES}. */
    static int budget(long maxHeapBytes) {
        return (int) Math.max(0, Math.min(MAX_BYTES, maxHeapBytes / 8));
    }
}
