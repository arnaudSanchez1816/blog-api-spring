package com.blog.api.apispring.dto.tag;

import com.blog.api.apispring.validation.NoXSS;
import com.blog.api.apispring.validation.TagSlug;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateTagRequest(@NotBlank @NoXSS @Length(max = 64) String name,
							   @NotBlank @NoXSS @Length(max = 30) @TagSlug String slug)
{
}
