package net.likelion.bebc25.sns.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.*;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import net.likelion.bebc25.sns.service.PostLikeService;
import net.likelion.bebc25.sns.service.PostService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "게시글 API", description = "게시글 CRUD 기능")
//@RestController
@RequestMapping("/api/v1/posts")
public class PostRestControllerSwagger {

    PostService postService;
    PostLikeService postLikeService;

    public PostRestControllerSwagger(PostService postService, PostLikeService postLikeService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
    }

    // 게시글 목록 조회 (필터링 조회)
    @Operation(
            summary = "게시글 목록 조회, 검색",
            description = "게시글의 목록을 조회하거나 검색을 수행합니다."
    )
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ParameterObject
            @ModelAttribute PostSearchRequest searchRequest
    ) {
        // 검색어에 해당하는 게시글 목록 조회
        List<PostResponse> posts = postService.searchPosts(searchRequest);
        return ResponseEntity.ok(posts); // ok : 200
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Parameter(description = "회원 id", example = "1")
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
    @Operation(
            summary = "게시글 단건 조회",
            description = "id 값으로 게시글의 정보를 조회합니다.<br> 대상이 없을 경우 404에러를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    @ApiResponse(
            responseCode = "404",
            description = "게시글이 존재하지 않음.",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        // 해당 ID의 게시글 단건 조회
        PostResponse post = postService.getPostById(postId);
        return ResponseEntity.ok(post); // ok : 200
    }

    // 게시글 단건 상세 조회 (댓글 포함)
    @Operation(
            summary = "게시글 상세 조회",
            description = "id 값으로 게시글의 상세 정보를 조회합니다.<br> 대상이 없을 경우 404에러를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    @ApiResponse(
            responseCode = "404",
            description = "게시글이 존재하지 않음.",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/detail/{postId}")
    public ResponseEntity<PostDetailResponse> getDetailPost(
            @PathVariable Long postId
    ) {
        // 해당 ID의 게시글 단건 상세 조회
        PostDetailResponse detailPost = postService.getPostDetailById(postId);
        return ResponseEntity.ok(detailPost); // ok : 200
    }

    // 게시글 수정
    @Operation(
            summary = "게시글 수정",
            description = "id값에 해당하는 게시글을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검사 실패.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        // 수정 전에 게시글 정보 확인
        PostResponse beforePost = postService.getPostById(postId);

        // 본인의 게시글인지 확인
        if(!beforePost.memberId().equals(memberId)){
            throw new IllegalStateException("본인의 게시글만 수정이 가능합니다.");
        }
        // 해당 id의 게시글 수정
        postService.updatePost(postId, request);

        PostResponse updatedPost = postService.getPostById(postId);
        return ResponseEntity.ok(updatedPost); // ok : 200
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long postId
    ) {
        // 수정 전에 게시글 정보 확인
        PostResponse beforePost = postService.getPostById(postId);

        // 본인의 게시글인지 확인
        if(!beforePost.memberId().equals(memberId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
        }

        postService.deletePost(postId);

        return ResponseEntity.noContent().build(); // 204 : No Content
    }

    // 게시글 좋아요 토글 (on/off)
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
            @PathVariable Long postId
    ) {
        LikeToggleResponse result = postLikeService.toggleLike(memberId, postId);

        return ResponseEntity.ok(result); // ok : 200
    }

}
