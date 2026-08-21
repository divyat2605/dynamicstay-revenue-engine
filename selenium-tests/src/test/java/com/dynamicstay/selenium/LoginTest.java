package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest extends BaseTest {

    @Test
    @DisplayName("Authenticated dashboard session can be logged out")
    void logoutReturnsToLoginScreen() {
        DashboardPage dashboard = new DashboardPage(driver, wait);

        dashboard.logout();

        assertTrue(driver.findElement(By.id("login-username")).isDisplayed());
        assertTrue(driver.findElement(By.id("dashboard")).getAttribute("class").contains("hidden"));
    }
}