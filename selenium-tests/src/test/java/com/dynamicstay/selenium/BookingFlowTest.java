package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.BookingPage;
import com.dynamicstay.selenium.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end coverage of the primary vertical slice: search rates → select a
 * room → book it → see the confirmation reflected in the Recent Bookings
 * table. This is the single flow the whole project is built to demonstrate.
 */
class BookingFlowTest extends BaseTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

    @Test
    @DisplayName("Manager can recalculate a rate, then a guest can complete a booking end-to-end")
    void fullBookingFlowSucceeds() {
        DashboardPage dashboard = new DashboardPage(driver, wait);
        dashboard.waitForRoomsLoaded();

        // Step 1 — search / recalculate a rate for a room a week out
        String checkIn = LocalDate.now().plusDays(10).format(ISO);
        String checkOut = LocalDate.now().plusDays(12).format(ISO);

        dashboard.selectFirstAvailableRoomForQuote()
                .setQuoteDates(checkIn, checkOut)
                .submitQuote();

        String quoteText = dashboard.waitForQuoteResultText();
        assertFalse(dashboard.isQuoteResultError(), "Expected a successful quote, got: " + quoteText);
        assertTrue(quoteText.contains("night"), "Quote result should show a per-night price");

        // Step 2 — select a room and complete the booking form
        BookingPage bookingPage = new BookingPage(driver, wait);
        String uniqueEmail = "selenium.tester+" + System.currentTimeMillis() + "@example.com";

        bookingPage.selectFirstAvailableRoom()
                .fillGuestDetails("Selenium Tester", uniqueEmail)
                .setDates(checkIn, checkOut)
                .submit();

        // Step 3 — confirm the booking succeeded
        String resultText = bookingPage.waitForResultText();
        assertFalse(bookingPage.isResultError(), "Expected booking to succeed, got: " + resultText);
        assertTrue(resultText.contains("confirmed"), "Result text should confirm the booking");

        // Step 4 — the new booking should now be reflected in Recent Bookings
        String firstRow = bookingPage.getFirstBookingRowText();
        assertTrue(firstRow.contains("Selenium Tester"),
                "Newest booking row should show the guest we just booked");
    }
}
