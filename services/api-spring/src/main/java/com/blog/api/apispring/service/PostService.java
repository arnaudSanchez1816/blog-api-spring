package com.blog.api.apispring.service;

import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.*;
import com.blog.api.apispring.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostService
{
	private final PostRepository postRepository;

	public PostService(PostRepository postRepository)
	{
		this.postRepository = postRepository;
	}

	public Optional<PostInfoWithAuthor> getPost(long id)
	{
		return postRepository.findWithAuthorById(id);
	}

	public Optional<PostInfoWithAuthorAndTags> getPostWithTags(long id)
	{
		return postRepository.findWithTagsById(id);
	}

	public Optional<PostInfoWithAuthorAndComments> getPostWithComments(long id)
	{
		return postRepository.findWithCommentsById(id);
	}

	public Optional<PostInfoWithAuthorTagsComments> getPostsWithTagsComments(long id)
	{
		return postRepository.findWithTagsAndCommentsById(id);
	}

	public Page<PostInfoWithAuthorAndTags> getPosts(Pageable pageable)
	{
		if (pageable == null)
		{
			pageable = Pageable.unpaged();
		}

		return postRepository.findAllWithTags(pageable);
	}

	public Long getCommentsCount(long id)
	{
		return postRepository.countCommentsById(id);
	}

	public Map<Long, Long> getCommentsCount(List<Long> postIds)
	{
		if (postIds == null || postIds.isEmpty())
		{
			return Collections.emptyMap();
		}

		return postRepository.countCommentsByIds(postIds);
	}
}
