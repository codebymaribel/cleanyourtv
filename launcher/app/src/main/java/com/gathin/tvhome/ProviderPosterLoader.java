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
 * {@link PosterLoader} on the companion app's poster files. The posters arrive already shrunk to the focused card
 * (432 x 648), and decode as RGB_565, half the memory of ARGB_8888 (a poster has no transparency): about 560 KB
 * each. A memory cache of {@link PosterMemory#budget} bytes keeps a tab or two of them.
 */
final class ProviderPosterLoader implements PosterLoader {

    private final ContentResolver resolver;
    private final LruCache<String, Bitmap> cache;
    private final ExecutorService decoder = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    ProviderPosterLoader(@NonNull ContentResolver resolver, int budgetBytes) {
        this.resolver = resolver;
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
        try (InputStream in = resolver.openInputStream(Uri.parse(uri))) {
            return in == null ? null : BitmapFactory.decodeStream(in, null, options);
        } catch (IOException | SecurityException e) {
            return null;   // gone since the list was read, or not readable: the card keeps its placeholder
        }
    }
}
