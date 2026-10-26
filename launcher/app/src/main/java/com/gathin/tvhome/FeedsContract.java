package com.gathin.tvhome;

/**
 * What the launcher reads from the companion app (cleantv-feeds), which fetches the Stremboxd catalogs so the
 * launcher never touches the network. A copy of the app's FeedsContract.kt: the tests on both sides pin the same
 * names, so a change on one side fails a test instead of leaving the tabs empty.
 */
final class FeedsContract {

    static final String PACKAGE = "com.codebymaribel.feeds";
    static final String AUTHORITY = "com.codebymaribel.feeds.provider";
    /** Signature permission both apps declare: only apps signed with the same key read the feeds. */
    static final String PERMISSION = "com.codebymaribel.feeds.permission.READ_FEEDS";

    static final String PATH_FEED = "feed";
    static final String PATH_POSTER = "poster";
    static final String PATH_BACKDROP = "backdrop";

    static final String COLUMN_TYPE = "type";
    static final String COLUMN_ID = "id";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_SUMMARY = "summary";
    /** 1 when the title has a poster saved, 0 when the card draws its placeholder. */
    static final String COLUMN_HAS_POSTER = "has_poster";
    /** 1 when the title has a backdrop saved. */
    static final String COLUMN_HAS_BACKDROP = "has_backdrop";

    static final String[] COLUMNS = {
            COLUMN_TYPE, COLUMN_ID, COLUMN_TITLE, COLUMN_SUMMARY, COLUMN_HAS_POSTER, COLUMN_HAS_BACKDROP};

    private FeedsContract() {
    }

    /** content://AUTHORITY, which the launcher watches for a finished sync. */
    static String root() {
        return "content://" + AUTHORITY;
    }

    /** A tab's titles. */
    static String feed(int tab) {
        return root() + "/" + PATH_FEED + "/" + tab;
    }

    /** One title's poster in a tab (the same title can have a different poster in another tab). */
    static String poster(int tab, String id) {
        return root() + "/" + PATH_POSTER + "/" + tab + "/" + id;
    }

    /** One title's backdrop, the same in every tab. */
    static String backdrop(String id) {
        return root() + "/" + PATH_BACKDROP + "/" + id;
    }
}
