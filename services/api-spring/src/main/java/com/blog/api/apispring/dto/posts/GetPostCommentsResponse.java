package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.metadata.Metadata;
import com.blog.api.apispring.projection.CommentInfo;

import java.util.Collection;

public record GetPostCommentsResponse(Metadata metadata,
									  Collection<CommentInfo> results)
{
	public static GetPostCommentsResponse fromCommentsInfo(Collection<CommentInfo> comments)
	{
		Metadata metadata = new Metadata();
		metadata.count(comments.size());
		return new GetPostCommentsResponse(metadata, comments);
	}
}
