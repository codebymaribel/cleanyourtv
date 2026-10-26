package com.gathin.tvhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** The words and numbers on a Continue Watching card. Plain Java, for the JVM tests. */
final class ResumeText {

    private ResumeText() {
    }

    /** "T2 · E5" for a series video id ("tt0903747:2:5"); "" for a movie or anything else. */
    @NonNull
    static String episode(@Nullable String videoId) {
        if (videoId == null) return "";
        String[] parts = videoId.split(":");
        if (parts.length != 3 || !parts[1].matches("\\d{1,4}") || !parts[2].matches("\\d{1,5}")) return "";
        return "T" + Integer.parseInt(parts[1]) + " · E" + Integer.parseInt(parts[2]);
    }

    /** The progress bar's fill, 0 to 1, from the 0 to 1000 the companion app stores. */
    static float fraction(int permille) {
        return Math.max(0, Math.min(1000, permille)) / 1000f;
    }
}
