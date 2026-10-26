package com.gathin.tvhome;

import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * The nav bar (config.xml, nav_tabs): Home, Popular, Watchlist and Friends as pills where the clock was. A tab is
 * selected as soon as it takes focus, like Google TV, so the content under the bar follows the remote. The views
 * come from activity_main.xml and are built once; switching a tab only flips view states, so it costs no layout
 * pass and allocates nothing.
 */
final class NavBar {

    /** What the activity does when the bar asks. */
    interface Listener {
        /** A tab was selected (focused, or chosen in code): show its content. */
        void onTabSelected(int tab);

        /** Down or OK on a tab: move focus into its content. */
        void onTabEnter(int tab);

        /** Right past the last tab: move focus to the pills; false keeps it on the tab. */
        boolean onPastLastTab();
    }

    private final View[] tabs = new View[NavTabs.COUNT];
    private final Listener listener;
    private int selected = NavTabs.HOME;

    NavBar(@NonNull ViewGroup bar, @NonNull Listener listener) {
        this.listener = listener;
        for (int i = 0; i < NavTabs.COUNT; i++) {
            View tab = bar.getChildAt(i);
            tabs[i] = tab;
            final int index = i;
            tab.setOnFocusChangeListener((v, focused) -> {
                if (focused) select(index);
            });
            tab.setOnKeyListener((v, keyCode, event) -> onKey(index, keyCode, event));
            tab.setOnClickListener(v -> listener.onTabEnter(selected));
        }
        tabs[selected].setSelected(true);
        bar.setVisibility(View.VISIBLE);
    }

    /** Selects a tab without moving focus; does nothing when it is already the selected one. */
    void select(int tab) {
        if (!NavTabs.isTab(tab) || tab == selected) return;
        tabs[selected].setSelected(false);
        selected = tab;
        tabs[tab].setSelected(true);
        listener.onTabSelected(tab);
    }

    /** Puts focus on the selected tab (which keeps it selected). */
    void focusSelected() {
        tabs[selected].requestFocus();
    }

    /** Puts focus on a tab, which selects it. */
    void focus(int tab) {
        if (NavTabs.isTab(tab)) tabs[tab].requestFocus();
    }

    int selected() {
        return selected;
    }

    boolean onHome() {
        return selected == NavTabs.HOME;
    }

    private boolean onKey(int index, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                return true;   // nothing above the bar
            case KeyEvent.KEYCODE_DPAD_DOWN:
                listener.onTabEnter(selected);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                boolean forward = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
                int next = NavTabs.step(index, forward);
                if (next != NavTabs.NONE) {
                    tabs[index].playSoundEffect(forward
                            ? SoundEffectConstants.NAVIGATION_RIGHT : SoundEffectConstants.NAVIGATION_LEFT);
                    tabs[next].requestFocus();
                    return true;
                }
                // Left of the first tab: nothing. Right of the last: the pills, if the activity takes it.
                return !forward || listener.onPastLastTab();
            }
            default:
                return false;
        }
    }
}
