package net.likelion.bebc25.sns.controller;

import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.*;
import net.likelion.bebc25.sns.mapper.PostMapper;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import net.likelion.bebc25.sns.service.PostLikeService;
import net.likelion.bebc25.sns.service.PostService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {

    PostService postService;
    PostLikeService postLikeService;

    public PostRestController(PostService postService, PostLikeService postLikeService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
    }

    // 게시글 목록 조회 (필터링 조회)
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ModelAttribute PostSearchRequest searchRequest
    ) {
        // 검색어에 해당하는 게시글 목록 조회
        List<PostResponse> posts = postService.searchPosts(searchRequest);
        return ResponseEntity.ok(posts); // ok : 200
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request // json 요청 바디를 객체로 자동 매핑
    ) {
        request.setMemberId(userDetails.getId());

        // 게시글 등록
        PostResponse createdPost = postService.createPost(request);
        // 필수는 아님. 이거하면 restfull 해짐
        URI location = URI.create("/api/v1/posts/" + createdPost.id());
        return ResponseEntity.created(location).body(createdPost); // created : 201
    }

    // 게시글 단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        // 해당 ID의 게시글 단건 조회
        PostResponse post = postService.getPostById(postId);
        return ResponseEntity.ok(post); // ok : 200
    }

    // 게시글 단건 상세 조회 (댓글 포함)
    @GetMapping("/detail/{postId}")
    public ResponseEntity<PostDetailResponse> getDetailPost(
            @PathVariable Long postId
    ) {
        // 해당 ID의 게시글 단건 상세 조회
        PostDetailResponse detailPost = postService.getPostDetailById(postId);
        return ResponseEntity.ok(detailPost); // ok : 200
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
//            @RequestHeader("X-Member-Id") Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostUpdateRequest request
    ) {
        // 해당 id의 게시글 수정
        postService.updatePost(postId, request);

        // 수정된 게시글 조회
        PostResponse updatedPost = postService.getPostById(postId);
        return ResponseEntity.ok(updatedPost); // ok : 200
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
//            @RequestHeader("X-Member-Id") Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);

        return ResponseEntity.noContent().build(); // 204 : No Content
    }

    // 게시글 좋아요 토글 (on/off)
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        LikeToggleResponse result = postLikeService.toggleLike(userDetails.getId(), postId);

        return ResponseEntity.ok(result); // ok : 200
    }

}
