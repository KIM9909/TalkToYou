package com.talktoyou.backend.controller;

import com.talktoyou.backend.dto.request.CreateRoomRequest;
import com.talktoyou.backend.dto.request.JoinRoomRequest;
import com.talktoyou.backend.dto.response.ApiResponse;
import com.talktoyou.backend.dto.response.RoomResponse;
import com.talktoyou.backend.service.ChatRoomService;
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
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발용, 나중에 수정 필요
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final JwtUtil jwtUtil;

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
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

            RoomResponse response = chatRoomService.createRoom(request, userId);
            log.info("채팅방 생성 성공: {}", request.getRoomName());

            return ResponseEntity.ok(ApiResponse.success("채팅방이 생성되었습니다.", response));
        } catch (RuntimeException e) {
            log.error("채팅방 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "CREATE_ROOM_ERROR"));
        } catch (Exception e) {
            log.error("채팅방 생성 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 채팅방 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        try {
            List<RoomResponse> rooms = chatRoomService.getAllRooms();
            return ResponseEntity.ok(ApiResponse.success("채팅방 목록 조회 완료", rooms));
        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 채팅방 참여
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = extractToken(authHeader);
            String userId = jwtUtil.getUserIdFromToken(token);

            RoomResponse response = chatRoomService.joinRoom(roomId, userId);
            log.info("채팅방 참여 성공: roomId={}", roomId);

            return ResponseEntity.ok(ApiResponse.success("채팅방에 참여했습니다.", response));
        } catch (RuntimeException e) {
            log.error("채팅방 참여 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "JOIN_ROOM_ERROR"));
        } catch (Exception e) {
            log.error("채팅방 참여 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 채팅방 나가기
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable String roomId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // JWT 토큰에서 사용자 ID 추출
            String token = extractToken(authHeader);
            String userId = jwtUtil.getUserIdFromToken(token);

            chatRoomService.leaveRoom(roomId, userId);
            log.info("채팅방 나가기 성공: roomId={}", roomId);

            return ResponseEntity.ok(ApiResponse.success("채팅방에서 나갔습니다."));
        } catch (RuntimeException e) {
            log.error("채팅방 나가기 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "LEAVE_ROOM_ERROR"));
        } catch (Exception e) {
            log.error("채팅방 나가기 중 예상치 못한 오류", e);
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