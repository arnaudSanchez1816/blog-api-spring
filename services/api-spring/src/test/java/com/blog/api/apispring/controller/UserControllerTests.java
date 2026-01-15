package com.blog.api.apispring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getUserWhenUnauthenticatedThenUnauthorized() throws Exception {
		mockMvc.perform(get("/users"))
			   .andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	void getUser() throws Exception {
		mockMvc.perform(get("/users"))
			   .andExpect(status().isOk());
	}
}
