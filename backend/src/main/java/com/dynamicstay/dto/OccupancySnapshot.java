package com.dynamicstay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancySnapshot {
    private LocalDate date;
    private long occupiedRooms;
    private long totalRooms;
    private double occupancyRate;
}
