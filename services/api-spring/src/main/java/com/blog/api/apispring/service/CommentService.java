package com.blog.api.apispring.service;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.CommentInfo;
import com.blog.api.apispring.repository.CommentRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class CommentService
{
	private final CommentRepository commentRepository;

	public CommentService(CommentRepository commentRepository)
	{
		this.commentRepository = commentRepository;
	}

	public Optional<Comment> getComment(long id)
	{
		return commentRepository.findById(id);
	}

	public Optional<CommentInfo> getCommentInfo(long id)
	{
		return commentRepository.findCommentInfoById(id);
	}

	public void deleteCommentById(long id)
	{
		commentRepository.deleteById(id);
	}

	public Comment updateComment(Comment comment, String username, String body)
	{
		if (username != null)
		{
			comment.setUsername(HtmlUtils.htmlEscape(username));
		}

		if (body != null)
		{
			comment.setBody(HtmlUtils.htmlEscape(body));
		}

		return commentRepository.save(comment);
	}

	public Set<CommentInfo> getAllCommentInfoByPostId(long postId)
	{
		return commentRepository.findAllInfoByPostId(postId);
	}

	public Comment addCommentToPost(Post post, String username, String body)
	{
		Comment comment = new Comment();
		comment.setCreatedAt(OffsetDateTime.now());
		comment.setUsername(HtmlUtils.htmlEscape(username));
		comment.setBody(HtmlUtils.htmlEscape(body));
		comment.setPost(post);

		return commentRepository.save(comment);
	}
}
