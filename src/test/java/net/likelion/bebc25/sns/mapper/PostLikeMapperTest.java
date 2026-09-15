package net.likelion.bebc25.sns.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 MySQL DataSource 사용
public class PostLikeMapperTest {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Test
    @DisplayName("좋아요 등록 및 등록 여부 조회")
    void insertAndCountLikeTest(){
        // given 1번 회원이 3번 게시글에 좋아요 등록
        Long memberId = 1L;
        Long postId = 2L;

        // when 좋아요 이전/이후의 카운트 조회
        int beforeCount = postLikeMapper.countLike(memberId, postId);
        postLikeMapper.insertLike(memberId,postId);
        int afterCount = postLikeMapper.countLike(memberId, postId);

        //then 등록 전: 0, 등록 후: 1
        assertThat(beforeCount).isEqualTo(0);
        assertThat(afterCount).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소 및 취소 여부 조회")
    void deleteAndCountLikeTest(){
        // given 1번 회원이 2번 게시글의 좋아요 취소
        Long memberId = 1L;
        Long postId = 2L;
        postLikeMapper.insertLike(memberId,postId);

        // when 좋아요 취소 이전/이후의 카운트 조회
        int beforeCount = postLikeMapper.countLike(memberId, postId);
        postLikeMapper.deleteLike(memberId,postId);
        int afterCount = postLikeMapper.countLike(memberId, postId);

        //then 취소 전: 1, 취소 후: 0
        assertThat(beforeCount).isEqualTo(1);
        assertThat(afterCount).isEqualTo(0);
    }
}
