package com.blog.api.apispring.utils;

import org.jspecify.annotations.NonNull;

public class TagUtils
{
	public static final String SLUG_REGEX = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

	public static boolean isSlug(@NonNull String slug)
	{
		return slug.length() <= 30 && slug.matches(SLUG_REGEX);
	}
}
