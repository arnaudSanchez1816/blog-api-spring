package com.blog.api.apispring.service;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.*;
import com.blog.api.apispring.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Service
public class PostService
{
	private final PostRepository postRepository;
	private final TagService tagService;
	private final EntityManager entityManager;

	public PostService(PostRepository postRepository, TagService tagService, EntityManager entityManager)
	{
		this.postRepository = postRepository;
		this.tagService = tagService;
		this.entityManager = entityManager;
	}

	public Optional<PostInfoWithAuthor> getPost(long id)
	{
		return postRepository.findWithAuthorById(id);
	}

	public Optional<PostInfoWithAuthorAndTags> getPostWithTags(long id)
	{
		return postRepository.findWithTagsById(id);
	}

	public Optional<PostInfoWithAuthorAndComments> getPostWithComments(long id)
	{
		return postRepository.findWithCommentsById(id);
	}

	public Optional<PostInfoWithAuthorTagsComments> getPostsWithTagsComments(long id)
	{
		return postRepository.findWithTagsAndCommentsById(id);
	}

	public Page<PostInfoWithAuthorAndTags> getPosts(Pageable pageable)
	{
		if (pageable == null)
		{
			pageable = Pageable.unpaged();
		}

		return postRepository.findAllWithTags(pageable);
	}

	public Long getCommentsCount(long id)
	{
		return postRepository.countCommentsById(id);
	}

	public Map<Long, Long> getCommentsCount(List<Long> postIds)
	{
		if (postIds == null || postIds.isEmpty())
		{
			return Collections.emptyMap();
		}

		return postRepository.countCommentsByIds(postIds);
	}

	public void deletePost(long id)
	{
		postRepository.deleteById(id);
	}

	public Post createPost(String title, long authorId)
	{
		User author = entityManager.getReference(User.class, authorId);

		Post newPost = new Post();
		newPost.setTitle(HtmlUtils.htmlEscape(title));
		newPost.setAuthor(author);

		return postRepository.save(newPost);
	}

	@Transactional
	public PostInfoWithAuthorAndTags updatePost(Post post, String title, String body, Set<TagIdOrSlug> tagIdsOrSlugs)
	{
		if (body != null)
		{
			// TODO : parse body
			String parsedBody = body;
			post.setBody(parsedBody);
		}

		if (title != null)
		{
			String parsedTitle = HtmlUtils.htmlEscape(title);
			post.setTitle(title);
		}

		if (tagIdsOrSlugs != null)
		{
			// TODO : parse tags
			Set<Tag> newTags = new HashSet<>();
			if (!tagIdsOrSlugs.isEmpty())
			{
				List<Long> ids = new ArrayList<>();
				List<String> slugs = new ArrayList<>();
				for (TagIdOrSlug idOrSlug : tagIdsOrSlugs)
				{
					if (idOrSlug.isSlug())
					{
						slugs.add(idOrSlug.getSlug());
					} else
					{
						ids.add(idOrSlug.getId());
					}
				}
				newTags = tagService.getAllTagsByIdOrSlug(ids, slugs);
			}
			post.setTags(newTags);
		}

		post = postRepository.save(post);
		// TODO : Check if there is a way to have that projection without a query.
		Optional<PostInfoWithAuthorAndTags> opUpdatedPost = postRepository.findWithTagsById(post.getId());
		if (opUpdatedPost.isEmpty())
		{
			throw new RuntimeException("Post is unexpectedly null. This should not happen");
		}

		return opUpdatedPost.get();
	}

	@Transactional
	public PostInfoWithAuthorAndTags updatePost(long id, String title, String body,
												Set<TagIdOrSlug> tagIdsOrSlugs) throws NoSuchElementException
	{
		Optional<Post> opPost = postRepository.findById(id);
		if (opPost.isEmpty())
		{
			throw new NoSuchElementException(String.format("Post with id : %d does not exists.", id));
		}

		Post post = opPost.get();
		return updatePost(post, title, body, tagIdsOrSlugs);
	}
}
