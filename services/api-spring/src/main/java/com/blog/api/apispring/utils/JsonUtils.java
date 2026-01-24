package com.blog.api.apispring.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils
{
	// Utility class for converting an object into JSON string
	public static String asJsonString(final Object obj)
	{
		try
		{
			final ObjectMapper mapper = new ObjectMapper();
			mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
