package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.enums.PostSortBy;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Post_;
import com.blog.api.apispring.specs.PostSpecs;
import com.blog.api.apispring.validation.NoXSS;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Collections;

@Data
public class GetPostsRequestImpl implements GetPostsRequest
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

	public boolean isUnpublished()
	{
		return unpublished;
	}

	public Pageable toPageable()
	{
		int page = this.getPage();
		int pageSize = this.getPageSize();
		PostSortBy sortBy = this.getSortBy();

		Sort sort;
		switch (sortBy)
		{
			case ID_ASC -> sort = Sort.by(Sort.Order.asc(Post_.id.getName()));
			case ID_DESC -> sort = Sort.by(Sort.Order.desc(Post_.id.getName()));
			case PUBLISHED_AT_ASC -> sort = Sort.by(Sort.Order.asc(Post_.publishedAt.getName()));
			case PUBLISHED_AT_DESC -> sort = Sort.by(Sort.Order.desc(Post_.publishedAt.getName()));
			default -> throw new UnsupportedOperationException("Unsupported sort by value");
		}
		return PageRequest.of(page, pageSize, sort);
	}

	public Specification<Post> toSpecifications()
	{
		String q = this.getQ();
		Collection<TagIdOrSlug> tags = this.getTags();
		boolean includeUnpublished = this.isUnpublished();

		return PostSpecs.withTags(tags)
						.and(PostSpecs.titleContains(q))
						.and(PostSpecs.onlyPublished(!includeUnpublished));
	}
}
