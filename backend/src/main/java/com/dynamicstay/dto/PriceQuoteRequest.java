package com.dynamicstay.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PriceQuoteRequest {

    @NotNull(message = "roomId is required")
    private Long roomId;

    @NotNull(message = "checkIn is required")
    @FutureOrPresent(message = "checkIn must be today or later")
    private LocalDate checkIn;

    @NotNull(message = "checkOut is required")
    @Future(message = "checkOut must be in the future")
    private LocalDate checkOut;
}
