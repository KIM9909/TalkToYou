package com.talktoyou.backend.controller;

import com.talktoyou.backend.dto.request.SendMessageRequest;
import com.talktoyou.backend.dto.response.ApiResponse;
import com.talktoyou.backend.dto.response.MessageResponse;
import com.talktoyou.backend.service.MessageService;
import com.talktoyou.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발용, 나중에 수정 필요
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    // 메시지 전송
    @PostMapping("/{roomId}")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable String roomId,
            @Valid @RequestBody SendMessageRequest request,
            BindingResult bindingResult,
            @RequestHeader("Authorization") String authHeader) {

        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("입력값 검증에 실패했습니다.", "VALIDATION_ERROR"));
        }

        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = extractToken(authHeader);
            String userId = jwtUtil.getUserIdFromToken(token);

            MessageResponse response = messageService.sendMessage(roomId, request, userId);
            log.info("메시지 전송 성공: roomId={}", roomId);

            return ResponseEntity.ok(ApiResponse.success("메시지가 전송되었습니다.", response));
        } catch (RuntimeException e) {
            log.error("메시지 전송 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "SEND_MESSAGE_ERROR"));
        } catch (Exception e) {
            log.error("메시지 전송 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 채팅방 메시지 목록 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getRoomMessages(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = extractToken(authHeader);
            String userId = jwtUtil.getUserIdFromToken(token);

            List<MessageResponse> messages = messageService.getRoomMessages(roomId, userId);
            log.info("메시지 목록 조회 성공: roomId={}, count={}", roomId, messages.size());

            return ResponseEntity.ok(ApiResponse.success("메시지 목록 조회 완료", messages));
        } catch (RuntimeException e) {
            log.error("메시지 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "GET_MESSAGES_ERROR"));
        } catch (Exception e) {
            log.error("메시지 목록 조회 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // JWT 토큰 추출 헬퍼 메서드
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("유효하지 않은 토큰 형식입니다.");
        }
        return authHeader.substring(7);
    }
}