package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Plain JVM tests for trailer links. */
public class YouTubeLinksTest {

    @Test
    public void aVideoIdBecomesAWatchLink() {
        assertEquals("https://www.youtube.com/watch?v=5jaI1XOB-bs", YouTubeLinks.watch("5jaI1XOB-bs"));
    }

    @Test
    public void anythingElseGivesNoLink() {
        assertNull(YouTubeLinks.watch(null));
        assertNull(YouTubeLinks.watch(""));
        assertNull(YouTubeLinks.watch("short"));
        assertNull(YouTubeLinks.watch("5jaI1XOB-bs&list=x"));
    }
}
