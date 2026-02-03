package com.blog.api.apispring.dto.posts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
		@NotBlank(message = "Post title cannot be blank.") @Size(max = 255, message = "Post title can be 255 characters maximum.") String title)
{
}
