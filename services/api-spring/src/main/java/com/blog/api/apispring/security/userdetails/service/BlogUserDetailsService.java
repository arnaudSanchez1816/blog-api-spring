package com.blog.api.apispring.security.userdetails.service;

import com.blog.api.apispring.exception.UserNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface BlogUserDetailsService extends UserDetailsService
{
	/**
	 * Locates the user based on the id. In the actual implementation.
	 *
	 * @param id the id identifying the user whose data is required.
	 * @return a fully populated user record (never <code>null</code>)
	 * @throws UserNotFoundException if the user could not be found
	 */
	UserDetails loadUserById(Long id) throws UserNotFoundException;
}
