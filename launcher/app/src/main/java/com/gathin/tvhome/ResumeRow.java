package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
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
 * Home's Continue Watching row (resume_row), under the dock: the latest titles of the Stremio library as 16:9 cards
 * with their backdrop, name, episode and a progress bar. Up goes back to the dock; OK resumes the title in Stremio.
 * Shown only on the Home tab, and only when there is something to resume.
 */
final class ResumeRow {

    /** What the activity does when the row asks. */
    interface Host {
        /** Up from a card: back to the dock. */
        void onLeaveUp();

        /** OK on a card. */
        void open(@NonNull ResumeItem item);

        /** A card took focus. */
        void onFocused();

        /** The row appeared (true) or went away (false): the dock makes room for it, or takes it back. */
        void onShown(boolean shown);
    }

    private static final float FOCUS_SCALE = 1.08f;
    private static final long ANIM_MS = 160;

    private final View container;
    private final RecyclerView row;
    private final ProviderResumeSource source;
    private final PosterLoader images;
    private final boolean animate;
    private final Host host;
    private final Adapter adapter = new Adapter();
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private boolean onHome = true;
    private boolean shown;
    private int remembered;

    ResumeRow(@NonNull View container, @NonNull ProviderResumeSource source, @NonNull PosterLoader images,
              boolean animate, @NonNull Host host) {
        this.container = container;
        this.source = source;
        this.images = images;
        this.animate = animate;
        this.host = host;
        row = container.findViewById(R.id.resumeRow);
        row.setLayoutManager(new LinearLayoutManager(container.getContext(), LinearLayoutManager.HORIZONTAL, false));
        row.setItemAnimator(null);
        row.setHasFixedSize(true);
        row.setAdapter(adapter);
    }

    /** Loads the titles in the background, then shows or hides the row. */
    void reload() {
        loader.execute(() -> {
            List<ResumeItem> loaded = source.load();
            main.post(() -> setItems(loaded));
        });
    }

    /** The row lives on the Home tab only. */
    void setOnHome(boolean home) {
        onHome = home;
        updateVisibility();
    }

    /** Down from the dock: the card it was last on. False when the row is not showing. */
    boolean focus() {
        if (!isShowing()) return false;
        focusCard(TitleRowLayout.clamp(remembered, adapter.getItemCount()));
        return true;
    }

    /** Back from Stremio: the card that opened it. False when the row is not showing. */
    boolean restoreFocus() {
        return focus();
    }

    void shutdown() {
        loader.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    private boolean isShowing() {
        return onHome && adapter.getItemCount() > 0;
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")   // the whole list is replaced after a sync
    private void setItems(List<ResumeItem> items) {
        adapter.items = items;
        adapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        boolean show = isShowing();
        container.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show != shown) {
            shown = show;
            host.onShown(show);
        }
    }

    private void focusCard(int position) {
        RecyclerView.ViewHolder vh = row.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            vh.itemView.requestFocus();
            return;
        }
        row.scrollToPosition(position);
        row.post(() -> {
            RecyclerView.ViewHolder later = row.findViewHolderForAdapterPosition(position);
            if (later != null) later.itemView.requestFocus();
        });
    }

    private boolean onCardKey(Holder holder, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                host.onLeaveUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;   // nothing under the row
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                int next = TitleRowLayout.step(holder.getBindingAdapterPosition(), adapter.getItemCount(),
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
                if (next != TitleRowLayout.NONE) focusCard(next);
                return true;
            }
            default:
                return false;
        }
    }

    private void setFocusedLook(View card, boolean focused) {
        float scale = focused ? FOCUS_SCALE : 1f;
        card.setActivated(focused);   // the white ring (resume_card_ring)
        if (animate) card.animate().scaleX(scale).scaleY(scale).setDuration(ANIM_MS).start();
        else {
            card.setScaleX(scale);
            card.setScaleY(scale);
        }
    }

    private final class Adapter extends RecyclerView.Adapter<Holder> {
        List<ResumeItem> items = Collections.emptyList();

        Adapter() {
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id.hashCode();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resume_card, parent, false);
            Holder holder = new Holder(v);
            v.setClipToOutline(true);
            v.setOnFocusChangeListener((card, focused) -> {
                setFocusedLook(card, focused);
                if (focused) {
                    remembered = holder.getBindingAdapterPosition();
                    host.onFocused();
                }
            });
            v.setOnKeyListener((card, keyCode, event) -> onCardKey(holder, keyCode, event));
            v.setOnClickListener(card -> {
                int position = holder.getBindingAdapterPosition();
                if (position >= 0 && position < items.size()) host.open(items.get(position));
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ResumeItem item = items.get(position);
            holder.itemView.setContentDescription(item.title);
            holder.title.setText(item.title);
            String episode = ResumeText.episode(item.videoId);
            holder.episode.setText(episode);
            holder.episode.setVisibility(episode.isEmpty() ? View.GONE : View.VISIBLE);
            float fill = ResumeText.fraction(item.progress);
            holder.progress.setScaleX(fill);
            holder.progressTrack.setVisibility(fill > 0f ? View.VISIBLE : View.GONE);
            bindBackdrop(holder, item);
            boolean focused = holder.itemView.isFocused();
            holder.itemView.setActivated(focused);
            holder.itemView.setScaleX(focused ? FOCUS_SCALE : 1f);
            holder.itemView.setScaleY(focused ? FOCUS_SCALE : 1f);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void bindBackdrop(Holder holder, ResumeItem item) {
            String uri = item.backdropUri;
            holder.image.setTag(uri);
            holder.image.setImageDrawable(null);
            if (uri == null) return;
            Bitmap cached = images.cached(uri);
            if (cached != null) {
                holder.image.setImageBitmap(cached);
                return;
            }
            images.load(uri, bitmap -> {
                if (bitmap != null && uri.equals(holder.image.getTag())) holder.image.setImageBitmap(bitmap);
            });
        }
    }

    private static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView episode;
        final View progressTrack;
        final View progress;

        Holder(View v) {
            super(v);
            image = v.findViewById(R.id.resumeImage);
            title = v.findViewById(R.id.resumeTitle);
            episode = v.findViewById(R.id.resumeEpisode);
            progressTrack = v.findViewById(R.id.resumeProgressTrack);
            progress = v.findViewById(R.id.resumeProgress);
        }
    }
}
