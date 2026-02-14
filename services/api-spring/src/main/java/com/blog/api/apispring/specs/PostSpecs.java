package com.blog.api.apispring.specs;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Post_;
import com.blog.api.apispring.model.Tag_;
import com.blog.api.apispring.model.User_;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PostSpecs
{
	public static PredicateSpecification<Post> onlyPublished(boolean onlyPublished)
	{
		return (from, criteriaBuilder) ->
		{
			if (onlyPublished)
			{
				return criteriaBuilder.isNotNull(from.get(Post_.PUBLISHED_AT));
			} else
			{
				return criteriaBuilder.conjunction();
			}
		};
	}

	public static PredicateSpecification<Post> titleContains(String q)
	{
		return (from, criteriaBuilder) ->
				q == null || q.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.like(criteriaBuilder.lower(
						from.get(Post_.title)), criteriaBuilder.lower(criteriaBuilder.literal("%" + q + "%")));
	}

	public static Specification<Post> withTags(Collection<TagIdOrSlug> tags)
	{
		if (tags == null || tags.isEmpty())
		{
			return ((_, query, criteriaBuilder) ->
					{
						query.distinct(true);
						return criteriaBuilder.conjunction();
					});
		}

		Set<Long> tagIds = new HashSet<>();
		Set<String> tagSlugs = new HashSet<>();
		for (TagIdOrSlug idOrSlug : tags)
		{
			if (idOrSlug.isId())
			{
				tagIds.add(idOrSlug.getId());
			} else if (idOrSlug.isSlug())
			{
				tagSlugs.add(idOrSlug.getSlug());
			}
		}

		return (from, query, criteriaBuilder) ->
		{
			query.distinct(true);

			CriteriaBuilder.In<Object> inIds = criteriaBuilder.in(from.get(Post_.TAGS)
																	  .get(Tag_.ID));
			inIds.value(tagIds);

			CriteriaBuilder.In<Object> inSlugs = criteriaBuilder.in(from.get(Post_.TAGS)
																		.get(Tag_.SLUG));
			inSlugs.value(tagSlugs);

			return criteriaBuilder.or(inIds, inSlugs);
		};
	}

	public static PredicateSpecification<Post> withAuthor(long userId)
	{
		return ((from, criteriaBuilder) -> criteriaBuilder.equal(from.get(Post_.AUTHOR)
																	 .get(User_.ID), userId));
	}
}
