package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Plain JVM tests for the hero's line of details. */
public class MetaLineTest {

    @Test
    public void everyKnownPartInOrderWithTheRatingLast() {
        assertEquals("2010 · Drama, Thriller · 108 min · ★ 4.2",
                MetaLine.compose("2010", "Drama, Thriller", "108 min", "4.2"));
    }

    @Test
    public void unknownPartsLeaveNoStraySeparator() {
        assertEquals("2010 · 108 min", MetaLine.compose("2010", "", "108 min", ""));
        assertEquals("Drama", MetaLine.compose(null, " Drama ", "  ", null));
        assertEquals("★ 3.4", MetaLine.compose("", null, "", " 3.4 "));
        assertEquals("", MetaLine.compose("", null, "", ""));
    }
}
