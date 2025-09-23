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
public class TokenValidationResponse {

    private boolean valid;

    private String message;

    private String userId;

    private String userName;

    private LocalDateTime expirationTime;

    private Long remainingTime; // 남은 유효 시간 (초)
}