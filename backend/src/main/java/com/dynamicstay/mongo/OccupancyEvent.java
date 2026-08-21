package com.dynamicstay.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Append-mostly analytics record. Written asynchronously off the booking
 * transaction path — losing one is not business-critical, so it lives in
 * MongoDB rather than competing for the Postgres transaction.
 */
@Document(collection = "occupancy_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyEvent {

    @Id
    private String id;

    private Long roomId;

    /** BOOKING_CREATED | CHECK_IN | CHECK_OUT | CANCELLATION */
    private String eventType;

    private Double occupancyRateAtEvent;

    private LocalDate date;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Map<String, Object> metadata;

    public static Map<String, Object> metadata(String strategyUsed, BigDecimal finalPrice) {
        return Map.of("strategyUsed", strategyUsed, "finalPrice", finalPrice);
    }
}
