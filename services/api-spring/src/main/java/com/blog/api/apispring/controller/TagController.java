package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.tag.CreateTagRequest;
import com.blog.api.apispring.dto.tag.GetTagsResponse;
import com.blog.api.apispring.dto.tag.UpdateTagRequest;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.service.TagService;
import com.blog.api.apispring.validation.TagSlug;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@RequestMapping("/tags")
public class TagController
{
	private final TagService tagService;

	public TagController(TagService tagService)
	{
		this.tagService = tagService;
	}

	@GetMapping
	public ResponseEntity<GetTagsResponse> getTags()
	{
		List<Tag> tags = tagService.getAllTags();
		return ResponseEntity.ok(new GetTagsResponse(tags));
	}

	@GetMapping("/{idOrSlug}")
	public ResponseEntity<Tag> getTag(@PathVariable String idOrSlug)
	{
		return handleTagIdOrSlug(idOrSlug, id ->
		{
			Optional<Tag> optionalTag = tagService.getTag(id);
			return optionalTag.map(ResponseEntity::ok)
							  .orElseGet(() -> ResponseEntity.notFound()
															 .build());
		}, slug ->
		{
			Optional<Tag> optionalTag = tagService.getTag(slug);
			return optionalTag.map(ResponseEntity::ok)
							  .orElseGet(() -> ResponseEntity.notFound()
															 .build());
		});
	}

	@PostMapping(produces = "application/json")
	public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest createTagDto, UriComponentsBuilder ucb)
	{
		Tag newTag = tagService.createTag(createTagDto);
		URI tagLocation = ucb.path("/tags/{id}")
							 .buildAndExpand(newTag.getId())
							 .toUri();
		return ResponseEntity.created(tagLocation)
							 .body(newTag);
	}

	@PutMapping("/{idOrSlug}")
	public ResponseEntity<Tag> updateTag(@PathVariable String idOrSlug, @RequestBody UpdateTagRequest updateTagDto)
	{
		return handleTagIdOrSlug(idOrSlug, id ->
		{
			Optional<Tag> tag = tagService.getTag(id);
			if (tag.isEmpty())
			{
				return ResponseEntity.notFound()
									 .build();
			}
			Tag updatedTag = tagService.updateTag(id, updateTagDto);
			return ResponseEntity.ok(updatedTag);
		}, slug ->
		{
			Optional<Tag> tag = tagService.getTag(slug);
			if (tag.isEmpty())
			{
				return ResponseEntity.notFound()
									 .build();
			}
			Tag updatedTag = tagService.updateTag(tag.get()
													 .getId(), updateTagDto);
			return ResponseEntity.ok(updatedTag);
		});
	}

	@DeleteMapping("/{idOrSlug}")
	public ResponseEntity<Tag> deleteTag(@PathVariable String idOrSlug)
	{
		return handleTagIdOrSlug(idOrSlug, id ->
		{
			Optional<Tag> deletedTag = tagService.deleteTag(id);
			return deletedTag.map(ResponseEntity::ok)
							 .orElseGet(() -> ResponseEntity.notFound()
															.build());
		}, slug ->
		{
			Optional<Tag> deletedTag = tagService.deleteTag(slug);
			return deletedTag.map(ResponseEntity::ok)
							 .orElseGet(() -> ResponseEntity.notFound()
															.build());
		});
	}

	private static <T> ResponseEntity<T> handleTagIdOrSlug(String idOrSlug, Function<Long, ResponseEntity<T>> idHandler,
														   Function<String, ResponseEntity<T>> slugHandler)
	{
		try
		{
			long id = Long.parseLong(idOrSlug);
			return idHandler.apply(id);
		} catch (NumberFormatException e)
		{
			if (isSlug(idOrSlug))
			{
				return slugHandler.apply(idOrSlug);
			}
		}
		return ResponseEntity.notFound()
							 .build();
	}

	private static boolean isSlug(@NonNull String slug)
	{
		return slug.length() <= 30 && slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
	}
}
