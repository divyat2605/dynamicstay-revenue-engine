package com.dynamicstay.service;

import com.dynamicstay.dto.RoomDto;
import com.dynamicstay.exception.ResourceNotFoundException;
import com.dynamicstay.model.Room;
import com.dynamicstay.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomDto> getAllActiveRooms() {
        return roomRepository.findByActiveTrue().stream()
                .map(RoomDto::fromEntity)
                .toList();
    }

    public Room getRoomEntity(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
    }

    public RoomDto getRoom(Long roomId) {
        return RoomDto.fromEntity(getRoomEntity(roomId));
    }
}
