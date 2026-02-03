package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.metadata.Metadata;
import com.blog.api.apispring.dto.posts.GetPostsRequest;
import com.blog.api.apispring.dto.posts.GetPostsResponse;
import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
class PostController
{
	private final PostService postService;

	public PostController(PostService postService)
	{
		this.postService = postService;
	}

	@GetMapping
	public ResponseEntity<GetPostsResponse> getPosts(@Valid GetPostsRequest getPostsRequest)
	{
		int page = getPostsRequest.page();
		int pageSize = getPostsRequest.pageSize();
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<PostInfoWithAuthorAndTags> postsPage = postService.getPosts(pageable);
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
				.pageSize(postsPage.getSize());
		return ResponseEntity.ok(new GetPostsResponse(results, metadata));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostDto> getPost(@PathVariable long id)
	{
		Optional<PostInfoWithAuthorAndTags> optionalPost = postService.getPostWithTags(id);
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
	public ResponseEntity<PostInfoWithAuthor> createPost() throws URISyntaxException
	{
		return ResponseEntity.created(new URI("/"))
							 .build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<PostInfoWithAuthor> updatePost(@PathVariable long id)
	{
		return ResponseEntity.ok(null);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<PostInfoWithAuthor> deletePost(@PathVariable long id)
	{
		return ResponseEntity.ok(null);
	}

	@GetMapping("/{id}/comments")
	public ResponseEntity<Void> getPostComments(@PathVariable long id)
	{
		return ResponseEntity.notFound()
							 .build();
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<Void> createPostComment(@PathVariable long id)
	{
		return ResponseEntity.notFound()
							 .build();
	}
}
