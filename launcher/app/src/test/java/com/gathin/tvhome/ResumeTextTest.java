package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for the words and numbers on a Continue Watching card. */
public class ResumeTextTest {

    @Test
    public void aSeriesVideoIdNamesItsEpisode() {
        assertEquals("T2 · E5", ResumeText.episode("tt0903747:2:5"));
        assertEquals("T10 · E12", ResumeText.episode("tt0903747:010:12"));
    }

    @Test
    public void aMovieOrAnythingElseNamesNothing() {
        assertEquals("", ResumeText.episode("tt0947798"));
        assertEquals("", ResumeText.episode(null));
        assertEquals("", ResumeText.episode("tt1:x:2"));
        assertEquals("", ResumeText.episode("tt1:1:2:3"));
    }

    @Test
    public void progressIsAFractionKeptInRange() {
        assertEquals(0.446f, ResumeText.fraction(446), 0.0001f);
        assertEquals(1f, ResumeText.fraction(5000), 0f);
        assertEquals(0f, ResumeText.fraction(-3), 0f);
    }
}
