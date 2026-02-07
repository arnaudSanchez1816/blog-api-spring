package com.blog.api.apispring.controller;

import com.blog.api.apispring.ApplicationTestConfig;
import com.blog.api.apispring.dto.LoginRequest;
import com.blog.api.apispring.dto.LoginResponse;
import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.UserRepository;
import com.blog.api.apispring.security.filter.RefreshJwtAuthenticationFilter;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.service.JwtService;
import com.blog.api.apispring.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Test
	void login_ReturnsToken_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		Mockito.when(passwordEncoder.matches(user.getPassword(), user.getPassword()))
			   .thenReturn(true);

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
							});
	}

	@Test
	void login_RefreshTokenCookieIsSet_WhenUserIsAuthenticated()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		Mockito.when(passwordEncoder.matches(user.getPassword(), user.getPassword()))
			   .thenReturn(true);

		LoginRequest body = new LoginRequest("user@email.com", user.getPassword());
		MvcTestResult response = mockMvc.post()
										.with(user(new SecurityUser(user)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(body))
										.uri("/auth/login")
										.exchange();
		assertThat(response).hasStatusOk()
							.cookies()
							.containsCookie(RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE)
							.isHttpOnly(RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE, true)
							.isSecure(RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE, true)
							.hasMaxAge(RefreshJwtAuthenticationFilter.REFRESH_TOKEN_COOKIE, Duration.ofDays(30));
	}

	@Test
	void login_Is401_WhenUserPasswordIsInvalid()
	{
		User user = new User("user@email.com", "user", "password123");
		user = userRepository.save(user);

		Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString()))
			   .thenReturn(false);

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

		Mockito.when(passwordEncoder.matches(user.getPassword(), user.getPassword()))
			   .thenReturn(true);

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
