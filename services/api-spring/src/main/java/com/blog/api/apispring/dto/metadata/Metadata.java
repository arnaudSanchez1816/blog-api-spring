package com.blog.api.apispring.dto.metadata;

import com.blog.api.apispring.enums.PostSortBy;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata
{
	private Long count;
	private Integer page;
	private Integer pageSize;
	private String sortBy;

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

	public Metadata sortBy(PostSortBy sortBy)
	{
		this.sortBy = sortBy.getValue();
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

	public String getSortBy()
	{
		return sortBy;
	}
}
