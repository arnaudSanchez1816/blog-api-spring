package com.blog.api.apispring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TagSlugImpl implements ConstraintValidator<TagSlug, String>
{
	private static final String SLUG_REGEX = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		return value.matches(SLUG_REGEX);
	}
}
