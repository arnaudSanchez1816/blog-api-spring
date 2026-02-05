package com.blog.api.apispring.repository;

import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for TagRepository.
 * This class tests the updateTag methods in the TagRepository interface.
 */
@SpringBootTest
@ExtendWith(ClearDatabaseExtension.class)
class TagRepositoryTests
{
	@Autowired
	private TagRepository tagRepository;

	/**
	 * Test updateTag(String slug, Tag tag) method.
	 * Verifies that a tag can be updated by slug.
	 */
	@Test
	void updateTagBySlug_UpdatesTag_WhenTagExists()
	{
		Tag tag = new Tag("Java", "java");
		tag = tagRepository.save(tag);

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag("java", updatedTag);

		Optional<Tag> result = tagRepository.findBySlug("java-programming");
		assertThat(result).isPresent();
		assertThat(result.get()
						 .getName()).isEqualTo("Java Programming");
		assertThat(result.get()
						 .getSlug()).isEqualTo("java-programming");

		Optional<Tag> oldSlug = tagRepository.findBySlug("java");
		assertThat(oldSlug).isEmpty();
	}

	/**
	 * Test updateTag(String slug, Tag tag) method.
	 * Verifies that updating a tag by slug does not affect other tags.
	 */
	@Test
	void updateTagBySlug_DoesNotAffectOtherTags_WhenUpdatingOneTag()
	{
		Tag tag1 = new Tag("Java", "java");
		tag1 = tagRepository.save(tag1);

		Tag tag2 = new Tag("Spring", "spring");
		tag2 = tagRepository.save(tag2);

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag("java", updatedTag);

		Optional<Tag> springTag = tagRepository.findBySlug("spring");
		assertThat(springTag).isPresent();
		assertThat(springTag.get()
							.getName()).isEqualTo("Spring");
		assertThat(springTag.get()
							.getSlug()).isEqualTo("spring");
	}

	/**
	 * Test updateTag(String slug, Tag tag) method.
	 * Verifies that updating a tag with a non-existent slug does not create a new tag.
	 */
	@Test
	void updateTagBySlug_DoesNotCreateTag_WhenSlugDoesNotExist()
	{
		long initialCount = tagRepository.count();

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag("non-existent", updatedTag);

		long finalCount = tagRepository.count();
		assertThat(finalCount).isEqualTo(initialCount);

		Optional<Tag> result = tagRepository.findBySlug("java-programming");
		assertThat(result).isEmpty();
	}

	/**
	 * Test updateTag(Long id, Tag tag) method.
	 * Verifies that a tag can be updated by id.
	 */
	@Test
	void updateTagById_UpdatesTag_WhenTagExists()
	{
		Tag tag = new Tag("Java", "java");
		tag = tagRepository.save(tag);
		Long tagId = tag.getId();

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag(tagId, updatedTag);

		Optional<Tag> result = tagRepository.findById(tagId);
		assertThat(result).isPresent();
		assertThat(result.get()
						 .getName()).isEqualTo("Java Programming");
		assertThat(result.get()
						 .getSlug()).isEqualTo("java-programming");
	}

	/**
	 * Test updateTag(Long id, Tag tag) method.
	 * Verifies that updating a tag by id does not affect other tags.
	 */
	@Test
	void updateTagById_DoesNotAffectOtherTags_WhenUpdatingOneTag()
	{
		Tag tag1 = new Tag("Java", "java");
		tag1 = tagRepository.save(tag1);
		Long tag1Id = tag1.getId();

		Tag tag2 = new Tag("Spring", "spring");
		tag2 = tagRepository.save(tag2);

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag(tag1Id, updatedTag);

		Optional<Tag> springTag = tagRepository.findBySlug("spring");
		assertThat(springTag).isPresent();
		assertThat(springTag.get()
							.getName()).isEqualTo("Spring");
		assertThat(springTag.get()
							.getSlug()).isEqualTo("spring");
	}

	/**
	 * Test updateTag(Long id, Tag tag) method.
	 * Verifies that updating a tag with a non-existent id does not create a new tag.
	 */
	@Test
	void updateTagById_DoesNotCreateTag_WhenIdDoesNotExist()
	{
		long initialCount = tagRepository.count();

		Tag updatedTag = new Tag("Java Programming", "java-programming");
		tagRepository.updateTag(99999L, updatedTag);

		long finalCount = tagRepository.count();
		assertThat(finalCount).isEqualTo(initialCount);

		Optional<Tag> result = tagRepository.findBySlug("java-programming");
		assertThat(result).isEmpty();
	}

