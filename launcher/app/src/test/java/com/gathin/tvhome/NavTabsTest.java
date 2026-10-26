package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for moving between the nav bar's tabs. */
public class NavTabsTest {

    @Test
    public void theBarIsHomePopularWatchlistFriendsInThatOrder() {
        assertEquals(0, NavTabs.HOME);
        assertEquals(1, NavTabs.POPULAR);
        assertEquals(2, NavTabs.WATCHLIST);
        assertEquals(3, NavTabs.FRIENDS);
        assertEquals(4, NavTabs.COUNT);
    }

    @Test
    public void rightAndLeftMoveOneTab() {
        assertEquals(NavTabs.POPULAR, NavTabs.step(NavTabs.HOME, true));
        assertEquals(NavTabs.FRIENDS, NavTabs.step(NavTabs.WATCHLIST, true));
        assertEquals(NavTabs.WATCHLIST, NavTabs.step(NavTabs.FRIENDS, false));
        assertEquals(NavTabs.HOME, NavTabs.step(NavTabs.POPULAR, false));
    }

    @Test
    public void theBarDoesNotWrapAtItsEnds() {
        assertEquals(NavTabs.NONE, NavTabs.step(NavTabs.HOME, false));
        assertEquals(NavTabs.NONE, NavTabs.step(NavTabs.FRIENDS, true));
    }

    @Test
    public void anUnknownIndexGoesNowhere() {
        assertEquals(NavTabs.NONE, NavTabs.step(-1, true));
        assertEquals(NavTabs.NONE, NavTabs.step(NavTabs.COUNT, false));
        assertEquals(NavTabs.NONE, NavTabs.back(99));
    }

    @Test
    public void backFromAnyTabGoesHomeAndFromHomeNowhere() {
        assertEquals(NavTabs.HOME, NavTabs.back(NavTabs.POPULAR));
        assertEquals(NavTabs.HOME, NavTabs.back(NavTabs.WATCHLIST));
        assertEquals(NavTabs.HOME, NavTabs.back(NavTabs.FRIENDS));
        assertEquals(NavTabs.NONE, NavTabs.back(NavTabs.HOME));
    }

    @Test
    public void onlyHomeShowsTheAppGrid() {
        assertTrue(NavTabs.showsGrid(NavTabs.HOME));
        assertFalse(NavTabs.showsGrid(NavTabs.POPULAR));
        assertFalse(NavTabs.showsGrid(NavTabs.WATCHLIST));
        assertFalse(NavTabs.showsGrid(NavTabs.FRIENDS));
    }
}
