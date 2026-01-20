package com.blog.api.apispring.config;

import com.blog.api.apispring.model.User;
import com.blog.api.apispring.service.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile(value = {"dev", "!test"})
public class DatabaseInitializer implements ApplicationRunner
{

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	public DatabaseInitializer(UserService userService, PasswordEncoder passwordEncoder)
	{
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(@NonNull ApplicationArguments args)
	{
		System.out.println("Initializing database...");
		userService.saveUser(new User("admin@blog.com", "admin", passwordEncoder.encode("AdminPassword10")));
		System.out.println("Initializing database done!");
	}
}