	/**
	 * Test updateTag(String slug, Tag tag) method.
	 * Verifies that only the name can be updated while keeping the slug the same.
	 */
	@Test
	void updateTagBySlug_UpdatesOnlyName_WhenSlugRemainsTheSame()
	{
		Tag tag = new Tag("Java", "java");
		tag = tagRepository.save(tag);

		Tag updatedTag = new Tag("Java Programming Language", "java");
		tagRepository.updateTag("java", updatedTag);

		Optional<Tag> result = tagRepository.findBySlug("java");
		assertThat(result).isPresent();
		assertThat(result.get()
						 .getName()).isEqualTo("Java Programming Language");
		assertThat(result.get()
						 .getSlug()).isEqualTo("java");
	}

	/**
	 * Test updateTag(Long id, Tag tag) method.
	 * Verifies that only the name can be updated while keeping the slug the same.
	 */
	@Test
	void updateTagById_UpdatesOnlyName_WhenSlugRemainsTheSame()
	{
		Tag tag = new Tag("Java", "java");
		tag = tagRepository.save(tag);
		Long tagId = tag.getId();

		Tag updatedTag = new Tag("Java Programming Language", "java");
		tagRepository.updateTag(tagId, updatedTag);

		Optional<Tag> result = tagRepository.findById(tagId);
		assertThat(result).isPresent();
		assertThat(result.get()
						 .getName()).isEqualTo("Java Programming Language");
		assertThat(result.get()
						 .getSlug()).isEqualTo("java");
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that tags can be found by their IDs.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsMatchingTags_WhenSearchingByIds()
	{
		Tag tag1 = tagRepository.save(new Tag("Java", "java"));
		Tag tag2 = tagRepository.save(new Tag("Spring", "spring"));
		Tag tag3 = tagRepository.save(new Tag("Docker", "docker"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(tag1.getId(), tag2.getId()),
				java.util.List.of());

		assertThat(result).hasSize(2);
		assertThat(result).contains(tag1, tag2);
		assertThat(result).doesNotContain(tag3);
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that tags can be found by their slugs.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsMatchingTags_WhenSearchingBySlugs()
	{
		Tag tag1 = tagRepository.save(new Tag("Java", "java"));
		Tag tag2 = tagRepository.save(new Tag("Spring", "spring"));
		Tag tag3 = tagRepository.save(new Tag("Docker", "docker"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(), java.util.List.of("java", "spring"));

		assertThat(result).hasSize(2);
		assertThat(result).contains(tag1, tag2);
		assertThat(result).doesNotContain(tag3);
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that tags can be found by both IDs and slugs.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsMatchingTags_WhenSearchingByIdsAndSlugs()
	{
		Tag tag1 = tagRepository.save(new Tag("Java", "java"));
		Tag tag2 = tagRepository.save(new Tag("Spring", "spring"));
		Tag tag3 = tagRepository.save(new Tag("Docker", "docker"));
		Tag tag4 = tagRepository.save(new Tag("Kubernetes", "kubernetes"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(tag1.getId(), tag2.getId()),
				java.util.List.of("docker", "kubernetes"));

		assertThat(result).hasSize(4);
		assertThat(result).contains(tag1, tag2, tag3, tag4);
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that an empty set is returned when no matches are found.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsEmptySet_WhenNoMatchesFound()
	{
		tagRepository.save(new Tag("Java", "java"));
		tagRepository.save(new Tag("Spring", "spring"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(99999L, 88888L),
				java.util.List.of("non-existent", "also-non-existent"));

		assertThat(result).isEmpty();
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that an empty set is returned when input collections are empty.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsEmptySet_WhenInputCollectionsAreEmpty()
	{
		tagRepository.save(new Tag("Java", "java"));
		tagRepository.save(new Tag("Spring", "spring"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(), java.util.List.of());

		assertThat(result).isEmpty();
	}

	/**
	 * Test findAllByIdOrSlug method.
	 * Verifies that the same tag is not duplicated when matched by both ID and slug.
	 */
	@Test
	void findAllByIdOrSlug_ReturnsUniqueTag_WhenSameTagMatchedByIdAndSlug()
	{
		Tag tag1 = tagRepository.save(new Tag("Java", "java"));
		Tag tag2 = tagRepository.save(new Tag("Spring", "spring"));

		var result = tagRepository.findAllByIdOrSlug(java.util.List.of(tag1.getId()), java.util.List.of("java"));

		assertThat(result).hasSize(1);
		assertThat(result).contains(tag1);
	}
}