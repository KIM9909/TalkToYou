package com.talktoyou.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "room_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(def = "{'user_id': 1, 'room_id': 1}", unique = true) // 복합 유니크 인덱스
public class RoomMember {

    @Id
    private String memberId;

    @Field("user_id")
    private String userId;

    @Field("room_id")
    private String roomId;

    @Field("joined_at")
    private LocalDateTime joinedAt;

    // 참여 시점 설정
    public void setJoinedAt() {
        this.joinedAt = LocalDateTime.now();
    }
}