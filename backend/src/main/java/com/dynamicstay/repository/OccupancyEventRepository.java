package com.dynamicstay.repository;

import com.dynamicstay.mongo.OccupancyEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface OccupancyEventRepository extends MongoRepository<OccupancyEvent, String> {
    List<OccupancyEvent> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
    List<OccupancyEvent> findByRoomIdOrderByTimestampDesc(Long roomId);
}
