package com.blog.api.apispring.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.blog.api.apispring.security.authentication.JwtAuthenticationToken;
import com.blog.api.apispring.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.web.util.WebUtils.getCookie;

@Slf4j
public class RefreshJwtAuthenticationFilter extends OncePerRequestFilter
{
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public static final String REFRESH_TOKEN_COOKIE = "__Http-REFRESHTOKEN";

	public RefreshJwtAuthenticationFilter(JwtService jwtService, AuthenticationManager authenticationManager)
	{
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
	{
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight

		String pathWithoutContext = request.getRequestURI()
										   .substring(request.getContextPath()
															 .length());
		return !pathWithoutContext.equals("/auth/token");
	}

	@Override
	@NullMarked
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
																												 ServletException,
																												 IOException
	{
		log.info("Refresh token authentication...");
		Cookie refreshTokenCookie = getCookie(request, REFRESH_TOKEN_COOKIE);
		if (refreshTokenCookie == null)
		{
			log.debug("Missing refresh JWT cookie");
			chain.doFilter(request, response);
			return;
		}

		try
		{
			// Extract token from cookie
			String token = refreshTokenCookie.getValue();
			DecodedJWT decodedJWT = jwtService.validateRefreshToken(token);
			JwtAuthenticationToken authRequest = new JwtAuthenticationToken(decodedJWT);

			SecurityContext securityContext = SecurityContextHolder.getContext();
			Authentication authResult = this.authenticationManager.authenticate(authRequest);
			securityContext.setAuthentication(authResult);
			if (log.isDebugEnabled())
			{
				log.debug("Set SecurityContextHolder to {}", authResult);
			}
		} catch (Exception e)
		{
			SecurityContextHolder.clearContext();
			log.debug("Failed to verify refresh JWT", e);
		}
		chain.doFilter(request, response);
	}
}
