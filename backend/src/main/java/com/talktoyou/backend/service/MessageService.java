package com.talktoyou.backend.service;

import com.talktoyou.backend.dto.request.SendMessageRequest;
import com.talktoyou.backend.dto.response.MessageResponse;
import com.talktoyou.backend.entity.ChatRoom;
import com.talktoyou.backend.entity.Message;
import com.talktoyou.backend.entity.User;
import com.talktoyou.backend.repository.ChatRoomRepository;
import com.talktoyou.backend.repository.MessageRepository;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    // 메시지 전송
    public MessageResponse sendMessage(String roomId, SendMessageRequest request, String userId) {
        // 채팅방 존재 확인
        ChatRoom room = chatRoomRepository.findByRoomIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 채팅방 참여 여부 확인
        if (!roomMemberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new RuntimeException("채팅방에 참여하지 않은 사용자입니다.");
        }

        // 메시지 생성
        Message message = Message.builder()
                .userId(userId)
                .roomId(roomId)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        log.info("메시지 전송 완료: {} in room {}", user.getUserName(), room.getRoomName());

        return convertToMessageResponse(savedMessage, user);
    }

    // 특정 채팅방의 메시지 목록 조회
    public List<MessageResponse> getRoomMessages(String roomId, String userId) {
        // 채팅방 존재 확인
        ChatRoom room = chatRoomRepository.findByRoomIdAndDeletedAtIsNull(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 채팅방 참여 여부 확인
        if (!roomMemberRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new RuntimeException("채팅방에 참여하지 않은 사용자입니다.");
        }

        // 최근 50개 메시지 조회 (시간 역순으로 가져온 후 다시 정순으로 정렬)
        List<Message> messages = messageRepository.findTop50ByRoomIdOrderByCreatedAtDesc(roomId);

        // 정순으로 다시 정렬 (오래된 것부터)
        messages = messages.stream()
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .collect(Collectors.toList());

        // MessageResponse로 변환
        return messages.stream()
                .map(message -> {
                    User messageUser = userRepository.findById(message.getUserId()).orElse(null);
                    return convertToMessageResponse(message, messageUser);
                })
                .collect(Collectors.toList());
    }

    // Entity를 Response로 변환
    private MessageResponse convertToMessageResponse(Message message, User user) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .userName(user != null ? user.getUserName() : "Unknown")
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}