package com.blog.api.apispring.validation;

import com.blog.api.apispring.utils.TagUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TagSlugImpl implements ConstraintValidator<TagSlug, String>
{
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context)
	{
		if (value == null || value.isBlank())
		{
			return false;
		}
		return value.matches(TagUtils.SLUG_REGEX);
	}
}
