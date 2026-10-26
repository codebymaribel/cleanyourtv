package com.gathin.tvhome;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * A nav tab's titles (nav_tabs), one at a time over its full-screen backdrop: the name, details, summary and the
 * Open in Stremio and Trailer buttons sit at the bottom, and under them a dash per title marks which one shows. The
 * dashes are the carousel: focused, Left and Right move to the previous or next title. Focus runs nav bar, buttons,
 * dashes, top to bottom. Titles come from a {@link CatalogSource} on a background thread and are kept per tab.
 */
final class TitleBrowser {

    /** What the activity does when the browser asks. */
    interface Host {
        /** Up from the buttons: back to the nav bar. */
        void onLeaveUp();

        /** The Open in Stremio button. */
        void openItem(@NonNull CatalogItem item);

        /** The Trailer button. */
        void playTrailer(@NonNull CatalogItem item);

        /** The hero now shows this title; null when the tab has none. */
        void onItemShown(@Nullable CatalogItem item);
    }

    private final View hero;
    private final TextView heroName;
    private final TextView heroMeta;
    private final TextView heroSummary;
    private final View openButton;
    private final View trailerButton;
    private final PageDashes dashes;
    private final View empty;
    private final Host host;
    private final CatalogSource source;
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final SparseArray<List<CatalogItem>> cache = new SparseArray<>();
    private final int[] remembered = new int[NavTabs.COUNT];

    private List<CatalogItem> items = Collections.emptyList();
    private int tab = NavTabs.NONE;
    /** The title showing; -1 when the tab has none. */
    private int current = -1;
    /** Whether focus was last on a button rather than the dashes, so coming back from an app returns there. */
    private boolean focusOnButtons = true;

    TitleBrowser(@NonNull View panel, @NonNull CatalogSource source, @NonNull Host host) {
        this.source = source;
        this.host = host;
        hero = panel.findViewById(R.id.titleHero);
        heroName = panel.findViewById(R.id.titleHeroName);
        heroMeta = panel.findViewById(R.id.titleHeroMeta);
        heroSummary = panel.findViewById(R.id.titleHeroSummary);
        openButton = panel.findViewById(R.id.titleHeroOpen);
        trailerButton = panel.findViewById(R.id.titleHeroTrailer);
        dashes = panel.findViewById(R.id.titleDashes);
        empty = panel.findViewById(R.id.titleEmpty);

        openButton.setOnClickListener(v -> withCurrent(host::openItem));
        trailerButton.setOnClickListener(v -> withCurrent(host::playTrailer));
        View.OnKeyListener buttonKeys = this::onButtonKey;
        openButton.setOnKeyListener(buttonKeys);
        trailerButton.setOnKeyListener(buttonKeys);
        View.OnFocusChangeListener buttonFocus = (v, focused) -> {
            if (focused) focusOnButtons = true;
        };
        openButton.setOnFocusChangeListener(buttonFocus);
        trailerButton.setOnFocusChangeListener(buttonFocus);
        dashes.setOnKeyListener((v, keyCode, event) -> onDashesKey(keyCode, event));
        dashes.setOnFocusChangeListener((v, focused) -> {
            if (focused) focusOnButtons = false;
        });
    }

    /** Shows a tab's titles: from the cache when they were loaded before, otherwise loaded in the background. */
    void show(int newTab) {
        if (newTab == tab) return;
        tab = newTab;
        focusOnButtons = true;
        current = -1;   // another tab's title is no place to keep
        List<CatalogItem> cached = cache.get(newTab);
        if (cached != null) {
            setItems(cached);
            return;
        }
        setItems(Collections.emptyList());
        empty.setVisibility(View.GONE);   // loading, not empty
        load(newTab);
    }

    /**
     * New titles arrived (the companion app finished a sync): forget every tab's list and load the visible one
     * again. The title showing stays if it is still in the list.
     */
    void reload() {
        cache.clear();
        if (NavTabs.isTab(tab)) load(tab);
    }

    /** Down from the nav bar: the Open in Stremio button. False when the tab has no titles yet. */
    boolean focusHero() {
        if (current < 0) return false;
        return openButton.requestFocus();
    }

    /** Back from an app: where focus was, a button or the dashes. False when the tab has no titles. */
    boolean restoreFocus() {
        if (current < 0) return false;
        return focusOnButtons ? openButton.requestFocus() : dashes.requestFocus();
    }

    /** Stops the background loader; the activity calls it when it goes away. */
    void shutdown() {
        loader.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    private void load(int forTab) {
        loader.execute(() -> {
            List<CatalogItem> loaded = source.load(forTab);
            main.post(() -> {
                cache.put(forTab, loaded);
                if (tab == forTab) setItems(loaded);
            });
        });
    }

    private void setItems(List<CatalogItem> newItems) {
        // A reload keeps the title showing when it is still there; otherwise the tab's remembered place.
        String keep = current >= 0 && current < items.size() ? items.get(current).id : null;
        items = newItems;
        boolean has = !newItems.isEmpty();
        empty.setVisibility(has ? View.GONE : View.VISIBLE);
        hero.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
        dashes.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
        if (!has) {
            current = -1;
            dashes.set(0, -1);
            host.onItemShown(null);
            return;
        }
        int at = indexOf(keep);
        showAt(at >= 0 ? at : TitleRowLayout.clamp(remembered[tab], newItems.size()));
    }

    private int indexOf(@Nullable String id) {
        if (id == null) return -1;
        for (int i = 0; i < items.size(); i++) if (items.get(i).id.equals(id)) return i;
        return -1;
    }

    private void showAt(int position) {
        current = position;
        remembered[tab] = position;
        CatalogItem item = items.get(position);
        heroName.setText(item.title);
        String meta = MetaLine.compose(item.year, item.genres, item.runtime);
        heroMeta.setText(meta);
        heroMeta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
        heroSummary.setText(item.summary);
        heroSummary.setVisibility(item.summary.isEmpty() ? View.GONE : View.VISIBLE);
        boolean trailer = YouTubeLinks.watch(item.trailerId) != null;
        if (!trailer && trailerButton.isFocused()) openButton.requestFocus();
        trailerButton.setVisibility(trailer ? View.VISIBLE : View.GONE);
        dashes.set(items.size(), position);
        dashes.setContentDescription(item.title);
        host.onItemShown(item);
    }

    private void withCurrent(Consumer<CatalogItem> action) {
        if (current >= 0 && current < items.size()) action.accept(items.get(current));
    }

    private boolean onButtonKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                host.onLeaveUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                dashes.requestFocus();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (v == trailerButton) openButton.requestFocus();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (v == openButton && trailerButton.getVisibility() == View.VISIBLE) trailerButton.requestFocus();
                return true;
            default:
                return false;
        }
    }

    private boolean onDashesKey(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                openButton.requestFocus();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;   // nothing under the dashes
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                int next = TitleRowLayout.step(current, items.size(), keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
                if (next != TitleRowLayout.NONE) showAt(next);
                return true;   // the first and last titles stop here
            }
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                withCurrent(host::openItem);   // OK on the carousel opens the title showing
                return true;
            default:
                return false;
        }
    }
}
