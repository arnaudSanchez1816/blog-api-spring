package com.blog.api.apispring.security.userdetails.service;


import com.blog.api.apispring.exception.UserNotFoundException;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements BlogUserDetailsService
{

	private final UserService userService;

	public JpaUserDetailsService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	@NullMarked
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		return userService.findByEmail(username)
						  .map(SecurityUser::new)
						  .orElseThrow(
								  () -> new UsernameNotFoundException(String.format("User not found:%s", username)));
	}

	@Override
	public UserDetails loadUserById(@NotNull Long id) throws UserNotFoundException
	{
		return userService.findById(id)
						  .map(SecurityUser::new)
						  .orElseThrow(() -> UserNotFoundException.fromId(id));
	}
}
