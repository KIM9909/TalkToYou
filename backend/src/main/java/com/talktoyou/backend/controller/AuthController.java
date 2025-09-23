package com.talktoyou.backend.controller;

import com.talktoyou.backend.dto.request.SignUpRequest;
import com.talktoyou.backend.dto.request.LoginRequest;
import com.talktoyou.backend.dto.response.AuthResponse;
import com.talktoyou.backend.dto.response.ApiResponse;
import com.talktoyou.backend.dto.response.TokenValidationResponse;
import com.talktoyou.backend.dto.response.ErrorResponse;
import com.talktoyou.backend.service.AuthService;
import com.talktoyou.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발용, 나중에 수정 필요
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signUp(
            @Valid @RequestBody SignUpRequest request,
            BindingResult bindingResult) {

        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> ErrorResponse.FieldError.builder()
                            .field(error.getField())
                            .rejectedValue(error.getRejectedValue())
                            .message(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("입력값 검증에 실패했습니다.", "VALIDATION_ERROR"));
        }

        try {
            AuthResponse response = authService.signUp(request);
            log.info("회원가입 성공: {}", request.getUserName());
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
        } catch (RuntimeException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "SIGNUP_ERROR"));
        } catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("입력값이 올바르지 않습니다.", "VALIDATION_ERROR"));
        }

        try {
            AuthResponse response = authService.login(request);
            log.info("로그인 성공: {}", request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
        } catch (RuntimeException e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "LOGIN_ERROR"));
        } catch (Exception e) {
            log.error("로그인 처리 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("유효하지 않은 토큰 형식입니다.", "INVALID_TOKEN_FORMAT"));
            }

            String token = authHeader.substring(7); // "Bearer " 제거
            authService.logout(token);

            return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
        } catch (RuntimeException e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "LOGOUT_ERROR"));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 예상치 못한 오류", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }

    // 토큰 검증
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                TokenValidationResponse response = TokenValidationResponse.builder()
                        .valid(false)
                        .message("유효하지 않은 토큰 형식입니다.")
                        .build();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("유효하지 않은 토큰 형식입니다.", "INVALID_TOKEN_FORMAT"));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                String userId = jwtUtil.getUserIdFromToken(token);
                String userName = jwtUtil.getUserNameFromToken(token);
                Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
                LocalDateTime expirationTime = expirationDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                long remainingTime = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

                TokenValidationResponse response = TokenValidationResponse.builder()
                        .valid(true)
                        .message("유효한 토큰입니다.")
                        .userId(userId)
                        .userName(userName)
                        .expirationTime(expirationTime)
                        .remainingTime(remainingTime)
                        .build();

                return ResponseEntity.ok(ApiResponse.success("토큰 검증 완료", response));
            } else {
                TokenValidationResponse response = TokenValidationResponse.builder()
                        .valid(false)
                        .message("유효하지 않은 토큰입니다.")
                        .build();

                return ResponseEntity.badRequest()
                        .body(ApiResponse.success("토큰 검증 완료", response));
            }
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("서버 내부 오류가 발생했습니다.", "INTERNAL_ERROR"));
        }
    }
}