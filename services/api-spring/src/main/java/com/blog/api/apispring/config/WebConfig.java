package com.blog.api.apispring.config;

import com.blog.api.apispring.converter.PostIdConverter;
import com.blog.api.apispring.converter.StringToPostSortByConverter;
import com.blog.api.apispring.converter.StringToTagIdOrSlugConverter;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer
{

	private final PostRepository postRepository;

	@Autowired
	public WebConfig(PostRepository postRepository)
	{
		this.postRepository = postRepository;
	}

	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new StringToTagIdOrSlugConverter());
		registry.addConverter(new PostIdConverter(postRepository));
		registry.addConverter(new StringToPostSortByConverter());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
	{
		resolvers.add(new PostIdConverter(postRepository));
	}
}
