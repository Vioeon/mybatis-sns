package net.likelion.bebc25.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    @Schema(hidden = true) // 문서화 하고 싶지 않을 때
    private Long id;

    @Schema(hidden = true) // 문서화 하고 싶지 않을 때
    private Long memberId;

    @Schema(description = "게시글 본문 내용", example = "스프링 부트 학습중...", requiredMode = Schema.RequiredMode.AUTO) // 필수 여부
    @NotBlank(message = "본문 내용은 필수 입력 항목입니다.")
    @Size(max = 1000, message = "본문은 1000자 이하여야 합니다.")
    private String content;

    @Schema(description = "첨부 이미지 URL", example = "http://sample.com/images/hello.png", nullable = true) // 없어도 된다.
    private String imageUrl;

    // 클라이언트 요청 바디의 JSON을 역직렬화 할때 사용되는 생성자 추가
    public PostCreateRequest(Long memberId, String content, String imageUrl) {
        this.memberId = memberId;
        this.content = content;
        this.imageUrl = imageUrl;
    }
}