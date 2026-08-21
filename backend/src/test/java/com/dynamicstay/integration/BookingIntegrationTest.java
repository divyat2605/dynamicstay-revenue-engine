package com.dynamicstay.integration;

import com.dynamicstay.dto.BookingRequest;
import com.dynamicstay.dto.BookingResponse;
import com.dynamicstay.exception.BookingConflictException;
import com.dynamicstay.repository.BookingRepository;
import com.dynamicstay.repository.OccupancyEventRepository;
import com.dynamicstay.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class BookingIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OccupancyEventRepository occupancyEventRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Test
    void concurrentRequestsForSameRoomAndDatesProduceExactlyOneBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            List<Future<Boolean>> results = List.of(
                    submitBooking(executor, start, "race-one@example.com", checkIn, checkOut),
                    submitBooking(executor, start, "race-two@example.com", checkIn, checkOut));
            start.countDown();

            long successes = 0;
            long conflicts = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                } else {
                    conflicts++;
                }
            }

            assertThat(successes).isEqualTo(1);
            assertThat(conflicts).isEqualTo(1);
            assertThat(bookingRepository.findOverlapping(1L, checkIn, checkOut)).isEmpty();
            assertThat(bookingRepository.findOverlapping(12L, checkIn, checkOut)).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void bookingAndCancellationPersistThroughTheTransactionalService() {
        LocalDate checkIn = LocalDate.now().plusDays(40);
        BookingResponse created = bookingService.createBooking(request("cancel@example.com", checkIn, checkIn.plusDays(1)));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(occupancyEventRepository.findByRoomIdOrderByTimestampDesc(12L))
                .anyMatch(event -> event.getDate().equals(checkIn)));

        BookingResponse cancelled = bookingService.cancelBooking(created.getId());

        assertThat(cancelled.getStatus().name()).isEqualTo("CANCELLED");
        assertThat(bookingRepository.findById(created.getId())).get()
                .extracting(booking -> booking.getStatus().name()).isEqualTo("CANCELLED");
    }

            @Test
            void unauthenticatedRequestsAreRejectedByTheFullApplicationContext() throws Exception {
            mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
            }

            @Test
            void managerCanReadButCannotCancelThroughTheFullApplicationContext() throws Exception {
            mockMvc.perform(get("/api/bookings")
                    .with(httpBasic("manager", "local-manager-change-me")))
                .andExpect(status().isOk());

            mockMvc.perform(delete("/api/bookings/999999")
                    .with(httpBasic("manager", "local-manager-change-me")))
                .andExpect(status().isForbidden());
            }

            @Test
            void adminPassesAuthorizationAndReachesTheController() throws Exception {
            mockMvc.perform(delete("/api/bookings/999999")
                    .with(httpBasic("admin", "local-admin-change-me")))
                .andExpect(status().isNotFound());
            }

    private Future<Boolean> submitBooking(ExecutorService executor, CountDownLatch start,
                                          String email, LocalDate checkIn, LocalDate checkOut) {
        return executor.submit(() -> {
            start.await();
            try {
                bookingService.createBooking(request(email, checkIn, checkOut));
                return true;
            } catch (BookingConflictException exception) {
                return false;
            }
        });
    }

    private BookingRequest request(String email, LocalDate checkIn, LocalDate checkOut) {
        BookingRequest request = new BookingRequest();
        request.setRoomId(12L);
        request.setGuestName("Integration Guest");
        request.setGuestEmail(email);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        return request;
    }
}
