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
public class MessageResponse {

    private String messageId;

    private String roomId;

    private String userId;

    private String userName;

    private String content;

    private LocalDateTime createdAt;
}