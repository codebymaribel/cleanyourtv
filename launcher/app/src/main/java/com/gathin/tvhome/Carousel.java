package com.gathin.tvhome;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A nav tab's carousel (nav_tabs): posters in a row, the focused one bigger and tinted blue on a fixed keyline at the
 * left, its title and summary in a panel beside it. The cards after the focused one slide right to make room for the
 * panel through translationX, a property change on the GPU, never a layout pass. Titles come from a {@link
 * CatalogSource} on a background thread and are kept per tab, so moving between tabs loads each list once; posters
 * come from a {@link PosterLoader}, also off the main thread.
 */
final class Carousel {

    /** What the activity does when the carousel asks. */
    interface Host {
        /** Up from a card: back to the nav bar. */
        void onCarouselUp();

        /** OK on a card: open its title. */
        void openItem(@NonNull CatalogItem item);
    }

    /** Blue over the focused card's poster. */
    private static final int TINT = 0x4D2E6BFF;
    private static final long ANIM_MS = 180;

    private final RecyclerView list;
    private final View detail;
    private final TextView detailTitle;
    private final TextView detailSummary;
    private final View empty;
    private final Host host;
    private final CatalogSource source;
    private final PosterLoader posters;
    private final boolean animate;
    private final Drawable tint = new ColorDrawable(TINT);
    private final Adapter adapter = new Adapter();
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final SparseArray<List<CatalogItem>> cache = new SparseArray<>();
    private final int[] remembered = new int[NavTabs.COUNT];
    private final int keyline;
    private final int cardWidth;
    private final int spacing;
    private final int room;

    private int tab = NavTabs.NONE;
    /** The card whose title the panel shows; the cards after it are shifted. -1 when the list is empty. */
    private int current = -1;

