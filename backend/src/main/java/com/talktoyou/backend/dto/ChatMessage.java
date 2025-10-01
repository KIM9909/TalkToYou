package com.talktoyou.backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    public enum MessageType {
        CHAT,       // 일반 채팅 메시지
        JOIN,       // 사용자 입장
        LEAVE       // 사용자 퇴장
    }

    private MessageType type;

    private String roomId;

    private String userId;

    private String userName;

    private String content;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}