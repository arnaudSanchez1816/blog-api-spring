package com.blog.api.apispring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests
{
	@Autowired
	private MockMvcTester mockMvc;

	@Test
	@WithMockUser
	void getUser_IsOk_WhenUserIsAuthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatusOk();
	}

	@Test
	void getUser_Is401_WhenNoUserAuthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}
}
