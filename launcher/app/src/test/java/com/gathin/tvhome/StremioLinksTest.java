package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Plain JVM tests for Stremio's deep links. */
public class StremioLinksTest {

    @Test
    public void aMovieOpensOnItsStreams() {
        assertEquals("stremio:///detail/movie/tt0947798/tt0947798", StremioLinks.detail("movie", "tt0947798"));
    }

    @Test
    public void aSeriesOpensOnItsEpisodeList() {
        assertEquals("stremio:///detail/series/tt0903747", StremioLinks.detail("series", "tt0903747"));
    }

    @Test
    public void partsAreTrimmedAndEncoded() {
        assertEquals("stremio:///detail/movie/tt1/tt1", StremioLinks.detail(" movie ", " tt1 "));
        assertEquals("stremio:///detail/movie/a%20b%2Fc/a%20b%2Fc", StremioLinks.detail("movie", "a b/c"));
    }

    @Test
    public void noLinkWithoutATypeOrAnId() {
        assertNull(StremioLinks.detail(null, "tt1"));
        assertNull(StremioLinks.detail("movie", null));
        assertNull(StremioLinks.detail("movie", "  "));
        assertNull(StremioLinks.detail("", "tt1"));
    }
}
