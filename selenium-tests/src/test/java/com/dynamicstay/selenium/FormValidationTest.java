package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.BookingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers client-side and server-side validation: invalid date ranges and
 * double-booking the same room for overlapping dates.
 */
class FormValidationTest extends BaseTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

    @Test
    @DisplayName("Booking form rejects a check-out date on/before check-in")
    void rejectsInvalidDateRange() {
        BookingPage bookingPage = new BookingPage(driver, wait);

        String checkIn = LocalDate.now().plusDays(5).format(ISO);
        String checkOutBeforeCheckIn = LocalDate.now().plusDays(3).format(ISO);

        bookingPage.selectFirstAvailableRoom()
                .fillGuestDetails("Invalid Date Tester", "invalid.date@example.com")
                .setDates(checkIn, checkOutBeforeCheckIn)
                .submit();

        String resultText = bookingPage.waitForResultText();
        assertTrue(bookingPage.isResultError(),
                "Expected client-side validation to reject checkOut <= checkIn, got: " + resultText);
        assertTrue(resultText.toLowerCase().contains("after"),
                "Error message should explain check-out must be after check-in");
    }

    @Test
    @DisplayName("Booking the same room for overlapping dates twice returns a conflict")
    void rejectsOverbooking() {
        BookingPage bookingPage = new BookingPage(driver, wait);

        String checkIn = LocalDate.now().plusDays(15).format(ISO);
        String checkOut = LocalDate.now().plusDays(17).format(ISO);

        // First booking should succeed.
        bookingPage.selectFirstAvailableRoom()
                .fillGuestDetails("First Booker", "first.booker+" + System.currentTimeMillis() + "@example.com")
                .setDates(checkIn, checkOut)
                .submit();
        String firstResult = bookingPage.waitForResultText();
        assertTrue(!bookingPage.isResultError(), "Setup booking should succeed, got: " + firstResult);

        // Refresh to reset the form/result box, then attempt the same room + overlapping dates.
        driver.navigate().refresh();
        wait.until(d -> d.findElement(By.id("book-room")).isDisplayed());

        BookingPage secondAttempt = new BookingPage(driver, wait);
        secondAttempt.selectFirstAvailableRoom()
                .fillGuestDetails("Second Booker", "second.booker+" + System.currentTimeMillis() + "@example.com")
                .setDates(checkIn, checkOut)
                .submit();

        String secondResult = secondAttempt.waitForResultText();
        assertTrue(secondAttempt.isResultError(),
                "Expected overlapping booking on the same room to be rejected, got: " + secondResult);
    }
}
