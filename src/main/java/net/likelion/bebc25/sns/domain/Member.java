package net.likelion.bebc25.sns.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder // JDBC Template 처럼 builder방식으로 가능하다
public class Member {
    // domain에 만든 것들은 테이블에 있는 모든 정보와 1대1로 가지고 있는 애들
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    @Builder.Default
    private String role = "ROLE_USER";
    private LocalDateTime createdAt;

}
