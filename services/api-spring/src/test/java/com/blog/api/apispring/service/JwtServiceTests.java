package com.blog.api.apispring.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests
{
	private JwtService jwtService;

	private String validAccessToken;
	private String validRefreshToken;
	private Long testUserId = 1L;
	private String testUsername = "testuser";
	private String testEmail = "test@example.com";

	@BeforeEach
	void setUp()
	{
		jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "jwtAccessSecret", "accessSecret");
		ReflectionTestUtils.setField(jwtService, "jwtRefreshSecret", "refreshSecret");
		validAccessToken = jwtService.generateAccessToken(testUserId, testUsername, testEmail);
		validRefreshToken = jwtService.generateRefreshToken(testUserId, testUsername, testEmail);
	}

	@Test
	void validateAccessToken_WithValidToken_ShouldReturnDecodedJWT()
	{
		DecodedJWT decodedJWT = jwtService.validateAccessToken(validAccessToken);

		assertNotNull(decodedJWT);
		assertEquals(String.valueOf(testUserId), decodedJWT.getSubject());
		assertEquals(testUsername,
				decodedJWT.getClaim("name")
						  .asString());
		assertEquals(testEmail,
				decodedJWT.getClaim("email")
						  .asString());
		assertEquals("blog-api", decodedJWT.getIssuer());
	}

	@Test
	void validateAccessToken_WithCustomExpiration_DoesNotThrow()
	{
		Instant forwardTime = Instant.now()
									 .plus(50, ChronoUnit.DAYS);
		String token = jwtService.generateAccessToken(testUserId, testUsername, testEmail, forwardTime);

		assertDoesNotThrow(() ->
		{
			jwtService.validateAccessToken(token);
		});
	}

	@Test
	void validateAccessToken_WithExpiredToken_ShouldThrowTokenExpiredException()
	{
		Instant pastTime = Instant.now()
								  .minus(1, ChronoUnit.DAYS);
		String expiredToken = jwtService.generateAccessToken(testUserId, testUsername, testEmail, pastTime);

		assertThrows(TokenExpiredException.class, () ->
		{
			jwtService.validateAccessToken(expiredToken);
		});
	}

	@Test
	void validateAccessToken_WithInvalidSignature_ShouldThrowJWTVerificationException()
	{
		String tokenWithInvalidSignature = validAccessToken.substring(0, validAccessToken.length() - 5) + "xxxxx";

		assertThrows(JWTVerificationException.class, () ->
		{
			jwtService.validateAccessToken(tokenWithInvalidSignature);
		});
	}

	@Test
	void validateAccessToken_WithNullToken_ShouldThrowJWTVerificationException()
	{
		assertThrows(JWTVerificationException.class, () ->
		{
			jwtService.validateAccessToken(null);
		});
	}

	@Test
	void validateRefreshToken_WithValidToken_ShouldReturnDecodedJWT()
	{
		DecodedJWT decodedJWT = jwtService.validateRefreshToken(validRefreshToken);

		assertNotNull(decodedJWT);
		assertEquals(String.valueOf(testUserId), decodedJWT.getSubject());
		assertEquals(testUsername,
				decodedJWT.getClaim("name")
						  .asString());
		assertEquals(testEmail,
				decodedJWT.getClaim("email")
						  .asString());
		assertEquals("blog-api", decodedJWT.getIssuer());
	}

	@Test
	void validateRefreshToken_WithCustomExpiration_DoesNotThrow()
	{
		Instant forwardTime = Instant.now()
									 .plus(50, ChronoUnit.DAYS);
		String token = jwtService.generateRefreshToken(testUserId, testUsername, testEmail, forwardTime);

		assertDoesNotThrow(() ->
		{
			jwtService.validateRefreshToken(token);
		});
	}

	@Test
	void validateRefreshToken_WithExpiredToken_ShouldThrowTokenExpiredException()
	{
		Instant pastTime = Instant.now()
								  .minus(1, ChronoUnit.DAYS);
		String expiredToken = jwtService.generateRefreshToken(testUserId, testUsername, testEmail, pastTime);

		assertThrows(TokenExpiredException.class, () ->
		{
			jwtService.validateRefreshToken(expiredToken);
		});
	}

	@Test
	void validateRefreshToken_WithInvalidSignature_ShouldThrowJWTVerificationException()
	{
		String tokenWithInvalidSignature = validRefreshToken.substring(0, validRefreshToken.length() - 5) + "xxxxx";

		assertThrows(JWTVerificationException.class, () ->
		{
			jwtService.validateRefreshToken(tokenWithInvalidSignature);
		});
	}

	@Test
	void validateRefreshToken_WithNullToken_ShouldThrowJWTVerificationException()
	{
		assertThrows(JWTVerificationException.class, () ->
		{
			jwtService.validateRefreshToken(null);
		});
	}
}