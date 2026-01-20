package com.blog.api.apispring.dto.tag;

import com.blog.api.apispring.validation.NoXSS;
import com.blog.api.apispring.validation.TagSlug;
import jakarta.validation.constraints.Max;

public record UpdateTagRequest(@NoXSS @Max(64) String name, @NoXSS @Max(30) @TagSlug String slug)
{
}
