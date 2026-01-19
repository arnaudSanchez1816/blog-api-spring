package com.blog.api.apispring.security.userdetails;

import org.springframework.security.core.userdetails.UserDetails;

public interface BlogUserDetails extends UserDetails
{
	Long getId();

	String getEmail();
}
