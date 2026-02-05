package com.blog.api.apispring.converter;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import org.springframework.core.convert.converter.Converter;

public class StringToTagIdOrSlugConverter implements Converter<String, TagIdOrSlug>
{
	@Override
	public TagIdOrSlug convert(String source)
	{
		return TagIdOrSlug.fromString(source);
	}
}
