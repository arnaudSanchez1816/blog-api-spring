package com.blog.api.apispring.converter;

import com.blog.api.apispring.annotation.WithPostComments;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.repository.PostRepository;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public class PostIdConverter implements Converter<String, Post>, HandlerMethodArgumentResolver
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

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return parameter.getParameterType()
						.equals(Post.class) && parameter.hasParameterAnnotation(WithPostComments.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
	{
		@SuppressWarnings("unchecked") Map<String, String> pathVariables = (Map<String, String>) webRequest.getAttribute(
				HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);

		if (pathVariables == null)
		{
			return null;
		}

		String paramName = parameter.getParameterName();
		String id = pathVariables.get(paramName);

		if (id == null)
		{
			return null;
		}

		try
		{
			return postRepository.findInfoWithCommentsById(Long.parseLong(id))
								 .orElse(null);
		} catch (NumberFormatException e)
		{
			return null;
		}
	}
}
