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
    static final String PATH_RESUME = "resume";

    static final String COLUMN_TYPE = "type";
    static final String COLUMN_ID = "id";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_SUMMARY = "summary";
    /** 1 when the title has a poster saved, 0 when the card draws its placeholder. */
    static final String COLUMN_HAS_POSTER = "has_poster";
    /** 1 when the title has a backdrop saved. */
    static final String COLUMN_HAS_BACKDROP = "has_backdrop";
    static final String COLUMN_YEAR = "year";
    /** Up to three genres, already joined with ", ". */
    static final String COLUMN_GENRES = "genres";
    static final String COLUMN_RUNTIME = "runtime";
    /** The trailer's YouTube video id, "" when there is none. */
    static final String COLUMN_TRAILER = "trailer";

    /** The video to resume: the movie's id, or "tt...:season:episode"; "" when unknown. */
    static final String COLUMN_VIDEO_ID = "video_id";
    /** How far in, 0 to 1000. */
    static final String COLUMN_PROGRESS = "progress";

    static final String[] RESUME_COLUMNS = {
            COLUMN_TYPE, COLUMN_ID, COLUMN_VIDEO_ID, COLUMN_TITLE, COLUMN_PROGRESS, COLUMN_HAS_BACKDROP};

    /** ContentResolver.call method: the app syncs when its data is older than 30 minutes. */
    static final String METHOD_SYNC_IF_STALE = "sync_if_stale";

    static final String[] COLUMNS = {
            COLUMN_TYPE, COLUMN_ID, COLUMN_TITLE, COLUMN_SUMMARY, COLUMN_HAS_POSTER, COLUMN_HAS_BACKDROP,
            COLUMN_YEAR, COLUMN_GENRES, COLUMN_RUNTIME, COLUMN_TRAILER};

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

    /** The Stremio library's Continue Watching, latest first. */
    static String resume() {
        return root() + "/" + PATH_RESUME;
    }

    /** One title's backdrop, the same in every tab. */
    static String backdrop(String id) {
        return root() + "/" + PATH_BACKDROP + "/" + id;
    }
}
