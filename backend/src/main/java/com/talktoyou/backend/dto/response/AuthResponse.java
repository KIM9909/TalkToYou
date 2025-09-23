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
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private String userId;

    private String userName;

    private String nickName;

    private String email;

    private LocalDateTime loginTime;

    private Long expiresIn; // 토큰 만료까지 남은 시간 (초)
}