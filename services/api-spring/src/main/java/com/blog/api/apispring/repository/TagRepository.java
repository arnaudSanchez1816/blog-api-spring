package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Tag;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends CrudRepository<Tag, Long>
{

	@Override
	@NullMarked
	List<Tag> findAll();

	Optional<Tag> deleteTagById(Long id);

	Optional<Tag> deleteTagBySlug(String slug);

	Optional<Tag> findBySlug(String slug);

	@Transactional
	@Modifying
	@Query("UPDATE Tag t set t.slug= :#{#tag.slug}, t.name= :#{#tag.name} WHERE t.slug= :slug")
	void updateTag(@Param("slug") String slug, @Param("tag") Tag tag);

	@Transactional
	@Modifying
	@Query("UPDATE Tag t set t.slug= :#{#tag.slug}, t.name= :#{#tag.name} WHERE t.id= :id")
	void updateTag(@Param("id") Long id, @Param("tag") Tag tag);

	@Query("select t from Tag t where t.id in :ids or t.slug in :slugs")
	Set<Tag> findAllByIdOrSlug(Iterable<Long> ids, Iterable<String> slugs);
}
