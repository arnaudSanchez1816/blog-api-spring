package com.blog.api.apispring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "blog-api.security.enable", havingValue = "false")
public class DisableSecurityConfig
{
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
	{
		http.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests((authz) -> authz.anyRequest()
												   .permitAll())
			.headers((headers) -> headers.frameOptions(
					FrameOptionsConfig::sameOrigin)  // to avoid setting of X-Frame-Options=deny header
			);
		return http.build();
	}
}
