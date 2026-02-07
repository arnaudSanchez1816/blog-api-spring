package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.validation.NoXSS;
import com.blog.api.apispring.validation.NullOrNotBlank;
import com.blog.api.apispring.validation.OneOfNotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@OneOfNotNull(fields = {"title", "body", "tags"})
public record UpdatePostRequest(@NoXSS
								@Size(max = 255, message = "Title must be between 1 and 255 characters long.")
								@NullOrNotBlank(message = "Title must have at least 1 non whitespace character.")
								String title,
								@NoXSS
								String body,
								Set<TagIdOrSlug> tags)
{

//	// Instead of class level validator
//	@AssertTrue(message = "At least one field must be provided.")
//	private boolean isValid()
//	{
//		return title != null || body != null || tags != null;
//	}
}
