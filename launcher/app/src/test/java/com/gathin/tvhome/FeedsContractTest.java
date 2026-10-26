package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/** Pins the names the companion app's FeedsContract.kt uses; FeedsContractTest.kt there pins the same values. */
public class FeedsContractTest {

    @Test
    public void theAuthorityPermissionAndPackageMatchTheApp() {
        assertEquals("com.codebymaribel.feeds", FeedsContract.PACKAGE);
        assertEquals("com.codebymaribel.feeds.provider", FeedsContract.AUTHORITY);
        assertEquals("com.codebymaribel.feeds.permission.READ_FEEDS", FeedsContract.PERMISSION);
    }

    @Test
    public void theColumnsMatchTheAppInOrder() {
        assertArrayEquals(new String[]{"type", "id", "title", "summary", "has_poster", "has_backdrop",
                "year", "genres", "runtime", "trailer"}, FeedsContract.COLUMNS);
    }

    @Test
    public void theResumePathAndColumnsMatchTheApp() {
        assertEquals("content://com.codebymaribel.feeds.provider/resume", FeedsContract.resume());
        assertArrayEquals(new String[]{"type", "id", "video_id", "title", "progress", "has_backdrop"},
                FeedsContract.RESUME_COLUMNS);
    }

    @Test
    public void theSyncRequestMethodMatchesTheApp() {
        assertEquals("sync_if_stale", FeedsContract.METHOD_SYNC_IF_STALE);
    }

    @Test
    public void urisFollowTheAppsPaths() {
        assertEquals("content://com.codebymaribel.feeds.provider", FeedsContract.root());
        assertEquals("content://com.codebymaribel.feeds.provider/feed/2", FeedsContract.feed(NavTabs.WATCHLIST));
        assertEquals("content://com.codebymaribel.feeds.provider/poster/3/tt0091064",
                FeedsContract.poster(NavTabs.FRIENDS, "tt0091064"));
        assertEquals("content://com.codebymaribel.feeds.provider/backdrop/tt0091064", FeedsContract.backdrop("tt0091064"));
    }
}
