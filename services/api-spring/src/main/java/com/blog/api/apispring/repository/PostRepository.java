package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>, PostRepositoryExtension
{

	@EntityGraph(attributePaths = {"author"})
	Optional<PostInfoWithAuthor> findWithAuthorById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "comments"})
	Optional<PostInfoWithAuthorAndComments> findWithCommentsById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "tags"})
	Optional<PostInfoWithAuthorAndTags> findWithTagsById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "tags", "comments"})
	Optional<PostInfoWithAuthorTagsComments> findWithTagsAndCommentsById(@Param("id") long id);

	@Query(value = """
				select p
				from Post p
			""", countQuery = """
				select count(p.id)
				from Post p
			""")
	@EntityGraph(attributePaths = {"author", "tags"})
	Page<PostInfoWithAuthorAndTags> findAllWithTags(Pageable pageable);

	@Query(value = """
				select count(c)
				from Post p
				left join Comment c on c.post = p
				where p.id = :id
			""")
	long countCommentsById(@Param("id") Long id);
}
