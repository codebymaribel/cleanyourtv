package com.gathin.tvhome;

import androidx.annotation.NonNull;

/**
 * The line of details under a title in the hero: "2010 · Drama, Thriller · 108 min · ★ 4.2". Plain Java, for the JVM
 * tests.
 */
final class MetaLine {

    static final String SEPARATOR = " · ";
    /** Before the Letterboxd rating. */
    static final String STAR = "★ ";

    private MetaLine() {
    }

    /** The parts that are known, in order, joined by {@link #SEPARATOR}; empty when none is. */
    @NonNull
    static String compose(String year, String genres, String runtime, String rating) {
        StringBuilder line = new StringBuilder();
        String star = rating == null || rating.trim().isEmpty() ? null : STAR + rating.trim();
        for (String part : new String[]{year, genres, runtime, star}) {
            if (part == null || part.trim().isEmpty()) continue;
            if (line.length() > 0) line.append(SEPARATOR);
            line.append(part.trim());
        }
        return line.toString();
    }
}
