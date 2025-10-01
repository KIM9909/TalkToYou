package com.talktoyou.backend.config;

import com.talktoyou.backend.dto.ChatMessage;
import com.talktoyou.backend.entity.User;
import com.talktoyou.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    // WebSocket 연결 이벤트
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("새로운 WebSocket 연결이 수립되었습니다");
    }

    // WebSocket 연결 해제 이벤트
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        if (userId != null && roomId != null) {
            log.info("사용자 연결 해제: userId={}, roomId={}", userId, roomId);

            // 사용자 정보 조회
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                // 퇴장 메시지 생성 및 브로드캐스트
                ChatMessage chatMessage = ChatMessage.builder()
                        .type(ChatMessage.MessageType.LEAVE)
                        .roomId(roomId)
                        .userId(userId)
                        .userName(user.getUserName())
                        .content(user.getUserName() + "님이 퇴장했습니다.")
                        .timestamp(LocalDateTime.now())
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);
                log.info("자동 퇴장 알림 전송 완료: user={}", user.getUserName());
            }
        }
    }
}