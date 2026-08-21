package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms that recalculating a rate for two very different date scenarios
 * (far-out / low-urgency vs. last-minute / high-occupancy) produces two
 * different displayed prices — i.e. the dynamic pricing engine's output is
 * actually wired through to the UI, not a static number.
 */
class PriceUpdateTest extends BaseTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$(\\d+\\.\\d{2})\\s*/\\s*night");

    @Test
    @DisplayName("Recalculating for a last-minute stay changes the displayed price vs. a far-out stay")
    void priceUpdatesReflectInUiAfterRecalculation() {
        DashboardPage dashboard = new DashboardPage(driver, wait);
        dashboard.waitForRoomsLoaded();

        // Far-out, low-urgency quote (30 days out)
        String farCheckIn = LocalDate.now().plusDays(30).format(ISO);
        String farCheckOut = LocalDate.now().plusDays(32).format(ISO);

        dashboard.selectFirstAvailableRoomForQuote()
                .setQuoteDates(farCheckIn, farCheckOut)
                .submitQuote();
        String farQuoteText = dashboard.waitForQuoteResultText();
        assertFalse(dashboard.isQuoteResultError(), "Far-out quote should succeed: " + farQuoteText);
        double farPricePerNight = extractPricePerNight(farQuoteText);

        // Last-minute quote (tomorrow) for the same room
        String soonCheckIn = LocalDate.now().plusDays(1).format(ISO);
        String soonCheckOut = LocalDate.now().plusDays(2).format(ISO);

        dashboard.selectFirstAvailableRoomForQuote()
                .setQuoteDates(soonCheckIn, soonCheckOut)
                .submitQuote();
        String soonQuoteText = dashboard.waitForQuoteResultText();
        assertFalse(dashboard.isQuoteResultError(), "Last-minute quote should succeed: " + soonQuoteText);
        double soonPricePerNight = extractPricePerNight(soonQuoteText);

        assertNotEquals(farPricePerNight, soonPricePerNight, 0.001,
                "Expected a different per-night price for a last-minute stay vs. a far-out stay");
        assertTrue(soonQuoteText.contains("LAST_MINUTE") || soonQuoteText.contains("Strategy"),
                "Last-minute quote should surface which strategy was applied");
    }

    private double extractPricePerNight(String quoteText) {
        Matcher matcher = PRICE_PATTERN.matcher(quoteText);
        assertTrue(matcher.find(), "Could not find a '$X.XX / night' price in: " + quoteText);
        return Double.parseDouble(matcher.group(1));
    }
}
