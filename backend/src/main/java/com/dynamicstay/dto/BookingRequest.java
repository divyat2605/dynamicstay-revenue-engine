package com.dynamicstay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingRequest {

    @NotNull(message = "roomId is required")
    @Positive(message = "roomId must be positive")
    private Long roomId;

    @NotBlank(message = "guest full name is required")
    private String guestName;

    @NotBlank(message = "guest email is required")
    @Email(message = "guest email must be valid")
    private String guestEmail;

    private String guestPhone;

    @NotNull(message = "checkIn is required")
    @FutureOrPresent(message = "checkIn must be today or later")
    private LocalDate checkIn;

    @NotNull(message = "checkOut is required")
    @Future(message = "checkOut must be in the future")
    private LocalDate checkOut;
}
