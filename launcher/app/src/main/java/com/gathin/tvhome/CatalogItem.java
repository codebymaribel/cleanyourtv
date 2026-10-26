package com.gathin.tvhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** One title in a nav tab's carousel: what Stremio needs to open it, and what the card and detail panel show. */
final class CatalogItem {

    /** Stremio content type: "movie" or "series". */
    @NonNull final String type;
    /** Stremio meta id, an IMDb id such as "tt0947798" for Cinemeta titles. */
    @NonNull final String id;
    @NonNull final String title;
    /** Shown in the detail panel next to the focused card; empty when there is none. */
    @NonNull final String summary;
    /** The poster's content URI in the companion app; null when it has none yet, and the card draws a placeholder. */
    @Nullable final String posterUri;

    CatalogItem(@NonNull String type, @NonNull String id, @NonNull String title, @NonNull String summary,
                @Nullable String posterUri) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.posterUri = posterUri;
    }
}
