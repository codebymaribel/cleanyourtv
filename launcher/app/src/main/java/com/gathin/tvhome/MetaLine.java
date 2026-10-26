package com.gathin.tvhome;

import androidx.annotation.NonNull;

/** The line of details under a title in the hero: "2010 · Drama, Thriller · 108 min". Plain Java, for the JVM tests. */
final class MetaLine {

    static final String SEPARATOR = " · ";

    private MetaLine() {
    }

    /** The parts that are known, in order, joined by {@link #SEPARATOR}; empty when none is. */
    @NonNull
    static String compose(String year, String genres, String runtime) {
        StringBuilder line = new StringBuilder();
        for (String part : new String[]{year, genres, runtime}) {
            if (part == null || part.trim().isEmpty()) continue;
            if (line.length() > 0) line.append(SEPARATOR);
            line.append(part.trim());
        }
        return line.toString();
    }
}
