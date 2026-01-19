package com.blog.api.apispring.security.userdetails;

import com.blog.api.apispring.model.User;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements BlogUserDetails
{

	private final User user;

	public SecurityUser(User user)
	{
		this.user = user;
	}

	@Override
	public Long getId()
	{
		return user.getId();
	}

	@Override
	public String getEmail()
	{
		return user.getEmail();
	}

	@Override
	@NullMarked
	public String getUsername()
	{
		return user.getName();
	}

	@Override
	public @Nullable String getPassword()
	{
		return user.getPassword();
	}

	@Override
	@NullMarked
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return List.of();
	}
}
