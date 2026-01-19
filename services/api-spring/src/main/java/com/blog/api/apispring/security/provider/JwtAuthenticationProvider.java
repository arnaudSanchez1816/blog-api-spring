package com.blog.api.apispring.security.provider;

import com.blog.api.apispring.security.authentication.JwtAuthenticationToken;
import com.blog.api.apispring.security.userdetails.service.BlogUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;

@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider
{
	private final BlogUserDetailsService userDetailsService;

	private static final String AUTHORITY = FactorGrantedAuthority.BEARER_AUTHORITY;

	public JwtAuthenticationProvider(BlogUserDetailsService userDetailsService)
	{
		this.userDetailsService = userDetailsService;
	}

	@Override
	public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException
	{
		Assert.isInstanceOf(JwtAuthenticationToken.class, authentication,
							() -> "Only JwtAuthenticationToken is supported");
		JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
		Long userId = (Long) jwtAuthenticationToken.getCredentials();
		UserDetails user = userDetailsService.loadUserById(userId);
		if (user == null)
		{
			throw new InternalAuthenticationServiceException(
					"UserDetailsService returned null, which is an interface contract violation");
		}

		return createSuccessAuthentication(user, authentication, user);
	}

	@Override
	public boolean supports(@NonNull Class<?> authentication)
	{
		return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
	}

	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
														 UserDetails user)
	{
		Collection<GrantedAuthority> authorities = new LinkedHashSet<>(user.getAuthorities());
		authorities.add(FactorGrantedAuthority.fromAuthority(AUTHORITY));
		UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken.authenticated(principal,
																									   authentication.getCredentials(),
																									   authorities);
		log.debug("Authenticated user");
		return result;
	}
}
