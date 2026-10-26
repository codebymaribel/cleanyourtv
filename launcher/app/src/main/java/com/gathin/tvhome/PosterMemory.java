package com.gathin.tvhome;

/** How much memory the image caches may use. Plain Java, so the JVM tests can check it. */
final class PosterMemory {

    /** The nav tabs' backdrop cache: four 960 x 540 RGB_565 backdrops, about 1 MB each. */
    static final int BACKDROP_BYTES = 4 * 960 * 540 * 2;

    /** Continue Watching's cache: its ten cards' backdrops, decoded at half (480 x 270 RGB_565). */
    static final int RESUME_BYTES = 10 * 480 * 270 * 2;

    private PosterMemory() {
    }
}
