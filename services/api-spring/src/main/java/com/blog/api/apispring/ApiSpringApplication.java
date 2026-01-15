package com.blog.api.apispring;

import com.blog.api.apispring.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class ApiSpringApplication {

	static void main(String[] args) {
		SpringApplication.run(ApiSpringApplication.class, args);
	}
}
