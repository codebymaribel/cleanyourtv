package com.gathin.tvhome;

/**
 * How often Home may ask the companion app for a sync: asking wakes its process, and coming back from an app happens
 * many times an hour. The app itself only syncs when its data is 30 minutes old. Plain Java, for the JVM tests.
 */
final class RefreshGate {

    static final long MIN_GAP_MS = 5L * 60 * 1000;
    /** No ask yet. */
    static final long NEVER = Long.MIN_VALUE;

    private RefreshGate() {
    }

    /** True when Home may ask again, on a clock that only moves forward (elapsedRealtime). */
    static boolean due(long nowMs, long lastAskMs) {
        return lastAskMs == NEVER || nowMs < lastAskMs || nowMs - lastAskMs >= MIN_GAP_MS;
    }
}
