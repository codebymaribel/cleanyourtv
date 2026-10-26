package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for the hero's line of details. */
public class MetaLineTest {

    @Test
    public void everyKnownPartInOrder() {
        assertEquals("2010 · Drama, Thriller · 108 min", MetaLine.compose("2010", "Drama, Thriller", "108 min"));
    }

    @Test
    public void unknownPartsLeaveNoStraySeparator() {
        assertEquals("2010 · 108 min", MetaLine.compose("2010", "", "108 min"));
        assertEquals("Drama", MetaLine.compose(null, " Drama ", "  "));
        assertEquals("", MetaLine.compose("", null, ""));
    }
}
