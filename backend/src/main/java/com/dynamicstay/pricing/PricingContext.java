package com.dynamicstay.pricing;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable snapshot of everything a {@link PricingStrategy} needs to price a
 * stay. Built by {@link com.dynamicstay.service.RateEngine} from live
 * Postgres data before any strategy is invoked, so strategies stay pure
 * functions of their input (easy to unit test with Mockito-free plain
 * objects).
 */
@Getter
@Builder
public class PricingContext {

    /** The room's undiscounted, unmodified nightly rate. */
    private final BigDecimal baseRate;

    /** Requested check-in date. */
    private final LocalDate checkInDate;

    /** Requested check-out date. */
    private final LocalDate checkOutDate;

    /** Current occupancy across the hotel for the requested date, 0.0–1.0. */
    private final double currentOccupancyRate;

    /** Today's date, injected so strategies stay deterministic/testable. */
    private final LocalDate today;

    public long daysUntilCheckIn() {
        return java.time.temporal.ChronoUnit.DAYS.between(today, checkInDate);
    }

    public long nights() {
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /** True for Northern-hemisphere peak leisure travel months (Jun–Aug) and Dec. */
    public boolean isPeakSeason() {
        int month = checkInDate.getMonthValue();
        return month == 6 || month == 7 || month == 8 || month == 12;
    }
}
