package com.dynamicstay.repository;

import com.dynamicstay.model.Booking;
import com.dynamicstay.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomIdAndStatusNot(Long roomId, BookingStatus status);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.room.id = :roomId
             AND b.status <> com.dynamicstay.model.BookingStatus.CANCELLED
             AND b.checkIn < :checkOut
             AND b.checkOut > :checkIn
           """)
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);

    @Query("""
           SELECT COUNT(b) FROM Booking b
           WHERE b.status <> com.dynamicstay.model.BookingStatus.CANCELLED
             AND b.checkIn <= :date
             AND b.checkOut > :date
           """)
    long countOccupiedOnDate(@Param("date") LocalDate date);

    List<Booking> findTop50ByOrderByCreatedAtDesc();
}
