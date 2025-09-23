package com.talktoyou.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String messageId;

    @Field("user_id")
    private String userId;

    @Field("room_id")
    private String roomId;

    @Field("content")
    private String content;

    @Field("created_at")
    private LocalDateTime createdAt;

    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    // 메시지 내용이 비어있는지 확인
    public boolean isEmpty() {
        return this.content == null || this.content.trim().isEmpty();
    }
}