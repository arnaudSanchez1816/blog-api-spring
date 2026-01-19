package com.blog.api.apispring.security.authentication;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken
{
	private final DecodedJWT decodedJWT;

	public JwtAuthenticationToken(DecodedJWT decodedJWT)
	{
		super((Collection<? extends GrantedAuthority>) null);
		this.decodedJWT = decodedJWT;
	}

	@Override
	public @Nullable Object getCredentials()
	{
		String subject = decodedJWT.getSubject();
		return Long.parseLong(subject);
	}

	@Override
	public @Nullable Object getPrincipal()
	{
		return decodedJWT;
	}
}
