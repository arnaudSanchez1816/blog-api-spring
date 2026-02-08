package com.blog.api.apispring.projection;

import java.time.OffsetDateTime;

/**
 * Projection for {@link com.blog.api.apispring.model.Comment}
 */
public interface CommentInfo
{
	Long getId();

	String getUsername();

	String getBody();

	OffsetDateTime getCreatedAt();

	long getPostId();
}