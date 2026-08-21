package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Discounts a room the closer check-in gets with no booking yet, on the
 * theory that an empty room tonight earns nothing — better to fill it at a
 * markdown than leave it vacant. Far-out bookings are untouched.
 */
@Component
public class LastMinutePricing implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        long daysOut = context.daysUntilCheckIn();
        BigDecimal multiplier;

        if (daysOut <= 1) {
            multiplier = new BigDecimal("0.70");
        } else if (daysOut <= 3) {
            multiplier = new BigDecimal("0.85");
        } else if (daysOut <= 7) {
            multiplier = new BigDecimal("0.95");
        } else {
            multiplier = BigDecimal.ONE;
        }

        return context.getBaseRate()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.LAST_MINUTE;
    }
}
