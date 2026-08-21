package com.dynamicstay.config;

import com.dynamicstay.controller.BookingController;
import com.dynamicstay.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, WebConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private BookingService bookingService;

    @Test
    void unauthenticatedApiRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managerCanReadBookings() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .with(httpBasic("manager", "local-manager-change-me")))
                .andExpect(status().isOk());
    }

    @Test
    void managerCannotCancelBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/42")
                        .with(httpBasic("manager", "local-manager-change-me")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminMayCancelBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/42")
                        .with(httpBasic("admin", "local-admin-change-me")))
                .andExpect(status().isOk());
    }

    @Test
    void configuredFrontendOriginIsAllowedAndWildcardIsNotUsed() throws Exception {
        mockMvc.perform(options("/api/bookings")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5500"));
    }
}