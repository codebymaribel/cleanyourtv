package com.gathin.tvhome;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A nav tab's titles (nav_tabs), Apple TV style: a hero filling the top with the focused title's name, details,
 * summary and its Open in Stremio and Trailer buttons, and under it every poster in one row that scrolls sideways. Titles come from a
 * {@link CatalogSource} on a background thread and are kept per tab; posters come from a {@link PosterLoader}.
 * Focus runs nav bar, hero buttons, row, top to bottom.
 */
final class TitleBrowser {

    /** What the activity does when the browser asks. */
    interface Host {
        /** Up from the hero: back to the nav bar. */
        void onLeaveUp();

        /** OK on a card, or the Open in Stremio button. */
        void openItem(@NonNull CatalogItem item);

        /** The Trailer button. */
        void playTrailer(@NonNull CatalogItem item);

        /** The hero now shows this title; null when the tab has none. */
        void onItemShown(@Nullable CatalogItem item);
    }

    private static final float FOCUS_SCALE = 1.08f;
    private static final long ANIM_MS = 160;

    private final RecyclerView row;
    private final View hero;
    private final TextView heroName;
    private final TextView heroMeta;
    private final TextView heroSummary;
    private final View openButton;
    private final View trailerButton;
    private final View empty;
    private final Host host;
    private final CatalogSource source;
    private final PosterLoader posters;
    private final boolean animate;
    private final Adapter adapter = new Adapter();
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final SparseArray<List<CatalogItem>> cache = new SparseArray<>();
    private final int[] remembered = new int[NavTabs.COUNT];

    private int tab = NavTabs.NONE;
    /** The title the hero shows; -1 when the tab has none. */
    private int current = -1;
    /** Whether focus was last on a hero button rather than a card, so coming back from an app returns there. */
    private boolean focusInHero;

    TitleBrowser(@NonNull View panel, @NonNull CatalogSource source, @NonNull PosterLoader posters, boolean animate,
                 @NonNull Host host) {
        this.source = source;
        this.posters = posters;
        this.animate = animate;
        this.host = host;
        hero = panel.findViewById(R.id.titleHero);
        heroName = panel.findViewById(R.id.titleHeroName);
        heroMeta = panel.findViewById(R.id.titleHeroMeta);
        heroSummary = panel.findViewById(R.id.titleHeroSummary);
        openButton = panel.findViewById(R.id.titleHeroOpen);
        trailerButton = panel.findViewById(R.id.titleHeroTrailer);
        empty = panel.findViewById(R.id.titleEmpty);
        row = panel.findViewById(R.id.titleRow);

        row.setLayoutManager(new LinearLayoutManager(panel.getContext(), LinearLayoutManager.HORIZONTAL, false));
        row.setItemAnimator(null);   // whole lists are swapped, never animated item by item
        row.setHasFixedSize(true);
        row.setAdapter(adapter);

        openButton.setOnClickListener(v -> withCurrent(host::openItem));
        trailerButton.setOnClickListener(v -> withCurrent(host::playTrailer));
        View.OnKeyListener heroKeys = (v, keyCode, event) -> onHeroKey(v, keyCode, event);
        openButton.setOnKeyListener(heroKeys);
        trailerButton.setOnKeyListener(heroKeys);
        View.OnFocusChangeListener heroFocus = (v, focused) -> {
            if (focused) focusInHero = true;
        };
        openButton.setOnFocusChangeListener(heroFocus);
        trailerButton.setOnFocusChangeListener(heroFocus);
    }

    /** Shows a tab's titles: from the cache when they were loaded before, otherwise loaded in the background. */
    void show(int newTab) {
        if (newTab == tab) return;
        tab = newTab;
        focusInHero = false;
        List<CatalogItem> items = cache.get(newTab);
        if (items != null) {
            setItems(items);
            return;
        }
        setItems(Collections.emptyList());
        empty.setVisibility(View.GONE);   // loading, not empty
        load(newTab);
    }

    /**
     * New titles arrived (the companion app finished a sync): forget every tab's list and load the visible one
     * again. The old cards stay on screen until the new ones are ready, and keep focus thanks to stable ids.
     */
    void reload() {
        cache.clear();
        if (NavTabs.isTab(tab)) load(tab);
    }

    /** Down from the nav bar: the hero's Open in Stremio button. False when the tab has no titles yet. */
    boolean focusHero() {
        if (current < 0) return false;
        return openButton.requestFocus();
    }

