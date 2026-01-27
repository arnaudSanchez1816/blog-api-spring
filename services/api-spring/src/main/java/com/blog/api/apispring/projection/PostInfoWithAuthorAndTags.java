package com.blog.api.apispring.projection;

import com.blog.api.apispring.model.Tag;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public interface PostInfoWithAuthorAndTags extends PostInfoWithAuthor
{
	@Value("#{target?.tags ?: new java.util.HashSet()}")
	Set<Tag> getTags();
}
