package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Discounts a room the closer check-in gets with no booking yet, on the
 * theory that an empty room tonight earns nothing — better to fill it at a
 * markdown than leave it vacant. Far-out bookings are untouched.
 */
@Component
public class LastMinutePricing implements PricingStrategy {

    private final int immediateDays;
    private final int nearTermDays;
    private final int shortTermDays;
    private final BigDecimal immediateMultiplier;
    private final BigDecimal nearTermMultiplier;
    private final BigDecimal shortTermMultiplier;

    public LastMinutePricing() {
        this(1, 3, 7, "0.70", "0.85", "0.95");
    }

    @Autowired
    public LastMinutePricing(
            @Value("${pricing.last-minute.immediate-days:1}") int immediateDays,
            @Value("${pricing.last-minute.near-term-days:3}") int nearTermDays,
            @Value("${pricing.last-minute.short-term-days:7}") int shortTermDays,
            @Value("${pricing.last-minute.immediate-multiplier:0.70}") BigDecimal immediateMultiplier,
            @Value("${pricing.last-minute.near-term-multiplier:0.85}") BigDecimal nearTermMultiplier,
            @Value("${pricing.last-minute.short-term-multiplier:0.95}") BigDecimal shortTermMultiplier) {
        if (!(immediateDays >= 0 && immediateDays < nearTermDays && nearTermDays < shortTermDays)) {
            throw new IllegalArgumentException("last-minute day thresholds must be ordered");
        }
        this.immediateDays = immediateDays;
        this.nearTermDays = nearTermDays;
        this.shortTermDays = shortTermDays;
        this.immediateMultiplier = requirePositive(immediateMultiplier, "immediateMultiplier");
        this.nearTermMultiplier = requirePositive(nearTermMultiplier, "nearTermMultiplier");
        this.shortTermMultiplier = requirePositive(shortTermMultiplier, "shortTermMultiplier");
    }

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        long daysOut = context.daysUntilCheckIn();
        BigDecimal multiplier;

        if (daysOut <= immediateDays) {
            multiplier = immediateMultiplier;
        } else if (daysOut <= nearTermDays) {
            multiplier = nearTermMultiplier;
        } else if (daysOut <= shortTermDays) {
            multiplier = shortTermMultiplier;
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

    private BigDecimal requirePositive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
