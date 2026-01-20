package com.blog.api.apispring.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService
{

	@Value("${blog-api.security.jwt.access-secret}")
	private String jwtAccessSecret;
	@Value("${blog-api.security.jwt.refresh-secret}")
	private String jwtRefreshSecret;

	public DecodedJWT validateAccessToken(String token) throws JWTVerificationException
	{
		return validateToken(jwtAccessSecret, token);
	}

	public DecodedJWT validateRefreshToken(String token) throws JWTVerificationException
	{
		return validateToken(jwtRefreshSecret, token);
	}

	private DecodedJWT validateToken(String jwtSecret, String token) throws JWTVerificationException
	{
		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
								  .withIssuer("blog-api")
								  .build();
		return verifier.verify(token);
	}

	public String generateAccessToken(Long userId, String username, String email)
	{
		return generateJwtToken(jwtAccessSecret, username, email, userId, Instant.now()
																				 .plus(1, ChronoUnit.DAYS));
	}

	public String generateAccessToken(Long userId, String username, String email, Instant expiresAt)
	{
		return generateJwtToken(jwtAccessSecret, username, email, userId, expiresAt);
	}

	public String generateRefreshToken(Long userId, String username, String email)
	{
		return generateJwtToken(jwtAccessSecret, username, email, userId, Instant.now()
																				 .plus(30, ChronoUnit.DAYS));
	}

	public String generateRefreshToken(Long userId, String username, String email, Instant expiresAt)
	{
		return generateJwtToken(jwtRefreshSecret, username, email, userId, expiresAt);
	}


	private String generateJwtToken(String jwtSecret, String username, String email, Long userId, Instant expiresAt)
	{
		try
		{
			Instant now = Instant.now();
			return JWT.create()
					  .withIssuer("blog-api")
					  .withSubject(String.valueOf(userId))
					  .withClaim("name", username)
					  .withClaim("email", email)
					  .withIssuedAt(now)
					  .withExpiresAt(expiresAt)
					  .sign(Algorithm.HMAC256(jwtSecret));
		} catch (JWTCreationException exception)
		{
			throw new RuntimeException(exception);
		}
	}
}
