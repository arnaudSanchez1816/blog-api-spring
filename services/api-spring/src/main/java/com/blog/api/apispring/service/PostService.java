package com.blog.api.apispring.service;

import com.blog.api.apispring.dto.posts.GetPostsRequest;
import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.model.*;
import com.blog.api.apispring.projection.*;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.specs.PostSpecs;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Cleaner cleaner = new Cleaner(Safelist.none());
		if (body != null)
		{
			// Update body, description and reading time
			String sanitizedBody = cleaner.clean(Jsoup.parse(body))
										  .text();
			post.setBody(sanitizedBody);

			// Parse markdown to plain text for processing.
			Parser parser = Parser.builder()
								  .build();
			Node parsedBodyMarkdown = parser.parse(body);
			TextContentRenderer textRenderer = TextContentRenderer.builder()
																  .build();

			String plainTextBody = textRenderer.render(parsedBodyMarkdown);
			// Maybe we can only clean the body once?
			plainTextBody = cleaner.clean(Jsoup.parse(plainTextBody))
								   .text();

			// Description
			String description = plainTextBody;
			// Get first 50 words
			Pattern pattern = Pattern.compile("(^(?:\\S+\\s*){1,50}).*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(description);
			if (matcher.find())
			{
				String first50WordsGroup = matcher.group(1);
				if (first50WordsGroup != null)
				{
					description = first50WordsGroup.trim() + "...";
				}
			}
			post.setDescription(description);

			// Reading time
			int readingTime = estimateReadingTime(plainTextBody);
			post.setReadingTime(readingTime);
		}

		if (title != null)
		{
			String sanitizedTitle = cleaner.clean(Jsoup.parse(title))
										   .text();
			post.setTitle(sanitizedTitle);
		}

		if (tagIdsOrSlugs != null)
		{
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
				// todo : is there a way to fetch entity references from a slug ?
				newTags = tagService.getAllTagsByIdOrSlug(ids, slugs);
			}
			post.setTags(newTags);
		}

		post = postRepository.save(post);
		// Eager fetch tags and author, maybe there is a better way?
		Set<Tag> postTags = post.getTags();
		User author = post.getAuthor();
		// Make a projection from the post.
		ProjectionFactory pf = new SpelAwareProxyProjectionFactory();
		PostInfoWithAuthorAndTags postProjection = pf.createProjection(PostInfoWithAuthorAndTags.class, post);

		return postProjection;
	}

	private int estimateReadingTime(String plainText)
	{
		Pattern pattern = Pattern.compile("\\S+");
		Matcher matcher = pattern.matcher(plainText);
		int count = 0;
		while (matcher.find())
		{
			count += 1;
		}

		return count > 0 ? Math.max(count / 200, 1) : 1;
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
