package com.dynamicstay.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the "New Booking" form (search → select room → book → confirm). */
public class BookingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By roomSelect = By.id("book-room");
    private final By nameInput = By.id("book-name");
    private final By emailInput = By.id("book-email");
    private final By checkInInput = By.id("book-checkin");
    private final By checkOutInput = By.id("book-checkout");
    private final By bookButton = By.id("book-btn");
    private final By bookingResult = By.id("booking-result");
    private final By bookingsTbody = By.id("bookings-tbody");

    public BookingPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public BookingPage selectFirstAvailableRoom() {
        wait.until(ExpectedConditions.presenceOfElementLocated(roomSelect));
        new Select(driver.findElement(roomSelect)).selectByIndex(0);
        return this;
    }

    public BookingPage fillGuestDetails(String name, String email) {
        setValue(nameInput, name);
        setValue(emailInput, email);
        return this;
    }

    public BookingPage setDates(String checkIn, String checkOut) {
        setValue(checkInInput, checkIn);
        setValue(checkOutInput, checkOut);
        return this;
    }

    public BookingPage submit() {
        driver.findElement(bookButton).click();
        return this;
    }

    public String waitForResultText() {
        wait.until(d -> {
            String text = d.findElement(bookingResult).getText();
            return text != null && !text.isBlank() && !text.equals("Submitting…");
        });
        return driver.findElement(bookingResult).getText();
    }

    public boolean isResultError() {
        return driver.findElement(bookingResult).getAttribute("class").contains("error");
    }

    public String getFirstBookingRowText() {
        wait.until(d -> !d.findElement(bookingsTbody).getText().contains("Loading bookings"));
        return driver.findElement(bookingsTbody).findElement(By.tagName("tr")).getText();
    }

    private void setValue(By locator, String value) {
        WebElement el = driver.findElement(locator);
        el.clear();
        el.sendKeys(value);
    }
}
