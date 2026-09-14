package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.LikeToggleResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.mapper.PostLikeMapper;
import net.likelion.bebc25.sns.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Transactional
public class PostLikeServiceTest {
    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostMapper postMapper;

    //실제 PostMapper의 동작은 유지되지만 필요하다면 특정 메서드만 모킹(가짜생성)해서 강제 예외 발생가능
    @MockitoSpyBean
    private PostLikeMapper postLikeMapper;


    @Test
    @DisplayName("좋아요 토글, 카운트 증감 테스트")
    void toggleLikeAddTest(){
        // given : 1번 회원이 2번 게시글에 대해 좋아요 시도 (미등록 상태)
        Long memberId = 1L;
        Long postId = 3L;

        // 이전 좋아요 개수
        PostResponse beforePost = postMapper.findById(postId);
        int beforeLikeCount = beforePost.likeCount();
        // 1 : 등록상태, 0 : 미등록상태
        boolean beforeLiked = postLikeMapper.countLike(memberId, postId) > 0;

        // when : 좋아요 토글
        LikeToggleResponse result = postLikeService.toggleLike(memberId,postId);
        System.out.println("좋아요 유무 : " + result.liked() + ", 총 좋아요 수 : " + result.likeCount());

        // then
        PostResponse afterPost = postMapper.findById(postId);
        boolean afterLiked = postLikeMapper.countLike(memberId, postId) > 0;

        if(beforeLiked){ // 토글 이전에 좋아요 상태일 경우, 토글 이후에 좋아요 상태 : false, 좋아요수 -1
            assertThat(result.liked()).isFalse();
            assertThat(result.likeCount()).isEqualTo(beforeLikeCount - 1);
            assertThat(afterLiked).isFalse();

            assertThat(afterPost.likeCount()).isEqualTo(beforeLikeCount - 1);
        }else{ // 토글 이전에 좋아요 미등록 상태일 경우, 토글 이후에 좋아요 상태 : true, 좋아요수 +1
            assertThat(result.liked()).isTrue();
            assertThat(result.likeCount()).isEqualTo(beforeLikeCount + 1);
            assertThat(afterLiked).isTrue();

            assertThat(afterPost.likeCount()).isEqualTo(beforeLikeCount + 1);
        }
    }

    @Test
    @DisplayName("좋아요 토글, 카운트 수 변경 중 예외 발생 테스트")
    void toggleLikeRollbackTest(){ // 하나 성공, 하나 실패 시 정상적으로 롤백되는지
        // given : 1번 회원이 2번 게시글에 대해 좋아요 시도 (미등록 상태)
        Long memberId = 1L;
        Long postId = 2L;
        postLikeMapper.insertLike(memberId, postId);

        boolean beforeLiked = postLikeMapper.countLike(memberId, postId) > 0;

        postLikeMapper.deleteLike(memberId, postId);

//        postMapper.increaseLikeCount(postId); // 정상실행

        // 예외 발생 시킴.
        doThrow(new RuntimeException("데이터베이스 네트워크 장애 발생"))
                .when(postMapper).increaseLikeCount(postId);

        //        postMapper.increaseLikeCount(postId); // 예외 발생

        // when : 좋아요 토글
        LikeToggleResponse result = postLikeService.toggleLike(memberId,postId);

        boolean liked = postLikeMapper.countLike(memberId, postId) > 0;
        assertThat(liked).isFalse();
    }
}
