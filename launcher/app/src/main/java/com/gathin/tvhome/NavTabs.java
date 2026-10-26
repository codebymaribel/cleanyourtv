package com.gathin.tvhome;

/**
 * The nav bar's tabs (config.xml, nav_tabs) and how the remote moves between them. Plain Java, no Android types,
 * so the JVM tests can check it without a TV; {@link NavBar} wires it to the views.
 */
final class NavTabs {

    /** Tab indexes, in bar order. Home shows the app grid; the others show a list from Stremio or Letterboxd. */
    static final int HOME = 0;
    static final int POPULAR = 1;
    static final int WATCHLIST = 2;
    static final int FRIENDS = 3;
    static final int COUNT = 4;

    /** Returned by {@link #step} and {@link #back} when there is no tab to go to. */
    static final int NONE = -1;

    private NavTabs() {
    }

    /** The tab one step left or right of {@code current}, or {@link #NONE} past either end of the bar. */
    static int step(int current, boolean forward) {
        if (!isTab(current)) return NONE;
        int next = forward ? current + 1 : current - 1;
        return isTab(next) ? next : NONE;
    }

    /** Back on a tab: any tab goes to Home; on Home there is nowhere to go ({@link #NONE}). */
    static int back(int current) {
        return isTab(current) && current != HOME ? HOME : NONE;
    }

    /** Only Home holds the app grid; the dock, the pills' Down and restoring focus use it. */
    static boolean showsGrid(int tab) {
        return tab == HOME;
    }

    static boolean isTab(int index) {
        return index >= 0 && index < COUNT;
    }
}
