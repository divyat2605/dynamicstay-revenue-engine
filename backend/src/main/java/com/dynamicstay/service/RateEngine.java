package com.dynamicstay.service;

import com.dynamicstay.pricing.LastMinutePricing;
import com.dynamicstay.pricing.OccupancyBasedPricing;
import com.dynamicstay.pricing.PricingContext;
import com.dynamicstay.pricing.PricingStrategy;
import com.dynamicstay.pricing.PricingStrategyType;
import com.dynamicstay.pricing.SeasonalPricing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Orchestrates the {@link PricingStrategy} implementations. This is the
 * "brain" of DynamicStay: rather than hard-picking a single strategy, it
 * decides which signal should dominate for a given request, then blends
 * strategies so no single lever swings price too violently.
 *
 * <p>Decision rule (deliberately simple / explainable for a portfolio demo):
 * <ul>
 *   <li>If check-in is within 3 days, last-minute urgency dominates (60%),
 *       with occupancy and seasonality each contributing 20%.</li>
 *   <li>Else if current occupancy is at/above 70%, occupancy pressure
 *       dominates (60%), seasonality contributes 40%.</li>
 *   <li>Otherwise, seasonality alone drives price.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RateEngine {

    private final SeasonalPricing seasonalPricing;
    private final OccupancyBasedPricing occupancyBasedPricing;
    private final LastMinutePricing lastMinutePricing;

    public record Quote(BigDecimal price, PricingStrategyType dominantStrategy) {}

    public Quote quote(PricingContext context) {
        BigDecimal seasonal = seasonalPricing.calculatePrice(context);
        BigDecimal occupancy = occupancyBasedPricing.calculatePrice(context);
        BigDecimal lastMinute = lastMinutePricing.calculatePrice(context);

        boolean isLastMinute = context.daysUntilCheckIn() <= 3;
        boolean isHighOccupancy = context.getCurrentOccupancyRate() >= 0.70;

        BigDecimal blended;
        PricingStrategyType dominant;

        if (isLastMinute) {
            blended = weighted(lastMinute, "0.60")
                    .add(weighted(occupancy, "0.20"))
                    .add(weighted(seasonal, "0.20"));
            dominant = PricingStrategyType.LAST_MINUTE;
        } else if (isHighOccupancy) {
            blended = weighted(occupancy, "0.60")
                    .add(weighted(seasonal, "0.40"));
            dominant = PricingStrategyType.OCCUPANCY_BASED;
        } else {
            blended = seasonal;
            dominant = PricingStrategyType.SEASONAL;
        }

        return new Quote(blended.setScale(2, RoundingMode.HALF_UP), dominant);
    }

    private BigDecimal weighted(BigDecimal price, String weight) {
        return price.multiply(new BigDecimal(weight));
    }
}
