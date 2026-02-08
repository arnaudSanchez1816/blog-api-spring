package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.comment.UpdateCommentRequest;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.projection.CommentInfo;
import com.blog.api.apispring.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/comments")
class CommentController
{
	private final CommentService commentService;

	public CommentController(CommentService commentService)
	{
		this.commentService = commentService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<CommentInfo> getComment(@PathVariable long id)
	{
		Optional<CommentInfo> optionalCommentInfo = commentService.getCommentInfo(id);
		if (optionalCommentInfo.isEmpty())
		{
			return ResponseEntity.notFound()
								 .build();
		}

		return ResponseEntity.ok(optionalCommentInfo.get());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('UPDATE')")
	public ResponseEntity<CommentInfo> updateComment(@PathVariable long id,
													 @Valid @RequestBody UpdateCommentRequest request)
	{
		Optional<Comment> optionalComment = commentService.getComment(id);
		if (optionalComment.isEmpty())
		{
			return ResponseEntity.notFound()
								 .build();
		}

		String newUsername = request.username();
		String newBody = request.body();

		Comment comment = optionalComment.get();
		comment = commentService.updateComment(comment, newUsername, newBody);
		// TODO : Comment -> CommentInfo without query ?
		Optional<CommentInfo> commentInfo = commentService.getCommentInfo(comment.getId());
		assert commentInfo.isPresent();

		return ResponseEntity.ok(commentInfo.get());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('DELETE')")
	public ResponseEntity<CommentInfo> deleteComment(@PathVariable long id)
	{
		Optional<CommentInfo> optionalCommentInfo = commentService.getCommentInfo(id);
		if (optionalCommentInfo.isEmpty())
		{
			return ResponseEntity.notFound()
								 .build();
		}

		commentService.deleteCommentById(id);

		return ResponseEntity.ok(optionalCommentInfo.get());
	}
}
