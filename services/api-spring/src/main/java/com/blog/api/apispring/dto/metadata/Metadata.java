package com.blog.api.apispring.dto.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata
{
	private Integer count;

	public Metadata count(int count)
	{
		this.count = count;
		return this;
	}

	public Integer getCount()
	{
		return count;
	}
}
