package com.blog.api.apispring.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.AuthenticationException;

public class UserNotFoundException extends AuthenticationException
{
	private final Long id;

	private static final String DEFAULT_USER_ID_NOT_FOUND_MESSAGE = "User not found";

	public UserNotFoundException(@Nullable String msg, Long id)
	{
		super(msg);
		this.id = id;
	}

	public static UserNotFoundException fromId(Long id)
	{
		return new UserNotFoundException(DEFAULT_USER_ID_NOT_FOUND_MESSAGE, id);
	}

	public Long getId()
	{
		return id;
	}
}
