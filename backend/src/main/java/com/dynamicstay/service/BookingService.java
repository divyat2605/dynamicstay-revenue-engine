package com.dynamicstay.service;

import com.dynamicstay.dto.BookingRequest;
import com.dynamicstay.dto.BookingResponse;
import com.dynamicstay.dto.PriceQuoteRequest;
import com.dynamicstay.dto.PriceQuoteResponse;
import com.dynamicstay.exception.BookingConflictException;
import com.dynamicstay.exception.InvalidBookingRequestException;
import com.dynamicstay.model.Booking;
import com.dynamicstay.model.BookingStatus;
import com.dynamicstay.model.Guest;
import com.dynamicstay.model.Room;
import com.dynamicstay.model.Transaction;
import com.dynamicstay.pricing.PricingContext;
import com.dynamicstay.repository.BookingRepository;
import com.dynamicstay.repository.GuestRepository;
import com.dynamicstay.repository.TransactionRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final TransactionRepository transactionRepository;
    private final RoomService roomService;
    private final RateEngine rateEngine;
        private final OccupancyService occupancyService;
        private final ApplicationEventPublisher eventPublisher;
        private final MeterRegistry meterRegistry;

    /** Quote a price without creating a booking (used by the "Get Quote" UI action). */
    public PriceQuoteResponse quote(PriceQuoteRequest request) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateDateRange(request.getCheckIn(), request.getCheckOut());
            Room room = roomService.getRoomEntity(request.getRoomId());

            PricingContext context = buildContext(room, request.getCheckIn(), request.getCheckOut());
            RateEngine.Quote result = rateEngine.quote(context);

            long nights = context.nights();
            BigDecimal total = result.price().multiply(BigDecimal.valueOf(nights));

            return PriceQuoteResponse.builder()
                    .roomId(room.getId())
                    .baseRate(room.getBaseRate())
                    .quotedPricePerNight(result.price())
                    .nights(nights)
                    .totalPrice(total)
                    .strategyUsed(result.dominantStrategy().name())
                    .occupancyRateAtQuote(context.getCurrentOccupancyRate())
                    .build();
                } finally {
            timer.stop(meterRegistry.timer("pricing.quote.duration"));
                }
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Timer.Sample timer = Timer.start(meterRegistry);
        try {
            validateDateRange(request.getCheckIn(), request.getCheckOut());
            Room room = roomService.getRoomEntity(request.getRoomId());

        List<Booking> overlapping = bookingRepository.findOverlapping(
                room.getId(), request.getCheckIn(), request.getCheckOut());
        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(
                    "Room " + room.getRoomNumber() + " is already booked for an overlapping date range.");
        }

        Guest guest = guestRepository.findByEmail(request.getGuestEmail())
                .orElseGet(() -> guestRepository.save(Guest.builder()
                        .fullName(request.getGuestName())
                        .email(request.getGuestEmail())
                        .phone(request.getGuestPhone())
                        .build()));

        PricingContext context = buildContext(room, request.getCheckIn(), request.getCheckOut());
        RateEngine.Quote result = rateEngine.quote(context);
        BigDecimal totalPrice = result.price().multiply(BigDecimal.valueOf(context.nights()));

        Booking booking = Booking.builder()
                .guest(guest)
                .room(room)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .quotedRate(result.price())
                .finalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .pricingStrategyUsed(result.dominantStrategy().name())
                .build();
            try {
                // Flush now so PostgreSQL arbitrates concurrent requests before we
                // create the payment record or publish the post-commit event.
                booking = bookingRepository.saveAndFlush(booking);
            } catch (DataIntegrityViolationException ex) {
                meterRegistry.counter("booking.failure.count", "reason", "conflict").increment();
                throw new BookingConflictException(
                        "Room " + room.getRoomNumber() + " is already booked for an overlapping date range.");
            }

        Transaction transaction = Transaction.builder()
                .booking(booking)
                .amount(totalPrice)
                .paymentStatus("COMPLETED")
                .build();
        transactionRepository.save(transaction);

        double occupancyAfter = occupancyService.currentOccupancyRate(request.getCheckIn());
        eventPublisher.publishEvent(new OccupancyEventRequested(room.getId(), "BOOKING_CREATED", occupancyAfter,
                request.getCheckIn(), result.dominantStrategy().name(), totalPrice));

        log.info("Booking {} created for room {} ({} -> {}) at {} using {} strategy",
                booking.getId(), room.getRoomNumber(), request.getCheckIn(), request.getCheckOut(),
                totalPrice, result.dominantStrategy());
        meterRegistry.counter("booking.creation.count", "outcome", "success").increment();
                        return BookingResponse.fromEntity(booking);
                } finally {
                        timer.stop(meterRegistry.timer("booking.creation.duration"));
                }
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new com.dynamicstay.exception.ResourceNotFoundException(
                        "Booking not found: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        eventPublisher.publishEvent(new OccupancyEventRequested(booking.getRoom().getId(), "CANCELLATION",
                occupancyService.currentOccupancyRate(booking.getCheckIn()), booking.getCheckIn(),
                booking.getPricingStrategyUsed(), booking.getFinalPrice()));

        return BookingResponse.fromEntity(booking);
    }

    public List<BookingResponse> recentBookings() {
        return bookingRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    private PricingContext buildContext(Room room, LocalDate checkIn, LocalDate checkOut) {
        double occupancy = occupancyService.currentOccupancyRate(checkIn);
        return PricingContext.builder()
                .baseRate(room.getBaseRate())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .currentOccupancyRate(occupancy)
                .today(LocalDate.now())
                .build();
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingRequestException("checkIn and checkOut are required");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingRequestException("checkOut must be after checkIn");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingRequestException("checkIn cannot be in the past");
        }
    }
}
