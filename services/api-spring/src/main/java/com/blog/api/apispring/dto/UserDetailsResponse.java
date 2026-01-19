package com.blog.api.apispring.dto;

import com.blog.api.apispring.security.userdetails.BlogUserDetails;

public record UserDetailsResponse(String email, Long id, String name)
{
	public UserDetailsResponse(BlogUserDetails userDetails)
	{
		this(userDetails.getEmail(), userDetails.getId(), userDetails.getUsername());
	}
}
