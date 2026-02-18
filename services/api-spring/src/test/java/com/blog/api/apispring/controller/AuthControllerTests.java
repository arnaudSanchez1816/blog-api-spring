package com.blog.api.apispring.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.blog.api.apispring.dto.LoginRequest;
import com.blog.api.apispring.dto.LoginResponse;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.UserRepository;
import com.blog.api.apispring.security.filter.RefreshJwtAuthenticationFilter;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.service.JwtService;
import com.blog.api.apispring.utils.JsonUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;

import static com.blog.api.apispring.security.filter.RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(ClearDatabaseExtension.class)
public class AuthControllerTests
{
	@Autowired
	private MockMvcTester mockMvc;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void login_ReturnsUserDetails_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);

		LoginRequest body = new LoginRequest("user@email.com", user.getPassword());
		MvcTestResult response = mockMvc.post()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(body))
										.uri("/auth/login")
										.exchange();
		User finalUser = user;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.doesNotHavePath("$.user.password");
							})
							.convertTo(LoginResponse.class)
							.satisfies(dto ->
							{
								assertThat(dto.user()).isNotNull();
								assertThat(dto.user()
											  .id()).isEqualTo(finalUser.getId());
								assertThat(dto.user()
											  .email()).isEqualTo(finalUser.getEmail());
								assertThat(dto.user()
											  .name()).isEqualTo(finalUser.getName());
							});
	}

	@Test
	void login_ReturnsToken_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);
		when(jwtService.generateAccessToken(user.getId(), user.getName(), user.getEmail())).thenReturn("Access Token");

		LoginRequest body = new LoginRequest("user@email.com", user.getPassword());
		MvcTestResult response = mockMvc.post()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(body))
										.uri("/auth/login")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(LoginResponse.class)
							.satisfies(dto ->
							{
								assertThat(dto.accessToken()).isNotBlank();
								assertThat(dto.accessToken()).isEqualTo("Access Token");
							});
	}

	@Test
	void login_RefreshTokenCookieIsSet_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);

		LoginRequest body = new LoginRequest("user@email.com", user.getPassword());
		MvcTestResult response = mockMvc.post()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(body))
										.uri("/auth/login")
										.exchange();
		assertThat(response).hasStatusOk()
							.cookies()
							.hasCookieSatisfying(REFRESH_TOKEN_COOKIE, cookie ->
							{
								assertThat(cookie.isHttpOnly()).isTrue();
								assertThat(cookie.getSecure()).isTrue();
								assertThat(cookie.getAttribute("SameSite")).isEqualTo("Strict");
								assertThat(cookie.getPath()).endsWith("/auth/token");
								assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(30)
																				 .getSeconds());
							});
	}

	@Test
	void login_Is401_WhenUserPasswordIsInvalid()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		LoginRequest body = new LoginRequest("user@email.com", "invalid password");
		MvcTestResult response = mockMvc.post()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(body))
										.uri("/auth/login")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void getToken_ReturnsToken_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);
		when(jwtService.generateAccessToken(user.getId(), user.getName(), user.getEmail())).thenReturn("Access Token");

		MvcTestResult response = mockMvc.get()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/auth/token")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(LoginResponse.class)
							.satisfies(dto ->
							{
								assertThat(dto.accessToken()).isNotBlank();
								assertThat(dto.accessToken()).isEqualTo("Access Token");
							});
	}

	@Test
	void getToken_ReturnsToken_WhenUserIsAuthenticatedWithRefreshToken()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);

		when(jwtService.validateAccessToken(Mockito.anyString())).thenThrow(JWTVerificationException.class);
		DecodedJWT decodedJWT = Mockito.mock(DecodedJWT.class);
		when(decodedJWT.getSubject()).thenReturn(String.valueOf(user.getId()));
		when(jwtService.validateRefreshToken(Mockito.anyString())).thenReturn(decodedJWT);
		when(jwtService.generateAccessToken(user.getId(), user.getName(), user.getEmail())).thenReturn("Access Token");
		MvcTestResult response = mockMvc.get()
										.header("Authorization", "Bearer ExpiredAccessToken")
										.cookie(new Cookie(REFRESH_TOKEN_COOKIE, "Refresh token"))
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/auth/token")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(LoginResponse.class)
							.satisfies(dto ->
							{
								assertThat(dto.accessToken()).isNotBlank();
								assertThat(dto.accessToken()).isEqualTo("Access Token");
							});
	}

	@Test
	void getToken_Is401_WhenUserIsUnauthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/auth/token")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}
}
