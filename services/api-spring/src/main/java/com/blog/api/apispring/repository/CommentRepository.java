package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.CommentInfo;
import jakarta.persistence.SqlResultSetMapping;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends CrudRepository<Comment, Long>
{
	@Query(value = """
			select c.id as id, c.username as username, c.body as body, c.createdAt as createdAt, c.post.id as postId
			from Comment c
			where c.id = :id
			""")
	Optional<CommentInfo> findCommentInfoById(@Param("id") long id);
}