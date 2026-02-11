package com.blog.api.apispring.converter;

import com.blog.api.apispring.enums.PostSortBy;
import org.springframework.core.convert.converter.Converter;

public class StringToPostSortByConverter implements Converter<String, PostSortBy>
{
	@Override
	public PostSortBy convert(String source)
	{
		if (source == null)
		{
			return null;
		}

		PostSortBy[] values = PostSortBy.values();
		String sourceUpper = source.toUpperCase();
		for (PostSortBy sortBy : values)
		{
			String valueUpper = sortBy.getValue()
									  .toUpperCase();
			if (sourceUpper.equals(valueUpper))
			{
				return sortBy;
			}
		}
		return null;
	}
}
