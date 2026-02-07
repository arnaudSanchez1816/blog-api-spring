package com.blog.api.apispring.security;

import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("postSecurity")
public class PostSecurityService
{
	public boolean isOwner(Authentication authentication, Post post)
	{
		if (authentication == null || post == null)
		{
			return false;
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof BlogUserDetails userDetails))
		{
			return false;
		}

		return post.getAuthor().getId().equals(userDetails.getId());
	}
}
