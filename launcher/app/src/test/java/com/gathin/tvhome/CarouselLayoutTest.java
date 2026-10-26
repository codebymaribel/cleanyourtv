package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Plain JVM tests for the carousel's geometry. */
public class CarouselLayoutTest {

    @Test
    public void aCardScrollsOntoTheKeyline() {
        assertEquals(300, CarouselLayout.scrollToKeyline(320, 20));
        assertEquals(-20, CarouselLayout.scrollToKeyline(0, 20));
        assertEquals(0, CarouselLayout.scrollToKeyline(20, 20));
    }

    @Test
    public void theDetailRoomCoversTheBiggerCardGapPanelGapLessTheNormalSpacing() {
        // 360 px card at 1.2 is 72 px wider.
        assertEquals(72, CarouselLayout.focusGrowth(360));
        assertEquals(72 + 720 + 48 + 48 - 32, CarouselLayout.detailRoom(720, 48, 32, 360));
        assertEquals(0, CarouselLayout.detailRoom(0, 0, 32, 0));
    }

    @Test
    public void theFirstCardSitsOnTheBaseKeyline() {
        assertEquals(20, CarouselLayout.keylineFor(0, 20, 360, 32));
    }

    @Test
    public void fromTheSecondCardAFifthOfThePreviousOneShows() {
        // 20% of a 360 px card is 72 px of poster, then the 32 px spacing before the focused card.
        assertEquals(72 + 32, CarouselLayout.keylineFor(1, 20, 360, 32));
        assertEquals(72 + 32, CarouselLayout.keylineFor(7, 20, 360, 32));
    }

    @Test
    public void thePeekNeverPutsACardLeftOfTheBaseKeyline() {
        assertEquals(200, CarouselLayout.keylineFor(3, 200, 360, 32));
    }

    @Test
    public void onlyCardsAfterTheFocusedOneShift() {
        assertEquals(0, CarouselLayout.shiftFor(2, 3, 500));
        assertEquals(0, CarouselLayout.shiftFor(3, 3, 500));
        assertEquals(500, CarouselLayout.shiftFor(4, 3, 500));
        assertEquals(500, CarouselLayout.shiftFor(10, 3, 500));
    }

    @Test
    public void nothingShiftsWithoutAFocusedCard() {
        assertEquals(0, CarouselLayout.shiftFor(4, -1, 500));
    }

    @Test
    public void theLastCardCanReachTheKeyline() {
        assertEquals(1920 - 20 - 320, CarouselLayout.endPadding(1920, 20, 320));
        assertEquals(0, CarouselLayout.endPadding(100, 20, 320));
    }

    @Test
    public void leftAndRightStopAtTheEnds() {
        assertEquals(1, CarouselLayout.step(0, 5, true));
        assertEquals(3, CarouselLayout.step(4, 5, false));
        assertEquals(-1, CarouselLayout.step(0, 5, false));
        assertEquals(-1, CarouselLayout.step(4, 5, true));
        assertEquals(-1, CarouselLayout.step(0, 0, true));
    }

    @Test
    public void aRememberedPositionStaysInsideTheList() {
        assertEquals(2, CarouselLayout.clamp(2, 5));
        assertEquals(4, CarouselLayout.clamp(9, 5));
        assertEquals(0, CarouselLayout.clamp(-3, 5));
        assertEquals(0, CarouselLayout.clamp(3, 0));
    }

    @Test
    public void aTitleAlwaysGetsTheSamePlaceholderColour() {
        int c = CarouselLayout.placeholderColor("Black Swan");
        assertEquals(c, CarouselLayout.placeholderColor("Black Swan"));
        boolean inPalette = false;
        for (int p : CarouselLayout.PLACEHOLDER_COLORS) inPalette |= p == c;
        assertTrue(inPalette);
        CarouselLayout.placeholderColor(null);   // no crash without a title
    }
}
