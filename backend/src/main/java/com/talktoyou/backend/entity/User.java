package com.talktoyou.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String userId;

    @Field("user_name")
    @Indexed(unique = true)
    private String userName;

    @Field("nick_name")
    private String nickName;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("password")
    private String password;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("deleted_at")
    private LocalDateTime deletedAt;

    // 생성 시점에 자동으로 현재 시간 설정
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    // Soft Delete 처리
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제된 유저인지 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}