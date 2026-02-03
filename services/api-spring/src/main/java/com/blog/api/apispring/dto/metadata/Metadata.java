package com.blog.api.apispring.dto.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata
{
	private Long count;
	private Integer page;
	private Integer pageSize;

	public Metadata count(long count)
	{
		this.count = count;
		return this;
	}

	public Metadata page(int page)
	{
		this.page = page;
		return this;
	}

	public Metadata pageSize(int pageSize)
	{
		this.pageSize = pageSize;
		return this;
	}

	public Long getCount()
	{
		return count;
	}

	public Integer getPage()
	{
		return page;
	}

	public Integer getPageSize()
	{
		return pageSize;
	}
}
