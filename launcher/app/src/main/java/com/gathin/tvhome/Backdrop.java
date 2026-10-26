package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The focused title's backdrop, full screen behind everything (the nav bar too) and above the grid backdrop, while a
 * nav tab other than Home is open. Two image views take turns, so one fades in over the other; a fixed scrim keeps the text
 * readable. A new backdrop loads only once the focus has rested for {@link #SETTLE_MS}, so running along the row
 * decodes nothing for the cards it passes.
 */
final class Backdrop {

    static final long SETTLE_MS = 250;
    private static final long FADE_MS = 300;

    private final ImageView[] images = new ImageView[2];
    private final View scrim;
    private final PosterLoader loader;
    private final boolean animate;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable apply = this::apply;

    /** The view showing the current backdrop; the other one is the next to fade in. */
    private int front;
    /** What the hero asked for last, and what is on screen; null for none. */
    @Nullable private String wanted;
    @Nullable private String shown;

    Backdrop(@NonNull View layer, @NonNull PosterLoader loader, boolean animate) {
        images[0] = layer.findViewById(R.id.backdropA);
        images[1] = layer.findViewById(R.id.backdropB);
        scrim = layer.findViewById(R.id.backdropScrim);
        this.loader = loader;
        this.animate = animate;
        layer.setVisibility(View.VISIBLE);
    }

    /** Show this backdrop once the focus settles; null fades back to the grid. */
    void show(@Nullable String uri) {
        wanted = uri;
        handler.removeCallbacks(apply);
        if (animate) handler.postDelayed(apply, SETTLE_MS);
        else apply();
    }

    /** Back to the grid at once: Home has no backdrop. */
    void hide() {
        wanted = null;
        handler.removeCallbacks(apply);
        fadeTo(null, null);
    }

    /** Stops pending work; the activity calls it when it goes away. */
    void shutdown() {
        handler.removeCallbacksAndMessages(null);
    }

    private void apply() {
        String uri = wanted;
        if (uri == null) {
            fadeTo(null, null);
            return;
        }
        if (uri.equals(shown)) return;
        Bitmap cached = loader.cached(uri);
        if (cached != null) {
            fadeTo(cached, uri);
            return;
        }
        loader.load(uri, bitmap -> {
            if (uri.equals(wanted)) fadeTo(bitmap, bitmap == null ? null : uri);   // late results for old cards are dropped
        });
    }

    private void fadeTo(@Nullable Bitmap bitmap, @Nullable String uri) {
        shown = uri;
        ImageView out = images[front];
        if (bitmap == null) {
            alpha(out, 0f);
            alpha(images[1 - front], 0f);
            alpha(scrim, 0f);
            return;
        }
        int next = 1 - front;
        ImageView in = images[next];
        in.setImageBitmap(bitmap);
        alpha(in, 1f);
        alpha(out, 0f);
        alpha(scrim, 1f);
        front = next;
    }

    private void alpha(View v, float to) {
        v.animate().cancel();
        if (animate) v.animate().alpha(to).setDuration(FADE_MS).start();
        else v.setAlpha(to);
    }
}
