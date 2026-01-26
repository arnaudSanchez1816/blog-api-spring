package com.blog.api.apispring.projection;

import java.time.OffsetDateTime;

/**
 * Projection for {@link com.blog.api.apispring.model.Post}
 */
public interface PostInfoWithAuthor
{
	Long getId();

	String getTitle();

	String getDescription();

	String getBody();

	int getReadingTime();

	OffsetDateTime getPublishedAt();

	UserInfo getAuthor();

	/**
	 * Projection for {@link com.blog.api.apispring.model.User}
	 */
	interface UserInfo
	{
		Long getId();

		String getName();
	}
}