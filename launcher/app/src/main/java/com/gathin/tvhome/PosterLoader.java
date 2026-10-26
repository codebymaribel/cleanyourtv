package com.gathin.tvhome;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

/** Decodes carousel posters off the main thread and keeps the recent ones in memory. */
interface PosterLoader {

    /** The decoded poster when it is in memory, or null. Main thread. */
    @Nullable
    Bitmap cached(@NonNull String uri);

    /** Decodes the poster in the background and hands it (or null when it cannot be read) to {@code done} on the main thread. */
    void load(@NonNull String uri, @NonNull Consumer<Bitmap> done);

    /** Forgets every decoded poster, after a sync replaced them. */
    void clear();

    /** Stops the decoder thread; the activity calls it when it goes away. */
    void shutdown();
}
