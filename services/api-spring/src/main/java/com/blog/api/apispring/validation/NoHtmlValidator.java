package com.blog.api.apispring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

public class NoHtmlValidator implements ConstraintValidator<NoXSS, String>
{
	@Override
	public boolean isValid(String value, ConstraintValidatorContext ctx)
	{
		if (value == null) return true;
		Cleaner cleaner = new Cleaner(Safelist.none());
		String text = cleaner.clean(Jsoup.parse(value))
							 .text();
		return text.equals(value);
	}
}