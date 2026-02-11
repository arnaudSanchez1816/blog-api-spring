package com.blog.api.apispring.exception;

import org.jspecify.annotations.Nullable;

public class PostPublicationConflictException extends RuntimeException
{
	private final long postId;

	private static final String DEFAULT_CONFLICT_MESSAGE = "Post already published/unpublished";

	public PostPublicationConflictException(@Nullable String message, long postId)
	{
		super(message);
		this.postId = postId;
	}

	public static PostPublicationConflictException fromPost(long postId)
	{
		return new PostPublicationConflictException(DEFAULT_CONFLICT_MESSAGE, postId);
	}

	public long getPostId()
	{
		return this.postId;
	}
}
