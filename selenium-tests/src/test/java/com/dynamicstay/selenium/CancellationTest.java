package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.BookingPage;
import com.dynamicstay.selenium.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Run with -Ddynamicstay.username=admin and the matching admin password. */
class CancellationTest extends BaseTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

    @Test
    @DisplayName("Admin can cancel a confirmed booking from the dashboard")
    void adminCanCancelBooking() {
        Assumptions.assumeTrue("admin".equals(USERNAME),
            "Run this scenario with dynamicstay.username=admin");
        String checkIn = LocalDate.now().plusDays(60).format(ISO);
        String checkOut = LocalDate.now().plusDays(62).format(ISO);

        BookingPage bookingPage = new BookingPage(driver, wait);
        bookingPage
                .selectFirstAvailableRoom()
                .fillGuestDetails("Cancellation Tester", "cancel.selenium+" + System.currentTimeMillis() + "@example.com")
                .setDates(checkIn, checkOut)
                .submit();

        String bookingResult = bookingPage.waitForResultText();
        assertTrue(!bookingPage.isResultError(), "Setup booking should succeed: " + bookingResult);

        DashboardPage dashboard = new DashboardPage(driver, wait);
        dashboard.cancelFirstBooking();
        assertTrue(dashboard.waitForBookingResultText().contains("cancelled"));
    }
}