package com.gathin.tvhome;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for the sample titles the carousel is built with. */
public class FakeCatalogSourceTest {

    private final CatalogSource source = new FakeCatalogSource();

    @Test
    public void everyListTabHasTitles() {
        assertFalse(source.load(NavTabs.POPULAR).isEmpty());
        assertFalse(source.load(NavTabs.WATCHLIST).isEmpty());
        assertFalse(source.load(NavTabs.FRIENDS).isEmpty());
    }

    @Test
    public void homeHasNone() {
        assertTrue(source.load(NavTabs.HOME).isEmpty());
        assertTrue(source.load(42).isEmpty());
    }

    @Test
    public void everyTitleCanOpenInStremioAndHasASummary() {
        for (int tab = NavTabs.POPULAR; tab < NavTabs.COUNT; tab++) {
            List<CatalogItem> items = source.load(tab);
            for (CatalogItem item : items) {
                assertTrue(item.id, item.id.matches("tt\\d{7,8}"));
                assertTrue(item.title, StremioLinks.detail(item.type, item.id) != null);
                assertFalse(item.title, item.summary.isEmpty());
            }
        }
    }
}