    /** Back from an app: where focus was, a hero button or the card. False when the tab has no titles. */
    boolean restoreFocus() {
        if (current < 0) return false;
        if (focusInHero) return openButton.requestFocus();
        focusCard(current);
        return true;
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

    // A tab switch replaces the whole list with another tab's titles, so there are no finer change events to send.
    @SuppressLint("NotifyDataSetChanged")
    private void setItems(List<CatalogItem> items) {
        adapter.items = items;
        adapter.notifyDataSetChanged();
        boolean has = !items.isEmpty();
        empty.setVisibility(has ? View.GONE : View.VISIBLE);
        hero.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
        current = has ? TitleRowLayout.clamp(remembered[tab], items.size()) : -1;
        if (has) {
            row.scrollToPosition(current);
            showHero(items.get(current));
        } else {
            host.onItemShown(null);
        }
    }

    private void showHero(CatalogItem item) {
        heroName.setText(item.title);
        String meta = MetaLine.compose(item.year, item.genres, item.runtime);
        heroMeta.setText(meta);
        heroMeta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
        heroSummary.setText(item.summary);
        heroSummary.setVisibility(item.summary.isEmpty() ? View.GONE : View.VISIBLE);
        boolean trailer = YouTubeLinks.watch(item.trailerId) != null;
        if (!trailer && trailerButton.isFocused()) openButton.requestFocus();
        trailerButton.setVisibility(trailer ? View.VISIBLE : View.GONE);
        host.onItemShown(item);
    }

    private void withCurrent(java.util.function.Consumer<CatalogItem> action) {
        if (current >= 0 && current < adapter.items.size()) action.accept(adapter.items.get(current));
    }

    private void focusCard(int position) {
        RecyclerView.ViewHolder vh = row.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            vh.itemView.requestFocus();
            return;
        }
        // Not laid out (scrolled away): bring it in, then focus it.
        row.scrollToPosition(position);
        row.post(() -> {
            RecyclerView.ViewHolder later = row.findViewHolderForAdapterPosition(position);
            if (later != null) later.itemView.requestFocus();
        });
    }

    private void onCardFocused(int position) {
        if (position < 0 || position >= adapter.items.size()) return;
        focusInHero = false;
        remembered[tab] = position;
        if (position == current) return;
        current = position;
        showHero(adapter.items.get(position));
    }

    private boolean onCardKey(Holder holder, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                openButton.requestFocus();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;   // nothing under the row
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                int next = TitleRowLayout.step(holder.getBindingAdapterPosition(), adapter.getItemCount(),
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
                if (next != TitleRowLayout.NONE) focusCard(next);
                return true;   // the row's ends stop here; the focus finder never jumps out of it
            }
            default:
                return false;
        }
    }

    private boolean onHeroKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                host.onLeaveUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (current >= 0) focusCard(current);
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

    private void setFocusedLook(View card, boolean focused) {
        float scale = focused ? FOCUS_SCALE : 1f;
        if (animate) card.animate().scaleX(scale).scaleY(scale).setDuration(ANIM_MS).start();
        else {
            card.setScaleX(scale);
            card.setScaleY(scale);
        }
    }

    private final class Adapter extends RecyclerView.Adapter<Holder> {
        List<CatalogItem> items = Collections.emptyList();

        Adapter() {
            // Ids from the titles: a reload that keeps a title keeps its card, and the focus on it.
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id.hashCode();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_card, parent, false);
            Holder holder = new Holder(v);
            v.setClipToOutline(true);
            v.setOnFocusChangeListener((card, focused) -> {
                setFocusedLook(card, focused);
                if (focused) onCardFocused(holder.getBindingAdapterPosition());
            });
            v.setOnKeyListener((card, keyCode, event) -> onCardKey(holder, keyCode, event));
            v.setOnClickListener(card -> {
                int position = holder.getBindingAdapterPosition();
                if (position >= 0 && position < items.size()) host.openItem(items.get(position));
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            CatalogItem item = items.get(position);
            holder.itemView.setContentDescription(item.title);
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(TitleRowLayout.placeholderColor(item.title)));
            bindPoster(holder, item);
            float scale = holder.itemView.isFocused() ? FOCUS_SCALE : 1f;
            holder.itemView.setScaleX(scale);
            holder.itemView.setScaleY(scale);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * The poster from memory when it is there, otherwise the title on its colour while the poster decodes in the
         * background. The poster's tag holds the URI it is waiting for, so a card rebound to another title in the
         * meantime ignores the late result.
         */
        private void bindPoster(Holder holder, CatalogItem item) {
            String uri = item.posterUri;
            holder.poster.setTag(uri);
            Bitmap cached = uri == null ? null : posters.cached(uri);
            if (cached != null) {
                showPoster(holder, cached);
                return;
            }
            holder.poster.setImageDrawable(null);
            holder.placeholder.setText(item.title);
            holder.placeholder.setVisibility(View.VISIBLE);
            if (uri == null) return;
            posters.load(uri, bitmap -> {
                if (bitmap != null && uri.equals(holder.poster.getTag())) showPoster(holder, bitmap);
            });
        }

        private void showPoster(Holder holder, Bitmap bitmap) {
            holder.poster.setImageBitmap(bitmap);
            holder.placeholder.setVisibility(View.GONE);
        }
    }

    private static final class Holder extends RecyclerView.ViewHolder {
        final ImageView poster;
        final TextView placeholder;

        Holder(View v) {
            super(v);
            poster = v.findViewById(R.id.titlePoster);
            placeholder = v.findViewById(R.id.titlePlaceholder);
        }
    }
}
