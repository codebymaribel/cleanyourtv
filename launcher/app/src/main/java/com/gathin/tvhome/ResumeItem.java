package com.gathin.tvhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** One title in Home's Continue Watching row, from the companion app's copy of the Stremio library. */
final class ResumeItem {

    /** "movie" or "series". */
    @NonNull final String type;
    @NonNull final String id;
    /** The video to resume: the movie's id, or "tt...:season:episode"; "" when unknown. */
    @NonNull final String videoId;
    @NonNull final String title;
    /** How far in, 0 to 1000. */
    final int progress;
    /** The card's backdrop in the companion app; null without one. */
    @Nullable final String backdropUri;

    ResumeItem(@NonNull String type, @NonNull String id, @NonNull String videoId, @NonNull String title, int progress,
               @Nullable String backdropUri) {
        this.type = type;
        this.id = id;
        this.videoId = videoId;
        this.title = title;
        this.progress = progress;
        this.backdropUri = backdropUri;
    }
}
