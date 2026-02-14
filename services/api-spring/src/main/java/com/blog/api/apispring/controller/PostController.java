package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.metadata.Metadata;
import com.blog.api.apispring.dto.posts.*;
import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.exception.PostPublicationConflictException;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.CommentInfo;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import com.blog.api.apispring.service.CommentService;
import com.blog.api.apispring.service.PostService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/posts")
class PostController
{
	private final PostService postService;
	private final CommentService commentService;

	public PostController(PostService postService, CommentService commentService)
	{
		this.postService = postService;
		this.commentService = commentService;
	}

	@GetMapping
	public ResponseEntity<GetPostsResponse> getPosts(@Valid GetPostsRequest getPostsRequest,
													 Authentication authentication)
	{
		if (authentication == null || !authentication.isAuthenticated())
		{
			// Only allow viewing unpublished
			getPostsRequest.setUnpublished(false);
		}

		Page<PostInfoWithAuthorAndTags> postsPage = postService.getPostsInfo(getPostsRequest);
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

	@GetMapping("/{id}")
	public ResponseEntity<PostDto> getPost(@PathVariable long id)
	{
		Optional<PostInfoWithAuthorAndTags> optionalPost = postService.getPostInfoWithTags(id);
		if (optionalPost.isEmpty())
		{
			return ResponseEntity.notFound()
								 .build();
		}
		PostInfoWithAuthorAndTags post = optionalPost.get();
		Long commentsCount = postService.getCommentsCount(post.getId());
		PostDto dto = new PostDto(post);
		dto.setCommentsCount(commentsCount);
		return ResponseEntity.ok(dto);
	}

	@PostMapping
	public ResponseEntity<PostDto> createPost(@Valid CreatePostRequest createPostRequest,
											  @AuthenticationPrincipal BlogUserDetails userDetails)
	{
		Post newPost = postService.createPost(createPostRequest.title(), userDetails.getId());

		URI location = UriComponentsBuilder.newInstance()
										   .path("/posts/{id}")
										   .buildAndExpand(newPost.getId())
										   .toUri();
		return ResponseEntity.created(location)
							 .body(new PostDto(newPost));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('UPDATE') || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<PostInfoWithAuthorAndTags> updatePost(@PathVariable("id") @NonNull Post post,
																@Valid @RequestBody UpdatePostRequest updatePostRequest)
	{
		String title = updatePostRequest.title();
		String body = updatePostRequest.body();
		Set<TagIdOrSlug> tags = updatePostRequest.tags();

		PostInfoWithAuthorAndTags updatedPost = postService.updatePost(post, title, body, tags);

		return ResponseEntity.ok(updatedPost);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('DELETE') || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<PostDto> deletePost(@PathVariable("id") @NonNull Post post)
	{
		postService.deletePost(post.getId());

		return ResponseEntity.ok(new PostDto(post));
	}

	@GetMapping("/{id}/comments")
	@PreAuthorize("#post.isPublished() || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<GetPostCommentsResponse> getPostComments(@PathVariable("id") @NonNull Post post)
	{
		Set<CommentInfo> commentsInfo = commentService.getAllCommentInfoByPostId(post.getId());
		return ResponseEntity.ok(GetPostCommentsResponse.fromCommentsInfo(commentsInfo));
	}

	@PostMapping("/{id}/comments")
	@PreAuthorize("#post.isPublished() || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<CommentInfo> createPostComment(@PathVariable("id") @NonNull Post post, @Valid @RequestBody
	CreatePostCommentRequest createPostCommentRequest)
	{
		String username = createPostCommentRequest.username();
		String body = createPostCommentRequest.body();

		Comment comment = postService.addCommentToPost(post, username, body);
		// Todo : Comment -> CommentInfo without query
		Optional<CommentInfo> commentInfo = commentService.getCommentInfo(comment.getId());
		assert commentInfo.isPresent();

		return ResponseEntity.ok(commentInfo.get());
	}

	@PostMapping("/{id}/publish")
	@PreAuthorize("hasAuthority('UPDATE') || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<Void> publishPost(@PathVariable("id") @NonNull Post post)
	{
		if (post.isPublished())
		{
			throw PostPublicationConflictException.fromPost(post.getId());
		}

		post = postService.publishPost(post);

		return ResponseEntity.status(HttpStatus.NO_CONTENT)
							 .build();
	}

	@PostMapping("/{id}/hide")
	@PreAuthorize("hasAuthority('UPDATE') || @postSecurity.isOwner(authentication, #post)")
	public ResponseEntity<Void> hidePost(@PathVariable("id") @NonNull Post post)
	{
		if (!post.isPublished())
		{
			throw PostPublicationConflictException.fromPost(post.getId());
		}

		post = postService.hidePost(post);

		return ResponseEntity.status(HttpStatus.NO_CONTENT)
							 .build();
	}

	@ExceptionHandler(PostPublicationConflictException.class)
	public ProblemDetail handlePostPublicationConflictException(PostPublicationConflictException ex)
	{
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		pd.setTitle("Post publication conflict");
		pd.setType(URI.create("about:blank"));
		pd.setProperty("timestamp",
				OffsetDateTime.now()
							  .toString());
		pd.setProperty("postId", ex.getPostId());
		pd.setProperty("errorCode", "RES_409");

		return pd;
	}
}
