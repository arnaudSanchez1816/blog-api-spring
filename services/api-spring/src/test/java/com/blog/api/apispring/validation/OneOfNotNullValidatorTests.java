package com.blog.api.apispring.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link OneOfNotNullValidator}.
 * <p>
 * This class tests the {@code isValid} method of the {@link OneOfNotNullValidator},
 * which validates that at least one of the specified fields in an object is not null.
 * </p>
 */
class OneOfNotNullValidatorTests
{
	@Test
	void isValid_ReturnsTrue_WhenValueIsNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

		boolean result = validator.isValid(null, context);

		assertThat(result).isTrue();
	}

	@Test
	void isValid_ReturnsTrue_WhenOneFieldIsNotNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1", "field2"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject("value", null);

		boolean result = validator.isValid(testObject, context);

		assertThat(result).isTrue();
	}

	@Test
	void isValid_ReturnsTrue_WhenMultipleFieldsAreNotNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1", "field2"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject("value1", "value2");

		boolean result = validator.isValid(testObject, context);

		assertThat(result).isTrue();
	}

	@Test
	void isValid_ReturnsFalse_WhenAllFieldsAreNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1", "field2"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject(null, null);

		boolean result = validator.isValid(testObject, context);

		assertThat(result).isFalse();
	}

	@Test
	void isValid_ThrowsIllegalArgumentException_WhenFieldDoesNotExist()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"nonExistentField"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject("value", null);

		assertThatThrownBy(() -> validator.isValid(testObject, context)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void isValid_ReturnsTrue_WhenSingleFieldIsNotNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject("value", null);

		boolean result = validator.isValid(testObject, context);

		assertThat(result).isTrue();
	}

	@Test
	void isValid_ReturnsFalse_WhenSingleFieldIsNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{"field1"});
		validator.initialize(annotation);
		ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
		TestObject testObject = new TestObject(null, null);

		boolean result = validator.isValid(testObject, context);

		assertThat(result).isFalse();
	}

	@Test
	void initialize_ThrowsIllegalArgumentException_WhenFieldsArrayIsEmpty()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(new String[]{});

		assertThatThrownBy(() -> validator.initialize(annotation)).isInstanceOf(IllegalArgumentException.class)
																  .hasMessageContaining(
																		  "At lease one field name must be provided");
	}

	@Test
	void initialize_ThrowsIllegalArgumentException_WhenFieldsArrayIsNull()
	{
		OneOfNotNullValidator validator = new OneOfNotNullValidator();
		OneOfNotNull annotation = Mockito.mock(OneOfNotNull.class);
		Mockito.when(annotation.fields())
			   .thenReturn(null);

		assertThatThrownBy(() -> validator.initialize(annotation)).isInstanceOf(IllegalArgumentException.class)
																  .hasMessageContaining(
																		  "At lease one field name must be provided");
	}

	private static class TestObject
	{
		private final String field1;
		private final String field2;

		public TestObject(String field1, String field2)
		{
			this.field1 = field1;
			this.field2 = field2;
		}
	}
}
