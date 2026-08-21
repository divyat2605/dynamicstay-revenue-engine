package com.dynamicstay.controller;

import com.dynamicstay.dto.RoomDto;
import com.dynamicstay.service.RoomService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List active rooms")
    public List<RoomDto> getAllRooms() {
        return roomService.getAllActiveRooms();
    }

    @GetMapping("/{id}")
        @Operation(summary = "Get an active room by ID")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room found"),
            @ApiResponse(responseCode = "404", description = "Room not found")
        })
        public RoomDto getRoom(@PathVariable @Positive Long id) {
        return roomService.getRoom(id);
    }
}
