package com.talktoyou.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private String roomId;

    private String roomName;

    private Integer maxRoomMember;

    private Integer currentRoomMember;

    private String creatorId;

    private String creatorName;

    private LocalDateTime createdAt;

    private boolean isFull;
}