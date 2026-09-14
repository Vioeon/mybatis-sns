package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.LikeToggleResponse;

import org.apache.ibatis.annotations.Param;

public interface PostLikeService {

    LikeToggleResponse toggleLike(@Param("memberId") Long memberId, @Param("postId") Long postId);
}
