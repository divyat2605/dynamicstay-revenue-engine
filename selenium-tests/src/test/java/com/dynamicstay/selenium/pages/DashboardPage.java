package com.dynamicstay.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the dashboard's occupancy summary and rate-recalculation
 * ("Get Quote") panel.
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By occupancyRate = By.id("occ-rate");
    private final By roomsTbody = By.id("rooms-tbody");
    private final By quoteRoomSelect = By.id("quote-room");
    private final By quoteCheckIn = By.id("quote-checkin");
    private final By quoteCheckOut = By.id("quote-checkout");
    private final By recalcButton = By.id("recalc-btn");
    private final By quoteResult = By.id("quote-result");
    private final By loginPanel = By.id("login-panel");
    private final By logoutButton = By.id("logout-btn");
    private final By cancelButton = By.cssSelector("#bookings-tbody .cancel-booking-btn");
    private final By bookingResult = By.id("booking-result");

    public DashboardPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public DashboardPage waitForRoomsLoaded() {
        wait.until(d -> !d.findElement(roomsTbody).getText().contains("Loading rooms"));
        return this;
    }

    public String getOccupancyRateText() {
        return driver.findElement(occupancyRate).getText();
    }

    public DashboardPage selectRoomForQuote(String visibleTextContains) {
        wait.until(ExpectedConditions.presenceOfElementLocated(quoteRoomSelect));
        Select select = new Select(driver.findElement(quoteRoomSelect));
        select.getOptions().stream()
                .filter(opt -> opt.getText().contains(visibleTextContains))
                .findFirst()
                .ifPresentOrElse(select::selectByVisibleText,
                        () -> select.selectByIndex(0));
        return this;
    }

    public DashboardPage selectFirstAvailableRoomForQuote() {
        wait.until(ExpectedConditions.presenceOfElementLocated(quoteRoomSelect));
        new Select(driver.findElement(quoteRoomSelect)).selectByIndex(0);
        return this;
    }

    public DashboardPage setQuoteDates(String checkIn, String checkOut) {
        WebElement checkInEl = driver.findElement(quoteCheckIn);
        checkInEl.clear();
        checkInEl.sendKeys(checkIn);

        WebElement checkOutEl = driver.findElement(quoteCheckOut);
        checkOutEl.clear();
        checkOutEl.sendKeys(checkOut);
        return this;
    }

    public DashboardPage submitQuote() {
        driver.findElement(recalcButton).click();
        return this;
    }

    public String waitForQuoteResultText() {
        wait.until(d -> {
            String text = d.findElement(quoteResult).getText();
            return text != null && !text.isBlank() && !text.equals("Calculating…");
        });
        return driver.findElement(quoteResult).getText();
    }

    public boolean isQuoteResultError() {
        return driver.findElement(quoteResult).getAttribute("class").contains("error");
    }

    public DashboardPage logout() {
        driver.findElement(logoutButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginPanel));
        return this;
    }

    public DashboardPage cancelFirstBooking() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        return this;
    }

    public String waitForBookingResultText() {
        wait.until(d -> {
            String text = d.findElement(bookingResult).getText();
            return text != null && !text.isBlank() && !text.equals("Cancelling booking…");
        });
        return driver.findElement(bookingResult).getText();
    }
}