    Carousel(@NonNull View panel, @NonNull CatalogSource source, @NonNull PosterLoader posters, boolean animate,
             @NonNull Host host) {
        this.source = source;
        this.posters = posters;
        this.animate = animate;
        this.host = host;
        Resources res = panel.getResources();
        keyline = res.getDimensionPixelSize(R.dimen.carousel_keyline);
        cardWidth = res.getDimensionPixelSize(R.dimen.carousel_card_width);
        spacing = res.getDimensionPixelSize(R.dimen.carousel_spacing);
        int gap = res.getDimensionPixelSize(R.dimen.carousel_gap);
        room = CarouselLayout.detailRoom(res.getDimensionPixelSize(R.dimen.carousel_detail_width), gap, spacing, cardWidth);

        list = panel.findViewById(R.id.carousel);
        detail = panel.findViewById(R.id.carouselDetail);
        detailTitle = panel.findViewById(R.id.carouselTitle);
        detailSummary = panel.findViewById(R.id.carouselSummary);
        empty = panel.findViewById(R.id.carouselEmpty);

        list.setLayoutManager(new LinearLayoutManager(panel.getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child,
                                                         @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
                return false;   // the carousel scrolls the focused card onto the keyline itself
            }
        });
        list.setItemAnimator(null);   // whole lists are swapped, never animated item by item
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
        // A card scrolled into view takes the shift for where it sits, so the gap for the panel never moves.
        list.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View v) {
                v.setTranslationX(CarouselLayout.shiftFor(list.getChildAdapterPosition(v), current, room));
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View v) {
                v.animate().cancel();
            }
        });
        // The last card can only reach its keyline (the peek one, further right) with room after it.
        int lastKeyline = CarouselLayout.keylineFor(1, keyline, cardWidth, spacing);
        list.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            int end = CarouselLayout.endPadding(r - l, lastKeyline, cardWidth);
            if (list.getPaddingEnd() != end) list.setPaddingRelative(keyline, list.getPaddingTop(), end, list.getPaddingBottom());
        });
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) detail.getLayoutParams();
        // Beside the first card at its full focused width; from the second card on, the panel slides right with it.
        lp.setMarginStart(keyline + cardWidth + CarouselLayout.focusGrowth(cardWidth) + gap);
        detail.setLayoutParams(lp);
    }

    /** Shows a tab's titles: from the cache when they were loaded before, otherwise loaded in the background. */
    void show(int newTab) {
        if (newTab == tab) return;
        tab = newTab;
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

    private void load(int forTab) {
        loader.execute(() -> {
            List<CatalogItem> loaded = source.load(forTab);
            main.post(() -> {
                cache.put(forTab, loaded);
                if (tab == forTab) setItems(loaded);
            });
        });
    }

    /** Focus the card this tab was last on; false when there is nothing to focus yet. */
    boolean focus() {
        if (!NavTabs.isTab(tab) || adapter.getItemCount() == 0) return false;
        int position = CarouselLayout.clamp(remembered[tab], adapter.getItemCount());
        focusPosition(position);
        return true;
    }

    /** Stops the background loader; the activity calls it when it goes away. */
    void shutdown() {
        loader.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    // A tab switch replaces the whole list with another tab's titles, so there are no finer change events to send.
    @SuppressLint("NotifyDataSetChanged")
    private void setItems(List<CatalogItem> items) {
        adapter.items = items;
        adapter.notifyDataSetChanged();
        boolean has = !items.isEmpty();
        empty.setVisibility(has ? View.GONE : View.VISIBLE);
        detail.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
        int position = has ? CarouselLayout.clamp(remembered[tab], items.size()) : -1;
        current = position;
        if (has) {
            ((LinearLayoutManager) list.getLayoutManager()).scrollToPositionWithOffset(position, offsetFor(position));
            detail.setTranslationX(offsetFor(position));
            showDetail(items.get(position), false);
        }
    }

    private void focusPosition(int position) {
        RecyclerView.ViewHolder vh = list.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            vh.itemView.requestFocus();
            return;
        }
        // Not laid out yet (far off screen): put it on its keyline first, then focus it.
        ((LinearLayoutManager) list.getLayoutManager()).scrollToPositionWithOffset(position, offsetFor(position));
        list.post(() -> {
            RecyclerView.ViewHolder later = list.findViewHolderForAdapterPosition(position);
            if (later != null) later.itemView.requestFocus();
        });
    }

    private void onCardFocused(int position, View card) {
        if (position < 0 || position >= adapter.getItemCount()) return;
        remembered[tab] = position;
        int dx = CarouselLayout.scrollToKeyline(card.getLeft(), keylineFor(position));
        if (dx != 0) {
            if (animate) list.smoothScrollBy(dx, 0);
            else list.scrollBy(dx, 0);
        }
        boolean moved = position != current;
        current = position;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            float shift = CarouselLayout.shiftFor(list.getChildAdapterPosition(child), current, room);
            if (animate) child.animate().translationX(shift).setDuration(ANIM_MS).start();
            else child.setTranslationX(shift);
        }
        float detailShift = offsetFor(position);
        if (animate) detail.animate().translationX(detailShift).setDuration(ANIM_MS).start();
        else detail.setTranslationX(detailShift);
        if (moved) showDetail(adapter.items.get(position), animate);
    }

    /** Where a card sits when it is the focused one: the base keyline for the first, the peek keyline after. */
    private int keylineFor(int position) {
        return CarouselLayout.keylineFor(position, keyline, cardWidth, spacing);
    }

    /** The same, from the list's start padding (the base keyline): what scrolling and the panel move by. */
    private int offsetFor(int position) {
        return keylineFor(position) - keyline;
    }

    private void showDetail(CatalogItem item, boolean fade) {
        detailTitle.setText(item.title);
        detailSummary.setText(item.summary);
        detailSummary.setVisibility(item.summary.isEmpty() ? View.GONE : View.VISIBLE);
        if (fade) {
            detail.setAlpha(0f);
            detail.animate().alpha(1f).setDuration(ANIM_MS).start();   // runs beside the slide, not instead of it
        } else {
            detail.setAlpha(1f);
        }
    }

    private void setFocusedLook(View card, boolean focused) {
        card.setForeground(focused ? tint : null);
        float scale = focused ? CarouselLayout.FOCUS_SCALE : 1f;
        if (animate) card.animate().scaleX(scale).scaleY(scale).setDuration(ANIM_MS).start();
        else {
            card.setScaleX(scale);
            card.setScaleY(scale);
        }
    }

    private boolean onCardKey(Holder holder, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        int position = holder.getBindingAdapterPosition();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                host.onCarouselUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;   // nothing under the carousel
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                int next = CarouselLayout.step(position, adapter.getItemCount(), keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
                if (next >= 0) focusPosition(next);
                return true;   // the ends stop here, and the focus finder never jumps out of the row
            }
            default:
                return false;
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_card, parent, false);
            Holder holder = new Holder(v);
            v.setClipToOutline(true);
            // Grow from the top-left corner: the focused card keeps its place on the keyline and the row's top line.
            v.setPivotX(0f);
            v.setPivotY(0f);
            v.setOnFocusChangeListener((card, focused) -> {
                setFocusedLook(card, focused);
                if (focused) onCardFocused(holder.getBindingAdapterPosition(), card);
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
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(CarouselLayout.placeholderColor(item.title)));
            bindPoster(holder, item);
            holder.itemView.setTranslationX(CarouselLayout.shiftFor(position, current, room));
            boolean focused = holder.itemView.isFocused();
            holder.itemView.setForeground(focused ? tint : null);
            holder.itemView.setScaleX(focused ? CarouselLayout.FOCUS_SCALE : 1f);
            holder.itemView.setScaleY(focused ? CarouselLayout.FOCUS_SCALE : 1f);
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
            poster = v.findViewById(R.id.carouselPoster);
            placeholder = v.findViewById(R.id.carouselPlaceholder);
        }
    }
}
