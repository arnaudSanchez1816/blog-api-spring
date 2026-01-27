package com.blog.api.apispring.projection;

import com.blog.api.apispring.model.Comment;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public interface PostInfoWithAuthorAndComments extends PostInfoWithAuthor
{
	@Value("#{target?.comments ?: new java.util.HashSet()}")
	Set<Comment> getComments();
}
