package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classic revenue-management demand curve: the fuller the hotel, the higher
 * the marginal room's price. Uses a tiered step function rather than a
 * smooth curve, mirroring how real RMS "rate tiers" are usually configured
 * so the behaviour is easy to explain and test.
 */
@Component
public class OccupancyBasedPricing implements PricingStrategy {

    private final double highThreshold;
    private final double mediumThreshold;
    private final double lowThreshold;
    private final BigDecimal highMultiplier;
    private final BigDecimal mediumMultiplier;
    private final BigDecimal lowMultiplier;
    private final BigDecimal emptyMultiplier;

    public OccupancyBasedPricing() {
        this(0.90, 0.70, 0.40, "1.50", "1.25", "1.05", "0.85");
    }

    @Autowired
    public OccupancyBasedPricing(
            @Value("${pricing.occupancy.high-threshold:0.90}") double highThreshold,
            @Value("${pricing.occupancy.medium-threshold:0.70}") double mediumThreshold,
            @Value("${pricing.occupancy.low-threshold:0.40}") double lowThreshold,
            @Value("${pricing.occupancy.high-multiplier:1.50}") BigDecimal highMultiplier,
            @Value("${pricing.occupancy.medium-multiplier:1.25}") BigDecimal mediumMultiplier,
            @Value("${pricing.occupancy.low-multiplier:1.05}") BigDecimal lowMultiplier,
            @Value("${pricing.occupancy.empty-multiplier:0.85}") BigDecimal emptyMultiplier) {
        if (!(lowThreshold >= 0 && lowThreshold < mediumThreshold && mediumThreshold < highThreshold && highThreshold <= 1)) {
            throw new IllegalArgumentException("occupancy thresholds must be ordered within 0..1");
        }
        this.highThreshold = highThreshold;
        this.mediumThreshold = mediumThreshold;
        this.lowThreshold = lowThreshold;
        this.highMultiplier = requirePositive(highMultiplier, "highMultiplier");
        this.mediumMultiplier = requirePositive(mediumMultiplier, "mediumMultiplier");
        this.lowMultiplier = requirePositive(lowMultiplier, "lowMultiplier");
        this.emptyMultiplier = requirePositive(emptyMultiplier, "emptyMultiplier");
    }

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        double occupancy = context.getCurrentOccupancyRate();
        BigDecimal multiplier;

        if (occupancy >= highThreshold) {
            multiplier = highMultiplier;
        } else if (occupancy >= mediumThreshold) {
            multiplier = mediumMultiplier;
        } else if (occupancy >= lowThreshold) {
            multiplier = lowMultiplier;
        } else {
            multiplier = emptyMultiplier;
        }

        return context.getBaseRate()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.OCCUPANCY_BASED;
    }

    private BigDecimal requirePositive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
