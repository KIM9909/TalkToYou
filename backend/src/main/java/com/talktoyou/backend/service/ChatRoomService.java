package com.talktoyou.backend.service;

import com.talktoyou.backend.dto.request.CreateRoomRequest;
import com.talktoyou.backend.dto.response.RoomResponse;
import com.talktoyou.backend.entity.ChatRoom;
import com.talktoyou.backend.entity.RoomMember;
import com.talktoyou.backend.entity.User;
import com.talktoyou.backend.repository.ChatRoomRepository;
import com.talktoyou.backend.repository.RoomMemberRepository;
import com.talktoyou.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    // 채팅방 생성
    public RoomResponse createRoom(CreateRoomRequest request, String userId) {
        // 사용자 존재 확인
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(request.getRoomName())
                .maxRoomMember(request.getMaxRoomMember())
                .currentRoomMember(1) // 생성자가 자동으로 참여
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 생성자를 채팅방에 자동 추가
        RoomMember roomMember = RoomMember.builder()
                .userId(userId)
                .roomId(savedRoom.getRoomId())
                .joinedAt(LocalDateTime.now())
                .build();

        roomMemberRepository.save(roomMember);

        log.info("채팅방 생성 완료: {} by {}", savedRoom.getRoomName(), creator.getUserName());

        return convertToRoomResponse(savedRoom, creator);
    }

    // 모든 채팅방 목록 조회
    public List<RoomResponse> getAllRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();

        return rooms.stream()
                .map(room -> {
                    User creator = userRepository.findById(room.getUserId()).orElse(null);
                    return convertToRoomResponse(room, creator);
                })
                .collect(Collectors.toList());
    }

    // 채팅방 참여
    public RoomResponse joinRoom(String roomId, String userId) {
        // 채팅방 존재 확인
        ChatRoom room = chatRoomRepository.findByRoomIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 참여했는지 확인
        if (roomMemberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new RuntimeException("이미 참여한 채팅방입니다.");
        }

        // 채팅방이 가득 찬지 확인
        if (room.isFull()) {
            throw new RuntimeException("채팅방이 가득 찼습니다.");
        }

        // 멤버 추가
        RoomMember roomMember = RoomMember.builder()
                .userId(userId)
                .roomId(roomId)
                .joinedAt(LocalDateTime.now())
                .build();

        roomMemberRepository.save(roomMember);

        // 현재 멤버 수 증가
        room.increaseMemberCount();
        chatRoomRepository.save(room);

        User creator = userRepository.findById(room.getUserId()).orElse(null);
        log.info("채팅방 참여: {} joined {}", user.getUserName(), room.getRoomName());

        return convertToRoomResponse(room, creator);
    }

    // 채팅방 나가기
    public void leaveRoom(String roomId, String userId) {
        // 채팅방 존재 확인
        ChatRoom room = chatRoomRepository.findByRoomIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 참여 여부 확인
        if (!roomMemberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new RuntimeException("참여하지 않은 채팅방입니다.");
        }

        // 멤버 삭제
        roomMemberRepository.deleteByUserIdAndRoomId(userId, roomId);

        // 현재 멤버 수 감소
        room.decreaseMemberCount();
        chatRoomRepository.save(room);

        User user = userRepository.findById(userId).orElse(null);
        log.info("채팅방 나가기: {} left {}",
                user != null ? user.getUserName() : userId, room.getRoomName());
    }

    // Entity를 Response로 변환
    private RoomResponse convertToRoomResponse(ChatRoom room, User creator) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .maxRoomMember(room.getMaxRoomMember())
                .currentRoomMember(room.getCurrentRoomMember())
                .creatorId(room.getUserId())
                .creatorName(creator != null ? creator.getUserName() : "Unknown")
                .createdAt(room.getCreatedAt())
                .isFull(room.isFull())
                .build();
    }
}