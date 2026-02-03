package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.metadata.Metadata;

import java.util.List;

public class GetPostsResponse
{
	private List<PostDto> results;
	private Metadata metadata;

	public GetPostsResponse(List<PostDto> results, Metadata metadata)
	{
		this.results = results;
		this.metadata = metadata;
	}

	public List<PostDto> getResults()
	{
		return results;
	}

	public void setResults(List<PostDto> results)
	{
		this.results = results;
	}

	public Metadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(Metadata metadata)
	{
		this.metadata = metadata;
	}
}
