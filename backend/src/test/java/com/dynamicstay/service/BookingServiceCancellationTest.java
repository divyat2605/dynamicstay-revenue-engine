package com.dynamicstay.service;

import com.dynamicstay.exception.InvalidBookingRequestException;
import com.dynamicstay.model.Booking;
import com.dynamicstay.model.BookingStatus;
import com.dynamicstay.model.Guest;
import com.dynamicstay.model.Room;
import com.dynamicstay.repository.BookingRepository;
import com.dynamicstay.repository.GuestRepository;
import com.dynamicstay.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceCancellationTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RoomService roomService;
    @Mock
    private RateEngine rateEngine;
    @Mock
    private OccupancyService occupancyService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter counter;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void configureMetrics() {
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    void confirmedBookingCanBeCancelled() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(occupancyService.currentOccupancyRate(booking.getCheckIn())).thenReturn(0.25);

        assertThat(bookingService.cancelBooking(7L).getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
        verify(eventPublisher).publishEvent(any(OccupancyEventRequested.class));
    }

    @Test
    void checkedOutBookingCannotBeCancelled() {
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking(BookingStatus.CHECKED_OUT)));

        assertThatThrownBy(() -> bookingService.cancelBooking(7L))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Only confirmed or pending bookings can be cancelled");
    }

    private Booking booking(BookingStatus status) {
        return Booking.builder()
                .id(7L)
                .guest(Guest.builder().fullName("Test Guest").email("test@example.com").build())
                .room(Room.builder().id(12L).roomNumber("402").baseRate(new BigDecimal("100.00")).build())
                .checkIn(LocalDate.of(2026, 9, 1))
                .checkOut(LocalDate.of(2026, 9, 2))
                .quotedRate(new BigDecimal("90.00"))
                .finalPrice(new BigDecimal("90.00"))
                .status(status)
                .pricingStrategyUsed("SEASONAL")
                .build();
    }
}