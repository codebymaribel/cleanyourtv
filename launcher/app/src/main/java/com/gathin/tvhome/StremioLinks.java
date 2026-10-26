package com.gathin.tvhome;

import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/** Stremio's deep links (stremio:///detail/...), so a card opens its title straight in the Stremio app. */
final class StremioLinks {

    /** Stremio for Android TV. */
    static final String PACKAGE = "com.stremio.one";

    private StremioLinks() {
    }

    /**
     * The link to a title's page. A movie's uses its own id as the video id, which opens Stremio on its list of
     * streams, so you pick the provider; a series' leaves the video out and opens its episode list. Null when the type
     * or the id is missing.
     */
    @Nullable
    static String detail(@Nullable String type, @Nullable String id) {
        if (type == null || id == null) return null;
        String t = type.trim();
        String i = id.trim();
        if (t.isEmpty() || i.isEmpty()) return null;
        String base = "stremio:///detail/" + encode(t) + "/" + encode(i);
        return "series".equals(t) ? base : base + "/" + encode(i);
    }

    private static String encode(String part) {
        try {
            // The String charset name, not the Charset overload: that one needs API 33 and minSdk is 26.
            return URLEncoder.encode(part, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);   // UTF-8 is always there
        }
    }
}
