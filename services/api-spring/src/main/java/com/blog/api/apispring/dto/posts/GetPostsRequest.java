package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.enums.PostSortBy;
import com.blog.api.apispring.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public interface GetPostsRequest
{
	String getQ();

	void setQ(String q);

	int getPage();

	void setPage(int page);

	int getPageSize();

	void setPageSize(int pageSize);

	PostSortBy getSortBy();

	void setSortBy(PostSortBy sortBy);

	Collection<TagIdOrSlug> getTags();

	void setTags(Collection<TagIdOrSlug> tags);

	boolean isUnpublished();

	void setUnpublished(boolean isUnpublished);

	Pageable toPageable();

	Specification<Post> toSpecifications();
}
