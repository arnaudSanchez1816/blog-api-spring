package com.blog.api.apispring.dto.posts;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreatePostCommentRequest(@NotBlank
									   String body,
									   @Length(max = 255)
									   @NotBlank
									   String username)
{
}
