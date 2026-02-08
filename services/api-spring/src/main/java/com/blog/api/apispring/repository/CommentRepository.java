package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.projection.CommentInfo;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface CommentRepository extends CrudRepository<Comment, Long>
{
	@Query(value = """
			select c.id as id, c.username as username, c.body as body, c.createdAt as createdAt, c.post.id as postId
			from Comment c
			where c.id = :id
			""")
	Optional<CommentInfo> findCommentInfoById(@Param("id") long id);

	@Query(value = """
				select c.id as id, c.username as username, c.body as body, c.createdAt as createdAt, c.post.id as postId
				from Comment c
				where c.post.id = :postId
			""", countQuery = """
				select count(c.id)
				from Comment c
			""")
	Set<CommentInfo> findAllInfoByPostId(@Param("postId") long postId);
}