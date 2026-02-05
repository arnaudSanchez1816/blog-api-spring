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

	public TagService(TagRepository tagRepository)
	{
		this.tagRepository = tagRepository;
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
		Tag newTag = new Tag(HtmlUtils.htmlEscape(createTagDto.name()), HtmlUtils.htmlEscape(createTagDto.slug()));
		newTag = tagRepository.save(newTag);

		return newTag;
	}

	@Transactional
	public Tag updateTag(Long id, UpdateTagRequest updateTagDto)
	{
		Tag updatedTag = new Tag(HtmlUtils.htmlEscape(updateTagDto.name()), HtmlUtils.htmlEscape(updateTagDto.slug()));
		updatedTag.setId(id);
		updatedTag = tagRepository.save(updatedTag);

		return updatedTag;
	}
}
