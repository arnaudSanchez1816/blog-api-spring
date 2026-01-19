package com.blog.api.apispring.controller;

import com.blog.api.apispring.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	@GetMapping("/me")
	public ResponseEntity<String> getCurrentUser() {
		return ResponseEntity.ok("hello");
	}
}
