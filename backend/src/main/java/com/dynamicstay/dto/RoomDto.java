package com.dynamicstay.dto;

import com.dynamicstay.model.Room;
import com.dynamicstay.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal baseRate;
    private Integer maxOccupancy;
    private Boolean active;

    public static RoomDto fromEntity(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .baseRate(room.getBaseRate())
                .maxOccupancy(room.getMaxOccupancy())
                .active(room.getActive())
                .build();
    }
}
