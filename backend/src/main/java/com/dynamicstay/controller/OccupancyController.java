package com.dynamicstay.controller;

import com.dynamicstay.dto.OccupancySnapshot;
import com.dynamicstay.exception.InvalidBookingRequestException;
import com.dynamicstay.service.OccupancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/occupancy")
@RequiredArgsConstructor
public class OccupancyController {

    private final OccupancyService occupancyService;

    @GetMapping("/today")
    public OccupancySnapshot today() {
        return occupancyService.snapshot(LocalDate.now());
    }

    /** Trend data for the dashboard chart, e.g. GET /api/occupancy/trend?from=2026-08-01&to=2026-08-21 */
    @GetMapping("/trend")
    public List<OccupancySnapshot> trend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (to.isBefore(from)) {
            throw new InvalidBookingRequestException("to must be on or after from");
        }
        return occupancyService.trend(from, to);
    }
}
