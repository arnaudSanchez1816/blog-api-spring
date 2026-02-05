package com.blog.api.apispring.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {OneOfNotNullValidator.class})
@Documented
public @interface OneOfNotNull
{

	String message() default "At least one of the provided fields must be not null.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String[] fields() default {};
}
