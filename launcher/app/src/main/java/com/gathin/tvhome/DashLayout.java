package com.gathin.tvhome;

/**
 * Where each dash of a nav tab's page indicator sits: one dash per title, the active one longer. Plain Java, so the
 * JVM tests can check it without a TV; {@link PageDashes} draws them.
 */
final class DashLayout {

    private DashLayout() {
    }

    /** The width of all the dashes and the gaps between them. */
    static float totalWidth(int count, float dash, float activeDash, float gap) {
        if (count <= 0) return 0f;
        return (count - 1) * (dash + gap) + activeDash;
    }

    /** The left edge of dash {@code index}: the dashes after the active one move right by its extra length. */
    static float left(int index, int active, float dash, float activeDash, float gap) {
        float x = index * (dash + gap);
        return active >= 0 && index > active ? x + (activeDash - dash) : x;
    }

    /** The width of dash {@code index}. */
    static float width(int index, int active, float dash, float activeDash) {
        return index == active ? activeDash : dash;
    }
}
