package com.dynamicstay.controller;

import com.dynamicstay.dto.PriceQuoteRequest;
import com.dynamicstay.dto.PriceQuoteResponse;
import com.dynamicstay.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final BookingService bookingService;

    /** "Manager triggers a rate recalculation" — this is that endpoint. */
    @PostMapping("/quote")
    public PriceQuoteResponse quote(@Valid @RequestBody PriceQuoteRequest request) {
        return bookingService.quote(request);
    }
}
