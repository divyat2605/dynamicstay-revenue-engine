package com.dynamicstay.selenium;

import com.dynamicstay.selenium.pages.DashboardPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Uses the default manager credentials and verifies admin-only cancellation. */
class AuthorizationTest extends BaseTest {

    @Test
    @DisplayName("Manager receives a clear authorization error for cancellation")
    void managerCannotCancelBooking() {
        Assumptions.assumeTrue("manager".equals(USERNAME),
            "Run this scenario with dynamicstay.username=manager");
        DashboardPage dashboard = new DashboardPage(driver, wait);

        dashboard.cancelFirstBooking();

        assertTrue(dashboard.waitForBookingResultText().contains("Only an administrator"));
    }
}