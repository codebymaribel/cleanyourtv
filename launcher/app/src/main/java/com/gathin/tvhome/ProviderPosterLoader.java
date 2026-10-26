package com.gathin.tvhome;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * {@link PosterLoader} on the companion app's image files (backdrops, already shrunk to 960 x 540). They decode as
 * RGB_565, half the memory of ARGB_8888 (they have no transparency), into an LRU cache of the given size
 * ({@link PosterMemory}).
 */
final class ProviderPosterLoader implements PosterLoader {

    private final ContentResolver resolver;
    private final int sampleSize;
    private final LruCache<String, Bitmap> cache;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    /**
     * @param sampleSize decode at 1/sampleSize of the stored size: 2 for Continue Watching's cards, which are about
     *                   half a stored backdrop and take a quarter of the memory that way; 1 for full-screen backdrops.
     */
    ProviderPosterLoader(@NonNull ContentResolver resolver, int budgetBytes, int sampleSize) {
        this.resolver = resolver;
        this.sampleSize = Math.max(1, sampleSize);
        cache = new LruCache<String, Bitmap>(Math.max(1, budgetBytes)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    @Nullable
    @Override
    public Bitmap cached(@NonNull String uri) {
        return cache.get(uri);
    }

    @Override
    public void load(@NonNull String uri, @NonNull Consumer<Bitmap> done) {
        decoder.execute(() -> {
            Bitmap bitmap = decode(uri);
            main.post(() -> {
                if (bitmap != null) cache.put(uri, bitmap);
                done.accept(bitmap);
            });
        });
    }

    @Override
    public void clear() {
        cache.evictAll();
    }

    @Override
    public void shutdown() {
        decoder.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    @Nullable
    private Bitmap decode(String uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = sampleSize;
        try (InputStream in = resolver.openInputStream(Uri.parse(uri))) {
            return in == null ? null : BitmapFactory.decodeStream(in, null, options);
        } catch (IOException | SecurityException e) {
            return null;   // gone since the list was read, or not readable: the card keeps its placeholder
        }
    }
}
