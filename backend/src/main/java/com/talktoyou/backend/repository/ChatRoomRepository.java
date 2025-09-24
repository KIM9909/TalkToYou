package com.talktoyou.backend.repository;

import com.talktoyou.backend.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    // 삭제되지 않은 채팅방만 조회
    List<ChatRoom> findByDeletedAtIsNullOrderByCreatedAtDesc();

    // 특정 채팅방 조회 (삭제되지 않은 것만)
    Optional<ChatRoom> findByRoomIdAndDeletedAtIsNull(String roomId);

    // 특정 사용자가 생성한 채팅방들 조회
    List<ChatRoom> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(String userId);

    // 채팅방 이름으로 검색 (삭제되지 않은 것만)
    List<ChatRoom> findByRoomNameContainingAndDeletedAtIsNullOrderByCreatedAtDesc(String roomName);
}