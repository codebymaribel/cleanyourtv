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
            int hasPoster = c.getColumnIndexOrThrow(FeedsContract.COLUMN_HAS_POSTER);
            List<CatalogItem> items = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                String itemId = text(c, id);
                String poster = c.getInt(hasPoster) == 1 ? FeedsContract.poster(tab, itemId) : null;
                items.add(new CatalogItem(text(c, type), itemId, text(c, title), text(c, summary), poster));
            }
            return items;
        } catch (SecurityException | IllegalArgumentException e) {
            // Not signed with the same key, or an older app with other columns: no titles rather than a crash.
            return Collections.emptyList();
        }
    }

    private static String text(Cursor c, int column) {
        String value = c.getString(column);
        return value == null ? "" : value;
    }
}
