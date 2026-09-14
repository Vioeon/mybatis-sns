package net.likelion.bebc25.sns.dto;

public record LikeToggleResponse(
        boolean liked,      // 좋아요 활성화 상태 (true: 등록, false: 취소)
        int likeCount       // 갱신된 게시글의 총 좋아요 수
) {}
