package com.blog.api.apispring.dto.users;

import com.blog.api.apispring.model.User;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;

/**
 * DTO for {@link com.blog.api.apispring.model.User}
 */
public record UserDetailsDto(long id,
							 String email,
							 String name)
{
	public static UserDetailsDto fromBlogUserDetails(BlogUserDetails userDetails)
	{
		return new UserDetailsDto(userDetails.getId(), userDetails.getEmail(), userDetails.getUsername());
	}

	public static UserDetailsDto fromUser(User user)
	{
		return new UserDetailsDto(user.getId(), user.getEmail(), user.getName());
	}
}