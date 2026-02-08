package com.blog.api.apispring.dto.comment;

import com.blog.api.apispring.validation.NullOrNotBlank;
import com.blog.api.apispring.validation.OneOfNotNull;
import org.hibernate.validator.constraints.Length;

@OneOfNotNull(fields = {"username", "body"})
public record UpdateCommentRequest(@Length(max = 255)
								   @NullOrNotBlank
								   String username,
								   String body)
{
}