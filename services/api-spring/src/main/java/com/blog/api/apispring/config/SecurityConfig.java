package com.blog.api.apispring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "blog-api.security.enable", value = "true")
public class SecurityConfig {

	@Bean
	public SecurityFilterChain web(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.httpBasic(AbstractHttpConfigurer::disable);
		http.authorizeHttpRequests(authorize -> {
			authorize.requestMatchers(HttpMethod.GET, "/users")
					 .authenticated();
			authorize.anyRequest()
					 .permitAll();
		});

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	// === CORS filter bean ===
//	// TODO: This is a very permissive configuration, adjust it to your needs!
//	@Bean
//	public CorsFilter corsFilter() {
//
//		CorsConfiguration cfg = new CorsConfiguration();
//
//		cfg.setAllowCredentials(true);
//		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//		cfg.addAllowedOriginPattern("*"); // TODO: Change with your front-end origin or remove if used with a mobile app
//		cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
//		cfg.setExposedHeaders(List.of("Authorization")); // Per il token JWT
//		// Preflight cache
//		cfg.setMaxAge(3600L);
//
//		UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
//		src.registerCorsConfiguration("/**", cfg);
//		return new CorsFilter(src);
//	}
}
