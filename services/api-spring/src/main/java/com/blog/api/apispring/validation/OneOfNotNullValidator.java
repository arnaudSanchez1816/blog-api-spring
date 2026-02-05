package com.blog.api.apispring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class OneOfNotNullValidator implements ConstraintValidator<OneOfNotNull, Object>
{
	private String[] fields;

	@Override
	public void initialize(OneOfNotNull constraintAnnotation)
	{
		fields = constraintAnnotation.fields();
		if (fields == null || fields.length == 0)
		{
			throw new IllegalArgumentException("At lease one field name must be provided.");
		}
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context)
	{
		if (value == null)
		{
			return true;
		}

		try
		{
			int notNullCounter = 0;
			for (String fieldName : fields)
			{
				Field field = value.getClass()
								   .getDeclaredField(fieldName);
				field.setAccessible(true);
				if (field.get(value) != null)
				{
					notNullCounter += 1;
				}
			}

			return notNullCounter >= 1;
		} catch (NoSuchFieldException e)
		{
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
