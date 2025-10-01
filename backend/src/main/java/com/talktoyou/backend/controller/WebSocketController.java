package com.talktoyou.backend.controller;

import com.talktoyou.backend.dto.ChatMessage;
import com.talktoyou.backend.entity.Message;
import com.talktoyou.backend.entity.User;
import com.talktoyou.backend.repository.MessageRepository;
import com.talktoyou.backend.repository.RoomMemberRepository;
import com.talktoyou.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;

    // 채팅 메시지 전송
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessage chatMessage) {
        try {
            log.info("메시지 수신: roomId={}, user={}, type={}",
                    roomId, chatMessage.getUserName(), chatMessage.getType());

            // 채팅방 참여 여부 확인
            if (!roomMemberRepository.existsByUserIdAndRoomId(chatMessage.getUserId(), roomId)) {
                log.warn("채팅방에 참여하지 않은 사용자의 메시지: userId={}", chatMessage.getUserId());
                return;
            }

            // 일반 채팅 메시지인 경우 DB에 저장
            if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
                Message message = Message.builder()
                        .userId(chatMessage.getUserId())
                        .roomId(roomId)
                        .content(chatMessage.getContent())
                        .createdAt(LocalDateTime.now())
                        .build();

                messageRepository.save(message);
                log.info("메시지 DB 저장 완료: messageId={}", message.getMessageId());
            }

            // 타임스탬프 설정
            chatMessage.setTimestamp(LocalDateTime.now());

            // 해당 채팅방 구독자들에게 브로드캐스트
            messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);

            log.info("메시지 브로드캐스트 완료: roomId={}", roomId);

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
        }
    }

    // 사용자 입장 알림
    @MessageMapping("/chat/{roomId}/join")
    public void userJoin(@DestinationVariable String roomId,
                         @Payload ChatMessage chatMessage,
                         SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("사용자 입장: roomId={}, user={}", roomId, chatMessage.getUserName());

            // 세션에 사용자 정보 저장
            headerAccessor.getSessionAttributes().put("userId", chatMessage.getUserId());
            headerAccessor.getSessionAttributes().put("roomId", roomId);

            chatMessage.setType(ChatMessage.MessageType.JOIN);
            chatMessage.setTimestamp(LocalDateTime.now());

            // 입장 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);

            log.info("입장 알림 완료: roomId={}, user={}", roomId, chatMessage.getUserName());

        } catch (Exception e) {
            log.error("사용자 입장 처리 중 오류 발생", e);
        }
    }

    // 사용자 퇴장 알림
    @MessageMapping("/chat/{roomId}/leave")
    public void userLeave(@DestinationVariable String roomId,
                          @Payload ChatMessage chatMessage) {
        try {
            log.info("사용자 퇴장: roomId={}, user={}", roomId, chatMessage.getUserName());

            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setTimestamp(LocalDateTime.now());

            // 퇴장 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);

            log.info("퇴장 알림 완료: roomId={}, user={}", roomId, chatMessage.getUserName());

        } catch (Exception e) {
            log.error("사용자 퇴장 처리 중 오류 발생", e);
        }
    }
}