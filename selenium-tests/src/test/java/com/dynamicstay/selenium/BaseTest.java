package com.dynamicstay.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Common WebDriver lifecycle for all DynamicStay E2E tests.
 *
 * Base URL for the dashboard defaults to the local static-file server used
 * in the README ("http://localhost:5500"), but can be overridden with
 * -Ddynamicstay.baseUrl=... so the same suite runs in CI against a deployed
 * frontend.
 */
public abstract class BaseTest {

    protected ChromeDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL =
            System.getProperty("dynamicstay.baseUrl", "http://localhost:5500");

    @BeforeEach
    void setUpDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("dynamicstay.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
