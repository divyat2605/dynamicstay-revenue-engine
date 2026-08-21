package com.dynamicstay.dto;

import com.dynamicstay.model.Booking;
import com.dynamicstay.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private String guestName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal finalPrice;
    private BookingStatus status;
    private String pricingStrategyUsed;

    public static BookingResponse fromEntity(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .roomId(b.getRoom().getId())
                .roomNumber(b.getRoom().getRoomNumber())
                .guestName(b.getGuest().getFullName())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .finalPrice(b.getFinalPrice())
                .status(b.getStatus())
                .pricingStrategyUsed(b.getPricingStrategyUsed())
                .build();
    }
}
