package com.gathin.tvhome;

/**
 * The carousel's geometry, in pixels: the focused card sits on a fixed keyline at the left, the detail panel to its
 * right, and the cards after it shift right to make room. Plain Java, so the JVM tests can check it without a TV.
 */
final class CarouselLayout {

    /** The focused card's scale; it grows right and down from its top-left corner, so the posters' tops line up. */
    static final float FOCUS_SCALE = 1.2f;
    /** How much of the card before the focused one shows at the left, from the second card on. */
    static final float PEEK = 0.2f;

    /** Dark backgrounds for cards without a poster yet, picked by title so a card keeps its colour. */
    static final int[] PLACEHOLDER_COLORS = {0xFF1B2A4A, 0xFF2A1B4A, 0xFF1B4A42, 0xFF4A2A1B, 0xFF3A1B2E, 0xFF22303A};

    private CarouselLayout() {
    }

    /** How far to scroll so a card whose laid-out left edge is at {@code cardLeft} lands on the keyline. */
    static int scrollToKeyline(int cardLeft, int keyline) {
        return cardLeft - keyline;
    }

    /**
     * Where the focused card's left edge sits. The first card sits on the base keyline; from the second on, the card
     * moves right far enough that {@link #PEEK} of the one before shows, with the normal spacing between them.
     */
    static int keylineFor(int position, int base, int cardWidth, int spacing) {
        if (position <= 0) return base;
        return Math.max(base, Math.round(cardWidth * PEEK) + spacing);
    }

    /** How much wider the focused card is than the others. */
    static int focusGrowth(int cardWidth) {
        return Math.round(cardWidth * (FOCUS_SCALE - 1f));
    }

    /**
     * Room the detail panel needs after the focused card, on top of the normal spacing between cards: the focused
     * card's extra width, gap, panel, gap, minus the spacing the next card already has.
     */
    static int detailRoom(int panelWidth, int gap, int spacing, int cardWidth) {
        return Math.max(0, focusGrowth(cardWidth) + panelWidth + 2 * gap - spacing);
    }

    /** Horizontal shift for the card at {@code position}: cards after the focused one move right by {@code room}. */
    static int shiftFor(int position, int focused, int room) {
        return focused >= 0 && position > focused ? room : 0;
    }

    /** Padding after the last card, so it too can scroll onto the keyline. */
    static int endPadding(int listWidth, int keyline, int cardWidth) {
        return Math.max(0, listWidth - keyline - cardWidth);
    }

    /** The card one step left or right, or -1 at either end (the carousel does not wrap). */
    static int step(int position, int count, boolean forward) {
        int next = forward ? position + 1 : position - 1;
        return position >= 0 && position < count && next >= 0 && next < count ? next : -1;
    }

    /** A position to come back to, kept inside a list that may have shrunk; 0 when the list is empty. */
    static int clamp(int position, int count) {
        return count <= 0 ? 0 : Math.max(0, Math.min(position, count - 1));
    }

    /** The placeholder colour for a title: the same title always gets the same colour. */
    static int placeholderColor(String title) {
        int h = title == null ? 0 : title.hashCode();
        return PLACEHOLDER_COLORS[Math.floorMod(h, PLACEHOLDER_COLORS.length)];
    }
}
