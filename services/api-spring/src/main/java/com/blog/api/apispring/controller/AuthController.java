package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.GetTokenResponse;
import com.blog.api.apispring.dto.LoginRequest;
import com.blog.api.apispring.dto.LoginResponse;
import com.blog.api.apispring.dto.users.UserDetailsDto;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import com.blog.api.apispring.service.JwtService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.blog.api.apispring.security.filter.RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE;

@RestController
@RequestMapping("/auth")
class AuthController
{
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ServletContext servletContext;

	// 30 days
	private static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
						  ServletContext servletContext)
	{
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.servletContext = servletContext;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response)
	{
		Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(),
				loginRequest.password());
		Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
		BlogUserDetails userDetails = (BlogUserDetails) authenticationResponse.getPrincipal();
		if (userDetails == null)
		{
			throw new RuntimeException("Unexpected User details type");
		}

		// Generate refresh token
		Long userId = userDetails.getId();
		String username = userDetails.getUsername();
		String email = userDetails.getEmail();
		String refreshToken = jwtService.generateRefreshToken(userId, username, email);

		Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
		refreshTokenCookie.setPath(servletContext.getContextPath() + "/token");
		refreshTokenCookie.setAttribute("SameSite", "Strict");
		response.addCookie(refreshTokenCookie);

		// Generate access token
		String accessToken = generateAccessToken(userDetails);

		return ResponseEntity.ok(new LoginResponse(accessToken, UserDetailsDto.fromBlogUserDetails(userDetails)));
	}

	@GetMapping("/token")
	public ResponseEntity<GetTokenResponse> getToken(@AuthenticationPrincipal BlogUserDetails userDetails)
	{
		String accessToken = generateAccessToken(userDetails);

		return ResponseEntity.ok(new GetTokenResponse(accessToken));
	}

	private String generateAccessToken(BlogUserDetails userDetails)
	{
		Long userId = userDetails.getId();
		String username = userDetails.getUsername();
		String email = userDetails.getEmail();
		return jwtService.generateAccessToken(userId, username, email);
	}
}
