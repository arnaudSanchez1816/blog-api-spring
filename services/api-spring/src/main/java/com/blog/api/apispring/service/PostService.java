package com.blog.api.apispring.service;

import com.blog.api.apispring.dto.posts.GetPostsRequest;
import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.model.*;
import com.blog.api.apispring.projection.*;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.specs.PostSpecs;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@Slf4j
public class PostService
{
	private final PostRepository postRepository;
	private final TagService tagService;
	private final EntityManager entityManager;
	private final CommentService commentService;

	public PostService(PostRepository postRepository, TagService tagService, EntityManager entityManager,
					   CommentService commentService)
	{
		this.postRepository = postRepository;
		this.tagService = tagService;
		this.entityManager = entityManager;
		this.commentService = commentService;
	}

	public Optional<PostInfoWithAuthor> getPostInfo(long id)
	{
		return postRepository.findInfoWithAuthorById(id);
	}

	public Optional<PostInfoWithAuthorAndTags> getPostInfoWithTags(long id)
	{
		return postRepository.findInfoWithTagsById(id);
	}

	public Optional<PostInfoWithAuthorAndComments> getPostInfoWithComments(long id)
	{
		return postRepository.findInfoWithCommentsById(id);
	}

	public Optional<PostInfoWithAuthorTagsComments> getPostsInfoWithTagsComments(long id)
	{
		return postRepository.findInfoWithTagsAndCommentsById(id);
	}

	public Page<PostInfoWithAuthorAndTags> getPageablePostsInfo(GetPostsRequest getPostsRequest)
	{
		Specification<Post> specs = getPostsRequest.toSpecifications();
		Pageable pageable = getPostsRequest.toPageable();
		return postRepository.findBy(specs,
				sfq -> sfq.as(PostInfoWithAuthorAndTags.class)
						  .page(pageable));
	}

	public Page<PostInfoWithAuthorAndTags> getPageablePostsInfoByAuthor(GetPostsRequest getPostsRequest, long authorId)
	{
		Specification<Post> specs = getPostsRequest.toSpecifications()
												   .and(PostSpecs.withAuthor(authorId));
		Pageable pageable = getPostsRequest.toPageable();

		return postRepository.findBy(specs,
				sfq -> sfq.as(PostInfoWithAuthorAndTags.class)
						  .page(pageable));
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
		Optional<PostInfoWithAuthorAndTags> opUpdatedPost = postRepository.findInfoWithTagsById(post.getId());
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

	public Comment addCommentToPost(Post post, String username, String body)
	{
		return commentService.addCommentToPost(post, username, body);
	}

	public Post publishPost(Post post)
	{
		if (post.isPublished())
		{
			log.info("Post {} is already published", post.getId());
			return post;
		}

		post.setPublishedAt(OffsetDateTime.now());
		return postRepository.save(post);
	}

	public Post hidePost(Post post)
	{
		if (!post.isPublished())
		{
			log.info("Post {} is already hidden", post.getId());
			return post;
		}

		post.setPublishedAt(null);
		return postRepository.save(post);
	}
}
