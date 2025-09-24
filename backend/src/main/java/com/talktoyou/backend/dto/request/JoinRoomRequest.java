package com.talktoyou.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {

    @NotBlank(message = "채팅방 ID는 필수입니다")
    private String roomId;
}