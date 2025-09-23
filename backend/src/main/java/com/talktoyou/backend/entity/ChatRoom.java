package com.talktoyou.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    private String roomId;

    @Field("room_name")
    private String roomName;

    @Field("max_room_member")
    private Integer maxRoomMember;

    @Field("current_room_member")
    private Integer currentRoomMember;

    @Field("user_id")
    private String userId;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("deleted_at")
    private LocalDateTime deletedAt;

    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public void increaseMemberCount() {
        if (this.currentRoomMember < this.maxRoomMember) {
            this.currentRoomMember++;
        }
    }

    public void decreaseMemberCount() {
        if (this.currentRoomMember > 0) {
            this.currentRoomMember--;
        }
    }

    // 채팅방 가득 찼는지 확인
    public boolean isFull() {
        return this.currentRoomMember >= this.maxRoomMember;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제된 채팅방인지 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}