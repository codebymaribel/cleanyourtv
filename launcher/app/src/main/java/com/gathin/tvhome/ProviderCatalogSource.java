package com.gathin.tvhome;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A tab's titles from the companion app's provider (FeedsContract). Empty when the app is missing or has none. */
final class ProviderCatalogSource implements CatalogSource {

    private final ContentResolver resolver;

    ProviderCatalogSource(@NonNull ContentResolver resolver) {
        this.resolver = resolver;
    }

    @NonNull
    @Override
    public List<CatalogItem> load(int tab) {
        if (!NavTabs.isTab(tab) || NavTabs.showsGrid(tab)) return Collections.emptyList();
        try (Cursor c = resolver.query(Uri.parse(FeedsContract.feed(tab)), null, null, null, null)) {
            if (c == null) return Collections.emptyList();   // the app is not installed
            int type = c.getColumnIndexOrThrow(FeedsContract.COLUMN_TYPE);
            int id = c.getColumnIndexOrThrow(FeedsContract.COLUMN_ID);
            int title = c.getColumnIndexOrThrow(FeedsContract.COLUMN_TITLE);
            int summary = c.getColumnIndexOrThrow(FeedsContract.COLUMN_SUMMARY);
            // Columns an older app does not have yet: -1, read as empty.
            int hasBackdrop = c.getColumnIndex(FeedsContract.COLUMN_HAS_BACKDROP);
            int year = c.getColumnIndex(FeedsContract.COLUMN_YEAR);
            int genres = c.getColumnIndex(FeedsContract.COLUMN_GENRES);
            int runtime = c.getColumnIndex(FeedsContract.COLUMN_RUNTIME);
            int trailer = c.getColumnIndex(FeedsContract.COLUMN_TRAILER);
            int rating = c.getColumnIndex(FeedsContract.COLUMN_RATING);
            int friend = c.getColumnIndex(FeedsContract.COLUMN_FRIEND);
            List<CatalogItem> items = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                String itemId = text(c, id);
                String backdrop = hasBackdrop >= 0 && c.getInt(hasBackdrop) == 1 ? FeedsContract.backdrop(itemId) : null;
                String trailerId = text(c, trailer);
                items.add(new CatalogItem(text(c, type), itemId, text(c, title), text(c, summary), backdrop,
                        text(c, year), text(c, genres), text(c, runtime), trailerId.isEmpty() ? null : trailerId,
                        text(c, rating), text(c, friend)));
            }
            return items;
        } catch (SecurityException | IllegalArgumentException e) {
            // Not signed with the same key, or an older app with other columns: no titles rather than a crash.
            return Collections.emptyList();
        }
    }

    private static String text(Cursor c, int column) {
        if (column < 0) return "";
        String value = c.getString(column);
        return value == null ? "" : value;
    }
}
