package com.blog.api.apispring.projection;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Tag;

import java.util.Set;

public interface PostInfoWithAuthorTagsComments extends PostInfoWithAuthor
{
	Set<Tag> getTags();

	Set<Comment> getComments();
}
