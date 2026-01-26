package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndComments;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.projection.PostInfoWithAuthorTagsComments;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long>
{
	@EntityGraph(attributePaths = {"author"})
	Optional<PostInfoWithAuthor> findWithAuthorById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "comments"})
	Optional<PostInfoWithAuthorAndComments> findWithCommentsById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "tags"})
	Optional<PostInfoWithAuthorAndTags> findWithTagsById(@Param("id") long id);

	@EntityGraph(attributePaths = {"author", "comments", "tags"})
	Optional<PostInfoWithAuthorTagsComments> findWithTagsAndCommentsById(@Param("id") long id);
}
