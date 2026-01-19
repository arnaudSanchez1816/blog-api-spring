package com.blog.api.apispring.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.blog.api.apispring.security.authentication.JwtAuthenticationToken;
import com.blog.api.apispring.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class AccessJwtAuthenticationFilter extends OncePerRequestFilter
{
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;


	public AccessJwtAuthenticationFilter(JwtService jwtService, AuthenticationManager authenticationManager)
	{
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException
	{
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight

		String pathWithoutContext = request.getRequestURI()
										   .substring(request.getContextPath()
															 .length());
		// TODO: Add other public endpoints if needed
		return pathWithoutContext.equals("/auth/token");
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
									@NonNull FilterChain chain) throws ServletException, IOException
	{
		SecurityContext securityContext = SecurityContextHolder.getContext();
		// If already authenticated, skip
		Authentication authentication = securityContext.getAuthentication();
		if (authentication != null)
		{
			chain.doFilter(request, response);
			return;
		}

		log.info("Access token authentication...");
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith(BEARER_PREFIX))
		{
			log.debug("Bearer Authorization header is missing or invalid.");
			chain.doFilter(request, response);
			return;
		}

		try
		{
			// Extract token from header
			String token = header.substring(BEARER_PREFIX.length());
			DecodedJWT decodedJWT = jwtService.validateAccessToken(token);
			JwtAuthenticationToken authRequest = new JwtAuthenticationToken(decodedJWT);
			authRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			Authentication authResult = this.authenticationManager.authenticate(authRequest);
			securityContext.setAuthentication(authResult);
			if (log.isDebugEnabled())
			{
				log.debug("Set SecurityContextHolder to {}", authResult);
			}
		} catch (Exception e)
		{
			SecurityContextHolder.clearContext();
			log.debug("Failed to verify access JWT", e);
		}
		chain.doFilter(request, response);
	}
}
