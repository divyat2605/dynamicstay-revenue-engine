package com.dynamicstay.pricing;

import java.math.BigDecimal;

/**
 * Strategy pattern: each implementation encodes one independent pricing
 * "lever" a real revenue-management platform (e.g. IDeaS) would expose —
 * seasonality, live occupancy pressure, or last-minute urgency. Keeping them
 * behind a single interface lets {@link com.dynamicstay.service.RateEngine}
 * pick, combine, or swap algorithms at runtime without any caller caring
 * which one ran.
 */
public interface PricingStrategy {

    /**
     * @return the per-night price after this strategy's adjustment is applied.
     */
    BigDecimal calculatePrice(PricingContext context);

    /** Identifies which strategy produced a price, for audit/logging. */
    PricingStrategyType getType();
}
