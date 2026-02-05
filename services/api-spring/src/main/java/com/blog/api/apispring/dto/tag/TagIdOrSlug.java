package com.blog.api.apispring.dto.tag;

import com.blog.api.apispring.utils.TagUtils;

public class TagIdOrSlug
{
	private final Long id;
	private final String slug;

	private TagIdOrSlug(Long id, String slug)
	{
		this.id = id;
		this.slug = slug;
	}

	public static TagIdOrSlug fromString(String value)
	{
		if (value.matches("^\\d+$"))
		{
			return new TagIdOrSlug(Long.parseLong(value), null);
		} else if (value.matches(TagUtils.SLUG_REGEX))
		{
			return new TagIdOrSlug(null, value);
		}

		throw new IllegalArgumentException("Invalid ID or slug format.");
	}

	public boolean isId()
	{
		return id != null;
	}

	public boolean isSlug()
	{
		return slug != null;
	}

	public Long getId()
	{
		return id;
	}

	public String getSlug()
	{
		return slug;
	}
}
