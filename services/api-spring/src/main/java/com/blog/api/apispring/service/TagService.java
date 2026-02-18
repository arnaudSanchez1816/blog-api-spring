package com.blog.api.apispring.service;

import com.blog.api.apispring.dto.tag.CreateTagRequest;
import com.blog.api.apispring.dto.tag.UpdateTagRequest;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TagService
{
	private final TagRepository tagRepository;
	private final TextService textService;

	public TagService(TagRepository tagRepository, TextService textService)
	{
		this.tagRepository = tagRepository;
		this.textService = textService;
	}

	public Optional<Tag> getTag(Long id)
	{
		return tagRepository.findById(id);
	}

	public Optional<Tag> getTag(String slug)
	{
		return tagRepository.findBySlug(slug);
	}

	public List<Tag> getAllTags()
	{
		return tagRepository.findAll();
	}

	public Set<Tag> getAllTagsById(Iterable<Long> ids)
	{
		return tagRepository.findAllByIdOrSlug(ids, Collections.emptyList());
	}

	public Set<Tag> getAllTagsBySlug(Iterable<String> slugs)
	{
		return tagRepository.findAllByIdOrSlug(Collections.emptyList(), slugs);
	}

	public Set<Tag> getAllTagsByIdOrSlug(Iterable<Long> ids, Iterable<String> slugs)
	{
		return tagRepository.findAllByIdOrSlug(ids, slugs);
	}

	public Tag saveTag(Tag tag)
	{
		return tagRepository.save(tag);
	}

	@Transactional
	public Optional<Tag> deleteTag(Long id)
	{
		return tagRepository.deleteTagById(id);
	}

	@Transactional
	public Optional<Tag> deleteTag(String slug)
	{
		return tagRepository.deleteTagBySlug(slug);
	}

	@Transactional
	public Tag createTag(CreateTagRequest createTagDto)
	{
		Tag newTag = new Tag(textService.sanitizeText(createTagDto.name()),
				textService.sanitizeText(createTagDto.slug()));
		newTag = tagRepository.save(newTag);

		return newTag;
	}

	@Transactional
	public Tag updateTag(Long id, UpdateTagRequest updateTagDto)
	{
		Tag updatedTag = new Tag(textService.sanitizeText(updateTagDto.name()),
				textService.sanitizeText(updateTagDto.slug()));
		updatedTag.setId(id);
		updatedTag = tagRepository.save(updatedTag);

		return updatedTag;
	}
}
