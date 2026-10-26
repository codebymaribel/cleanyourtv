package com.gathin.tvhome;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Continue Watching from the companion app's provider (FeedsContract). Empty when the app is missing or has none. */
final class ProviderResumeSource {

    private final ContentResolver resolver;

    ProviderResumeSource(@NonNull ContentResolver resolver) {
        this.resolver = resolver;
    }

    /** Off the main thread. */
    @NonNull
    List<ResumeItem> load() {
        try (Cursor c = resolver.query(Uri.parse(FeedsContract.resume()), null, null, null, null)) {
            if (c == null) return Collections.emptyList();
            int type = c.getColumnIndexOrThrow(FeedsContract.COLUMN_TYPE);
            int id = c.getColumnIndexOrThrow(FeedsContract.COLUMN_ID);
            int videoId = c.getColumnIndexOrThrow(FeedsContract.COLUMN_VIDEO_ID);
            int title = c.getColumnIndexOrThrow(FeedsContract.COLUMN_TITLE);
            int progress = c.getColumnIndexOrThrow(FeedsContract.COLUMN_PROGRESS);
            int hasBackdrop = c.getColumnIndexOrThrow(FeedsContract.COLUMN_HAS_BACKDROP);
            List<ResumeItem> items = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                String itemId = text(c, id);
                String backdrop = c.getInt(hasBackdrop) == 1 ? FeedsContract.backdrop(itemId) : null;
                items.add(new ResumeItem(text(c, type), itemId, text(c, videoId), text(c, title), c.getInt(progress), backdrop));
            }
            return items;
        } catch (SecurityException | IllegalArgumentException e) {
            // Not signed with the same key, or an app from before Continue Watching: no row rather than a crash.
            return Collections.emptyList();
        }
    }

    private static String text(Cursor c, int column) {
        String value = c.getString(column);
        return value == null ? "" : value;
    }
}
