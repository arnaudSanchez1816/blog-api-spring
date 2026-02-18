package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.metadata.Metadata;
import com.blog.api.apispring.dto.posts.GetPostsResponse;
import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.dto.users.GetUserPostsRequest;
import com.blog.api.apispring.dto.users.UserDetailsDto;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import com.blog.api.apispring.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController
{
	private final PostService postService;

	public UserController(PostService postService)
	{
		this.postService = postService;
	}

	@GetMapping("/me")
	public ResponseEntity<UserDetailsDto> getCurrentUser(@AuthenticationPrincipal BlogUserDetails userDetails)
	{
		return ResponseEntity.ok(UserDetailsDto.fromBlogUserDetails(userDetails));
	}

	// TODO : Fix duplication with GET /posts from PostController
	@GetMapping("/me/posts")
	public ResponseEntity<GetPostsResponse> getUserPosts(@Valid GetUserPostsRequest getPostsRequest,
														 @AuthenticationPrincipal BlogUserDetails userDetails)
	{
		Long userId = userDetails.getId();
		Page<PostInfoWithAuthorAndTags> postsPage = postService.getPageablePostsInfoByAuthor(getPostsRequest, userId);
		List<PostInfoWithAuthorAndTags> postsContent = postsPage.getContent();
		Map<Long, Long> commentsCount = postService.getCommentsCount(postsContent.stream()
																				 .map(PostInfoWithAuthor::getId)
																				 .toList());
		List<PostDto> results = postsContent.stream()
											.map(p ->
											{
												PostDto dto = new PostDto(p);
												dto.setCommentsCount(commentsCount.get(p.getId()));
												return dto;
											})
											.toList();

		Metadata metadata = new Metadata();
		metadata.count(postsPage.getTotalElements())
				.page(postsPage.getNumber())
				.pageSize(postsPage.getSize())
				.sortBy(getPostsRequest.getSortBy());
		return ResponseEntity.ok(new GetPostsResponse(results, metadata));
	}
}
