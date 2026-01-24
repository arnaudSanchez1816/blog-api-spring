package com.blog.api.apispring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TagSlugImpl.class)
public @interface TagSlug
{
	String message() default "The value of path variable must be String";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
