package net.likelion.bebc25.sns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @NotBlank(message = "수정할 본문 내용은 필수 입력 항목입니다.")
        @Size(max = 1000, message = "본문은 1000자 이하여야 합니다.")
        String content,

        String imageUrl
) {
        // 클라이언트 요청 바디의 JSON을 역직렬화 할때 사용되는 생성자 추가
        public PostUpdateRequest(String content, String imageUrl) {
                this.content = content;
                this.imageUrl = imageUrl;
        }
}