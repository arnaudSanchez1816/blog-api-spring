package com.blog.api.apispring.security.userdetails;

import com.blog.api.apispring.model.User;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

public class SecurityUser implements BlogUserDetails
{
	private final User user;
	private final Set<GrantedAuthority> authorities;

	public SecurityUser(User user)
	{
		this.user = user;
		this.authorities = Set.of();
	}

	public SecurityUser(User user, Collection<? extends GrantedAuthority> authorities)
	{
		this.user = user;
		this.authorities = Set.copyOf(authorities);
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
		return this.authorities;
	}
}
