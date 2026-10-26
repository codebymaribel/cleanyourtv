package com.gathin.tvhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** One title in a nav tab: what Stremio needs to open it, and what its card and the hero show. */
final class CatalogItem {

    /** Stremio content type: "movie" or "series". */
    @NonNull final String type;
    /** Stremio meta id, an IMDb id such as "tt0947798" for Cinemeta titles. */
    @NonNull final String id;
    @NonNull final String title;
    /** Shown under the title in the hero; empty when there is none. */
    @NonNull final String summary;
    /** The title's wide still in the companion app, shown full screen behind the tab; null without one. */
    @Nullable final String backdropUri;
    /** Release year, "" when unknown. */
    @NonNull final String year;
    /** Up to three genres, already joined ("Drama, Thriller"); "" when none. */
    @NonNull final String genres;
    /** Running time as the catalog gives it; "" when unknown. */
    @NonNull final String runtime;
    /** The trailer's YouTube video id; null without one. */
    @Nullable final String trailerId;
    /** Letterboxd's average rating out of 5 ("4.2"); "" when unknown. */
    @NonNull final String rating;
    /** In Friends, who watched it ("Luis Bohorquez · ★½"); "" elsewhere. */
    @NonNull final String friend;

    CatalogItem(@NonNull String type, @NonNull String id, @NonNull String title, @NonNull String summary,
                @Nullable String backdropUri, @NonNull String year, @NonNull String genres, @NonNull String runtime,
                @Nullable String trailerId, @NonNull String rating, @NonNull String friend) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.backdropUri = backdropUri;
        this.year = year;
        this.genres = genres;
        this.runtime = runtime;
        this.trailerId = trailerId;
        this.rating = rating;
        this.friend = friend;
    }
}
