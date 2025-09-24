package com.talktoyou.backend.repository;

import com.talktoyou.backend.entity.RoomMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMemberRepository extends MongoRepository<RoomMember, String> {

    // 특정 채팅방의 멤버 목록 조회
    List<RoomMember> findByRoomIdOrderByJoinedAtAsc(String roomId);

    // 특정 사용자가 참여한 채팅방 목록 조회
    List<RoomMember> findByUserIdOrderByJoinedAtDesc(String userId);

    // 특정 사용자가 특정 채팅방에 참여했는지 확인
    Optional<RoomMember> findByUserIdAndRoomId(String userId, String roomId);

    // 특정 채팅방 멤버 수 카운트
    long countByRoomId(String roomId);

    // 특정 사용자-채팅방 관계 삭제
    void deleteByUserIdAndRoomId(String userId, String roomId);

    // 특정 사용자가 특정 채팅방에 있는지 확인
    boolean existsByUserIdAndRoomId(String userId, String roomId);
}