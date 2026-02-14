package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.enums.PostSortBy;
import com.blog.api.apispring.validation.NoXSS;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Data
public class GetPostsRequest
{
	@NoXSS
	private String q;
	private int page = DEFAULT_PAGE;
	private int pageSize = DEFAULT_PAGE_SIZE;
	@NotNull
	private PostSortBy sortBy = PostSortBy.PUBLISHED_AT_DESC;
	private Collection<TagIdOrSlug> tags = Collections.emptySet();
	private boolean unpublished = false;

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_PAGE_SIZE = 20;
	public static final int MAX_PAGE_SIZE = 50;

	public void setPage(int page)
	{
		this.page = Math.max(DEFAULT_PAGE, page);
	}

	public void setPageSize(int pageSize)
	{
		if (pageSize <= 0)
		{
			this.pageSize = DEFAULT_PAGE_SIZE;
			return;
		}

		this.pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
	}
}
