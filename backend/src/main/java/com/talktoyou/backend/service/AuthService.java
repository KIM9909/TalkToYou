package com.talktoyou.backend.service;

import com.talktoyou.backend.dto.request.SignUpRequest;
import com.talktoyou.backend.dto.request.LoginRequest;
import com.talktoyou.backend.dto.response.AuthResponse;
import com.talktoyou.backend.entity.User;
import com.talktoyou.backend.repository.UserRepository;
import com.talktoyou.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration;

    // 회원가입
    public AuthResponse signUp(SignUpRequest request) {
        // 중복 검사
        if (userRepository.existsByUserNameAndDeletedAtIsNull(request.getUserName())) {
            throw new RuntimeException("이미 사용 중인 사용자명입니다.");
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .userName(request.getUserName())
                .nickName(request.getNickName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getUserName());

        log.info("새 사용자 회원가입 완료: {}", savedUser.getUserName());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .nickName(savedUser.getNickName())
                .email(savedUser.getEmail())
                .loginTime(LocalDateTime.now())
                .expiresIn(jwtExpiration / 1000) // 초 단위로 변환
                .build();
    }

    // 로그인
    public AuthResponse login(LoginRequest request) {
        // 사용자 찾기
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }

        User user = userOptional.get();

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("잘못된 비밀번호입니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getUserName());

        log.info("사용자 로그인: {}", user.getUserName());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .userId(user.getUserId())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .loginTime(LocalDateTime.now())
                .expiresIn(jwtExpiration / 1000) // 초 단위로 변환
                .build();
    }

    // 로그아웃
    public void logout(String token) {
        try {
            // 토큰 유효성 검사
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("유효하지 않은 토큰입니다.");
            }

            // 토큰 만료 시간 가져오기
            var expirationDate = jwtUtil.getExpirationDateFromToken(token);

            // 블랙리스트에 추가
            tokenBlacklistService.addToBlacklist(token, expirationDate);

            String userName = jwtUtil.getUserNameFromToken(token);
            log.info("사용자 로그아웃: {}", userName);

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            throw new RuntimeException("로그아웃 처리에 실패했습니다.");
        }
    }

    // 토큰 검증 (블랙리스트 포함)
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token) && !tokenBlacklistService.isBlacklisted(token);
    }

    // 토큰에서 사용자 정보 가져오기
    public User getUserFromToken(String token) {
        String userId = jwtUtil.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}