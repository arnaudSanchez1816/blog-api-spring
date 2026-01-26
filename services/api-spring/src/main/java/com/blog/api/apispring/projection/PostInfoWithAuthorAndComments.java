package com.blog.api.apispring.projection;

import com.blog.api.apispring.model.Comment;

import java.util.Set;

public interface PostInfoWithAuthorAndComments extends PostInfoWithAuthor
{
	Set<Comment> getComments();
}
