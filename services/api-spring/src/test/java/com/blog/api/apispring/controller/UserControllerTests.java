package com.blog.api.apispring.controller;

import com.blog.api.apispring.ApplicationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.json.JsonContent;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
//@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
@Import(ApplicationTestConfig.class)
public class UserControllerTests
{
	@Autowired
	private MockMvcTester mockMvc;

	@Test
	@WithUserDetails("admin")
	void getUser_IsOk_WhenUserIsAuthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.returns(1, fromPath("$.id"))
							.returns("admin", fromPath("$.name"))
							.returns("admin@blog.com", fromPath("$.email"));

	}

	@Test
	void getUser_Is401_WhenNoUserAuthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private static Function<JsonContent, Object> fromPath(String path)
	{
		return jsonContent -> assertThat(jsonContent).extractingPath(path)
													 .actual();
	}
}
