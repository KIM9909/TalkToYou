package com.talktoyou.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다")
    private String content;
}