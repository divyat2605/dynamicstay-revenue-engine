package com.dynamicstay.service;

import com.dynamicstay.dto.OccupancySnapshot;
import com.dynamicstay.mongo.OccupancyEvent;
import com.dynamicstay.repository.BookingRepository;
import com.dynamicstay.repository.OccupancyEventRepository;
import com.dynamicstay.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reads/writes occupancy signal. Live occupancy % (used by the pricing
 * engine) is computed from Postgres, since it must be correct-right-now.
 * Historical event logging goes to MongoDB and is fired asynchronously so it
 * never blocks or risks the booking transaction.
 */
@Service
@RequiredArgsConstructor
public class OccupancyService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final OccupancyEventRepository occupancyEventRepository;

    public double currentOccupancyRate(LocalDate date) {
        long totalRooms = roomRepository.findByActiveTrue().size();
        if (totalRooms == 0) {
            return 0.0;
        }
        long occupied = bookingRepository.countOccupiedOnDate(date);
        return Math.min(1.0, (double) occupied / (double) totalRooms);
    }

    public OccupancySnapshot snapshot(LocalDate date) {
        long totalRooms = roomRepository.findByActiveTrue().size();
        long occupied = bookingRepository.countOccupiedOnDate(date);
        double rate = totalRooms == 0 ? 0.0 : Math.min(1.0, (double) occupied / (double) totalRooms);
        return OccupancySnapshot.builder()
                .date(date)
                .occupiedRooms(occupied)
                .totalRooms(totalRooms)
                .occupancyRate(rate)
                .build();
    }

    public List<OccupancySnapshot> trend(LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1))
                .map(this::snapshot)
                .toList();
    }

    @Async
    public void logEvent(Long roomId, String eventType, double occupancyAtEvent,
                          LocalDate date, String strategyUsed, BigDecimal finalPrice) {
        OccupancyEvent event = OccupancyEvent.builder()
                .roomId(roomId)
                .eventType(eventType)
                .occupancyRateAtEvent(occupancyAtEvent)
                .date(date)
                .metadata(OccupancyEvent.metadata(strategyUsed, finalPrice))
                .build();
        occupancyEventRepository.save(event);
    }
}
