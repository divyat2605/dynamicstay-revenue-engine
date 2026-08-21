package com.dynamicstay.controller;

import com.dynamicstay.dto.PriceQuoteRequest;
import com.dynamicstay.dto.PriceQuoteResponse;
import com.dynamicstay.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final BookingService bookingService;

    /** "Manager triggers a rate recalculation" — this is that endpoint. */
    @PostMapping("/quote")
        @Operation(summary = "Calculate an explainable dynamic price quote")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quote calculated"),
            @ApiResponse(responseCode = "400", description = "Invalid room or date range")
        })
    public PriceQuoteResponse quote(@Valid @RequestBody PriceQuoteRequest request) {
        return bookingService.quote(request);
    }
}
