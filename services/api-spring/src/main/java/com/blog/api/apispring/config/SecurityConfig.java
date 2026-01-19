package com.blog.api.apispring.config;

import com.blog.api.apispring.exception.handler.RestAccessDeniedHandler;
import com.blog.api.apispring.exception.handler.RestAuthenticationEntryPoint;
import com.blog.api.apispring.security.filter.AccessJwtAuthenticationFilter;
import com.blog.api.apispring.security.filter.RefreshJwtAuthenticationFilter;
import com.blog.api.apispring.security.provider.JwtAuthenticationProvider;
import com.blog.api.apispring.security.userdetails.service.BlogUserDetailsService;
import com.blog.api.apispring.service.JwtService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "blog-api.security.enable", havingValue = "true")
public class SecurityConfig
{
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final RestAccessDeniedHandler restAccessDeniedHandler;
	private final JwtService jwtService;

	public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint,
						  RestAccessDeniedHandler restAccessDeniedHandler, JwtService jwtService)
	{
		this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
		this.restAccessDeniedHandler = restAccessDeniedHandler;
		this.jwtService = jwtService;
	}

	@Bean
	public SecurityFilterChain web(HttpSecurity http, BlogUserDetailsService userDetailsService) throws Exception
	{
		http.userDetailsService(userDetailsService);
		http.csrf(AbstractHttpConfigurer::disable);
		http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.httpBasic(AbstractHttpConfigurer::disable);
		http.formLogin(AbstractHttpConfigurer::disable);
		http.authorizeHttpRequests(authorize ->
								   {
									   authorize.requestMatchers(HttpMethod.POST, "/auth/login")
												.permitAll();
									   authorize.requestMatchers(HttpMethod.GET, "/auth/token")
												.authenticated();
									   authorize.requestMatchers(HttpMethod.GET, "/users/me")
												.authenticated();
									   authorize.anyRequest()
												.permitAll();
								   });

		// === Error handling JSON ===
		http.exceptionHandling(ex ->
							   {
								   ex.authenticationEntryPoint(restAuthenticationEntryPoint)
									 .accessDeniedHandler(restAccessDeniedHandler);
							   });

		// === JWT Filters ===
		AuthenticationManager authenticationManager = authenticationManager(userDetailsService);
		http.addFilterBefore(new RefreshJwtAuthenticationFilter(jwtService, authenticationManager),
							 UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(new AccessJwtAuthenticationFilter(jwtService, authenticationManager),
							 RefreshJwtAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(BlogUserDetailsService userDetailsService)
	{
		DaoAuthenticationProvider usernamePasswordAuthProvider = new DaoAuthenticationProvider(userDetailsService);
		JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(userDetailsService);

		return new ProviderManager(usernamePasswordAuthProvider, jwtAuthenticationProvider);
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
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
