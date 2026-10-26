package com.gathin.tvhome;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asks the companion app, off the main thread, to sync if its data is stale. The app answers right away and syncs in a
 * job of its own; when that finishes, its notifyChange reloads the nav tabs. Asks at most every {@link
 * RefreshGate#MIN_GAP_MS}.
 */
final class FeedsRefresh {

    private final ContentResolver resolver;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private long lastAskMs = RefreshGate.NEVER;

    FeedsRefresh(@NonNull ContentResolver resolver) {
        this.resolver = resolver;
    }

    /** Main thread, on every return to Home. */
    void requestIfStale() {
        long now = SystemClock.elapsedRealtime();
        if (!RefreshGate.due(now, lastAskMs)) return;
        lastAskMs = now;
        worker.execute(() -> {
            try {
                resolver.call(Uri.parse(FeedsContract.root()), FeedsContract.METHOD_SYNC_IF_STALE, null, null);
            } catch (IllegalArgumentException | SecurityException e) {
                // The app is not installed, or not signed with the same key: nothing to refresh.
            }
        });
    }

    void shutdown() {
        worker.shutdownNow();
    }
}
