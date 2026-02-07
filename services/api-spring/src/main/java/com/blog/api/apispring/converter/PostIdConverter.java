package com.blog.api.apispring.converter;

import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.repository.PostRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

public class PostIdConverter implements Converter<String, Post>
{
	private final PostRepository postRepository;

	public PostIdConverter(PostRepository postRepository)
	{
		this.postRepository = postRepository;
	}

	@Override
	public Post convert(String id)
	{
		try
		{
			return postRepository.findById(Long.parseLong(id))
								 .orElse(null);
		} catch (NumberFormatException e)
		{
			return null;
		}
	}
}
