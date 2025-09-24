package com.talktoyou.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(min = 2, max = 50, message = "채팅방 이름은 2-50자 사이여야 합니다")
    private String roomName;

    @Min(value = 2, message = "최소 참여자 수는 2명입니다")
    @Max(value = 100, message = "최대 참여자 수는 100명입니다")
    private Integer maxRoomMember;
}