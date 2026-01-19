package com.blog.api.apispring.security.userdetails.service;

import com.blog.api.apispring.exception.UserNotFoundException;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class BlogInMemoryUserDetailsService implements BlogUserDetailsService
{
	private final Map<String, BlogUserDetails> userByName;
	private final Map<Long, BlogUserDetails> userById;

	public BlogInMemoryUserDetailsService(BlogUserDetails... users)
	{
		this.userByName = Arrays.stream(users)
								.collect(toMap(BlogUserDetails::getUsername, Function.identity()));
		this.userById = Arrays.stream(users)
							  .collect(toMap(BlogUserDetails::getId, Function.identity()));
	}

	@Override
	public UserDetails loadUserById(Long id) throws UserNotFoundException
	{
		BlogUserDetails user = userById.get(id);
		if (user == null)
		{
			throw new UserNotFoundException("User not found", id);
		}
		return user;
	}

	@Override
	public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException
	{
		BlogUserDetails user = userByName.get(username);
		if (user == null)
		{
			throw new UsernameNotFoundException(username);
		}
		return user;
	}
}
