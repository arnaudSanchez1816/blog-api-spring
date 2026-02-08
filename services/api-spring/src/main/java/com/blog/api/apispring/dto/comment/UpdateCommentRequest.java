package com.blog.api.apispring.dto.comment;

import com.blog.api.apispring.validation.NullOrNotBlank;
import com.blog.api.apispring.validation.OneOfNotNull;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for {@link com.blog.api.apispring.model.Comment}
 */
@OneOfNotNull
public record UpdateCommentRequest(@Length(max = 255)
								   @NullOrNotBlank
								   String username,
								   String body)
{
}