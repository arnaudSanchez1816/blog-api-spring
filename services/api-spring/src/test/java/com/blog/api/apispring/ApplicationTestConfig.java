package com.blog.api.apispring;

import com.blog.api.apispring.model.User;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.security.userdetails.service.BlogInMemoryUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
public class ApplicationTestConfig
{
	@Bean
	UserDetailsService userDetailsService()
	{
		User admin = new User("admin@blog.com", "admin", "password");
		admin.setId(1L);
		User user = new User("user@blog.com", "user", "password");
		user.setId(2L);

		return new BlogInMemoryUserDetailsService(new SecurityUser(admin), new SecurityUser(user));
	}
}
