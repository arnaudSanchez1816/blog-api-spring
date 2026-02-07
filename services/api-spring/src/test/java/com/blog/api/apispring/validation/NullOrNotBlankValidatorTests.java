package com.blog.api.apispring.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullOrNotBlankValidatorTests
{
	@Test
	void isValid_ReturnsTrue_WhenValueIsNull()
	{
		NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

		boolean result = validator.isValid(null, null);

		assertTrue(result);
	}

	@Test
	void isValid_ReturnsTrue_WhenValueIsNotBlank()
	{
		NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

		boolean result = validator.isValid("test", null);

		assertTrue(result);
	}

	@Test
	void isValid_ReturnsFalse_WhenValueIsBlank()
	{
		NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

		boolean result = validator.isValid("", null);

		assertFalse(result);
	}

	@Test
	void isValid_ReturnsFalse_WhenValueIsEmpty()
	{
		NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

		boolean result = validator.isValid("", null);

		assertFalse(result);
	}

	@Test
	void isValid_ReturnsFalse_WhenValueIsWhitespaceOnly()
	{
		NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

		boolean result = validator.isValid("   ", null);

		assertFalse(result);
	}
}