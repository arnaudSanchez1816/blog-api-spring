package com.blog.api.apispring.extensions;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.test.context.junit.jupiter.SpringExtension;

// https://maciejwalkowiak.com/blog/spring-boot-flyway-clear-database-integration-tests/
public class ClearDatabaseExtension implements BeforeEachCallback
{
	@Override
	public void beforeEach(@NonNull ExtensionContext extensionContext) throws Exception
	{
		Flyway flyway = SpringExtension.getApplicationContext(extensionContext)
									   .getBean(Flyway.class);
		flyway.clean();
		flyway.migrate();
	}
}