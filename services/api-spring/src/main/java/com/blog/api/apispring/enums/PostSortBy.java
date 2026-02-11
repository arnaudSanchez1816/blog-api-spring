package com.blog.api.apispring.enums;

public enum PostSortBy
{
	ID_ASC("id"),
	ID_DESC("-id"),
	PUBLISHED_AT_ASC("publishedAt"),
	PUBLISHED_AT_DESC("-publishedAt");

	private final String value;

	PostSortBy(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return value;
	}
}
