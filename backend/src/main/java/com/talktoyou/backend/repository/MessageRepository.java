package com.talktoyou.backend.repository;

import com.talktoyou.backend.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // 특정 채팅방의 메시지 목록 조회 (시간순)
    List<Message> findByRoomIdOrderByCreatedAtAsc(String roomId);

    // 특정 채팅방의 최근 메시지 조회 (제한된 개수)
    List<Message> findTop50ByRoomIdOrderByCreatedAtDesc(String roomId);

    // 특정 사용자가 보낸 메시지 목록 조회
    List<Message> findByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 채팅방의 메시지 수 카운트
    long countByRoomId(String roomId);
}