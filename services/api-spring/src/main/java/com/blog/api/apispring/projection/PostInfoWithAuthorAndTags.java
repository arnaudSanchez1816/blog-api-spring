package com.blog.api.apispring.projection;

import com.blog.api.apispring.model.Tag;

import java.util.Set;

public interface PostInfoWithAuthorAndTags extends PostInfoWithAuthor
{
	Set<Tag> getTags();
}
