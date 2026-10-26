package com.gathin.tvhome;

import androidx.annotation.Nullable;

/** Links that open a trailer in the YouTube app. Plain Java, for the JVM tests. */
final class YouTubeLinks {

    /** YouTube for Android TV. */
    static final String PACKAGE = "com.google.android.youtube.tv";

    private YouTubeLinks() {
    }

    /** The watch link for a YouTube video id, or null when it is not one (11 letters, digits, - or _). */
    @Nullable
    static String watch(@Nullable String videoId) {
        if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}")) return null;
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}
