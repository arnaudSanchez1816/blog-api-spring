package com.blog.api.apispring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, CharSequence>
{

	/**
	 * Checks that the character sequence is either {@code null} or not {@link String#isBlank() blank}.
	 *
	 * @param charSequence               the character sequence to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the string is {@code null} or
	 * the call to {@link String#isBlank() charSequence.isBlank()} returns {@code false}, {@code true} otherwise
	 * @see String#isBlank()
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext)
	{
		if (charSequence == null)
		{
			return true;
		}

		return !charSequence.toString()
							.isBlank();
	}
}
