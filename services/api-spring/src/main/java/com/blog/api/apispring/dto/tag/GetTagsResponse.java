package com.blog.api.apispring.dto.tag;

import com.blog.api.apispring.dto.metadata.Metadata;
import com.blog.api.apispring.model.Tag;

import java.util.List;

public record GetTagsResponse(List<Tag> results, Metadata metadata)
{
	public GetTagsResponse(List<Tag> tags)
	{
		Metadata m = new Metadata();
		m.count(tags.size());
		this(tags, m);
	}
}
