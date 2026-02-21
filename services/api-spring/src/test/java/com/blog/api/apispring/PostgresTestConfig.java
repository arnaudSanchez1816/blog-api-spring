package com.blog.api.apispring;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("postgres")
@TestConfiguration
public class PostgresTestConfig
{
	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer()
	{
		return new PostgreSQLContainer<>("postgres:16.11-bookworm");
	}
}
