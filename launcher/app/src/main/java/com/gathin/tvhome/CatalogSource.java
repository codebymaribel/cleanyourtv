package com.gathin.tvhome;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Where a nav tab's titles come from. {@link FakeCatalogSource} for now; the companion app (Stremboxd catalogs and the
 * Stremio library, cached on the TV) replaces it, so the launcher itself never touches the network.
 */
interface CatalogSource {

    /** The titles for a nav tab, in order; empty for Home or a tab with nothing in it. Called off the main thread. */
    @NonNull
    List<CatalogItem> load(int tab);
}
