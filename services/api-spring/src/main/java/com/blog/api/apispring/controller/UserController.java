package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.UserDetailsResponse;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.security.userdetails.BlogUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController
{

	@GetMapping("/me")
	public ResponseEntity<UserDetailsResponse> getCurrentUser(@AuthenticationPrincipal BlogUserDetails userDetails)
	{
		return ResponseEntity.ok(new UserDetailsResponse(userDetails));
	}
}
