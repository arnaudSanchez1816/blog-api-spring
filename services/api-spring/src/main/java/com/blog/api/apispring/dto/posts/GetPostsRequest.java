package com.blog.api.apispring.dto.posts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GetPostsRequest(@NotNull @Min(0) int page, @Min(1) int pageSize)
{
}
