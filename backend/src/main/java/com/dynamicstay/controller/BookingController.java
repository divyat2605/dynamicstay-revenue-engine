package com.dynamicstay.controller;

import com.dynamicstay.dto.BookingRequest;
import com.dynamicstay.dto.BookingResponse;
import com.dynamicstay.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "List the 50 most recent bookings")
    public List<BookingResponse> recentBookings() {
        return bookingService.recentBookings();
    }

    @PostMapping
        @Operation(summary = "Create a dynamically priced booking")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request"),
            @ApiResponse(responseCode = "409", description = "Room dates conflict")
        })
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request);
    }

    @DeleteMapping("/{id}")
        @Operation(summary = "Cancel a pending or confirmed booking")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled")
        })
        public BookingResponse cancelBooking(@PathVariable @Positive Long id) {
        return bookingService.cancelBooking(id);
    }
}
