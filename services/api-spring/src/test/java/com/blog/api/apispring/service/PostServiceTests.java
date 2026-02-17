package com.blog.api.apispring.service;

import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTests
{
	@Mock
	private PostRepository postRepository;

	@Mock
	private TagService tagService;

	@Mock
	private CommentService commentService;

	@Mock
	private TextService textService;

	@Mock
	private MarkdownService markdownService;

	@Mock
	private EntityManager entityManager;

	private PostService postService;

	@BeforeEach
	void setUp()
	{
		postService = new PostService(postRepository,
				tagService,
				entityManager,
				commentService,
				textService,
				markdownService);
	}

	@Nested
	class GetPostInfo
	{
		@Test
		void shouldReturnPostInfoWhenPostExists()
		{
			long postId = 1L;
			PostInfoWithAuthor mockPostInfo = mock(PostInfoWithAuthor.class);
			when(postRepository.findInfoWithAuthorById(postId)).thenReturn(Optional.of(mockPostInfo));

			Optional<PostInfoWithAuthor> result = postService.getPostInfo(postId);

			assertTrue(result.isPresent());
			assertEquals(mockPostInfo, result.get());
			verify(postRepository).findInfoWithAuthorById(postId);
		}

		@Test
		void shouldReturnEmptyOptionalWhenPostDoesNotExist()
		{
			long postId = 999L;
			when(postRepository.findInfoWithAuthorById(postId)).thenReturn(Optional.empty());

			Optional<PostInfoWithAuthor> result = postService.getPostInfo(postId);

			assertFalse(result.isPresent());
			verify(postRepository).findInfoWithAuthorById(postId);
		}
	}

	@Nested
	class CreatePost
	{
		@Test
		void createPost_ShouldSanitizeTitle()
		{
			postService.createPost("Title", 1L);

			verify(textService).sanitizeText("Title");
		}
	}

	@Nested
	class UpdatePost
	{
		private Post mockPost;

		@BeforeEach
		void setUp()
		{
			mockPost = Mockito.mock(Post.class);
			when(postRepository.save(mockPost)).thenReturn(mockPost);
		}

		@Test
		void updatePost_ShouldSavePost()
		{
			postService.updatePost(mockPost, "Title", "Body", Collections.emptySet());

			verify(postRepository).save(mockPost);
		}

		@Test
		void updatePost_ShouldFetchTags_AfterSavingPost()
		{
			postService.updatePost(mockPost, "Title", "body", Collections.emptySet());

			InOrder inOrder = inOrder(postRepository, mockPost);
			inOrder.verify(postRepository)
				   .save(mockPost);
			inOrder.verify(mockPost)
				   .getTags();
		}

		@Test
		void updatePost_ShouldFetchAuthor_AfterSavingPost()
		{
			postService.updatePost(mockPost, "Title", "body", Collections.emptySet());

			InOrder inOrder = inOrder(postRepository, mockPost);
			inOrder.verify(postRepository)
				   .save(mockPost);
			inOrder.verify(mockPost)
				   .getAuthor();
		}

		@Test
		void updatePost_ShouldUpdateTitle_WhenGivenTitle()
		{
			when(textService.sanitizeText("Title")).thenReturn("Sanitized title");
			postService.updatePost(mockPost, "Title", null, null);

			verify(mockPost).setTitle("Sanitized title");
		}

		@Test
		void updatePost_ShouldNotUpdateTitle_WhenGivenNullTitle()
		{
			postService.updatePost(mockPost, null, null, null);

			verify(mockPost, Mockito.never()).setTitle(Mockito.anyString());
		}

		@Test
		void updatePost_ShouldUpdateBody_WhenGivenBody()
		{
			when(textService.sanitizeText("Body")).thenReturn("Sanitized body");
			postService.updatePost(mockPost, null, "Body", null);

			verify(mockPost).setBody("Sanitized body");
		}

		@Test
		void updatePost_ShouldNotUpdateBody_WhenGivenNullBody()
		{
			postService.updatePost(mockPost, null, null, null);

			verify(mockPost, never()).setBody(Mockito.anyString());
		}

		@Test
		void updatePost_ShouldUpdateDescription_WhenGivenBody()
		{
			when(textService.sanitizeText("Body")).thenReturn("Sanitized body");
			when(markdownService.parseMarkdownToPlainText("Sanitized body")).thenReturn("Plain body");

			when(textService.getFirstWordsSubstring(eq("Plain body"), Mockito.anyInt())).thenReturn("Description");
			postService.updatePost(mockPost, null, "Body", null);

			verify(mockPost).setDescription("Description...");
		}

		@Test
		void updatePost_ShouldNotUpdateDescription_WhenGivenNullBody()
		{
			postService.updatePost(mockPost, null, null, null);

			verify(mockPost, never()).setDescription(Mockito.anyString());
		}

		@Test
		void updatePost_ShouldUpdateReadingTime_WhenGivenBody()
		{
			when(textService.sanitizeText("Body")).thenReturn("Sanitized body");
			when(markdownService.parseMarkdownToPlainText("Sanitized body")).thenReturn("Plain body");

			when(textService.getFirstWordsSubstring(eq("Plain body"), Mockito.anyInt())).thenReturn("Description");
			when(textService.estimateReadingTime("Plain body")).thenReturn(10);
			postService.updatePost(mockPost, null, "Body", null);

			verify(mockPost).setReadingTime(10);
		}

		@Test
		void updatePost_ShouldNotUpdateReadingTime_WhenGivenNullBody()
		{
			postService.updatePost(mockPost, null, null, null);

			verify(mockPost, never()).setReadingTime(Mockito.anyInt());
		}

		@Test
		void updatePost_ShouldUpdateTags_WhenGivenTags()
		{
			TagIdOrSlug idTag = Mockito.mock(TagIdOrSlug.class);
			when(idTag.isSlug()).thenReturn(false);
			when(idTag.getId()).thenReturn(1L);
			TagIdOrSlug slugTag = Mockito.mock(TagIdOrSlug.class);
			when(slugTag.isSlug()).thenReturn(true);
			when(slugTag.getSlug()).thenReturn("slug");
			Set<TagIdOrSlug> tagIdOrSlugs = new HashSet<>(Arrays.asList(idTag, slugTag));

			Set<Tag> tags = new HashSet<>(Arrays.asList(Mockito.mock(Tag.class), Mockito.mock(Tag.class)));
			when(tagService.getAllTagsByIdOrSlug(Mockito.any(), Mockito.any())).thenReturn(tags);

			postService.updatePost(mockPost, null, null, tagIdOrSlugs);

			verify(mockPost).setTags(argThat(set -> set.size() == 2));
		}

		@Test
		void updatePost_ShouldUpdateTags_WhenGivenEmptyTagSet()
		{
			postService.updatePost(mockPost, null, null, Collections.emptySet());

			verify(mockPost).setTags(argThat(Set::isEmpty));
		}

		@Test
		void updatePost_ShouldNotUpdateTags_WhenGivenNullTagSet()
		{
			postService.updatePost(mockPost, null, null, null);

			verify(mockPost, never()).setTags(anySet());
		}
	}

	@Nested
	class PublishPost
	{
		@Test
		void publishPost_ShouldSetPostPublishedAt_WhenUnpublished()
		{
			OffsetDateTime dateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			try (MockedStatic<OffsetDateTime> mocked = Mockito.mockStatic(OffsetDateTime.class))
			{
				Post mockPost = mock(Post.class);
				when(mockPost.isPublished()).thenReturn(false);
				mocked.when(OffsetDateTime::now)
					  .thenReturn(dateTime);
				postService.publishPost(mockPost);

				verify(mockPost).setPublishedAt(dateTime);
				verify(postRepository).save(mockPost);
			}
		}

		@Test
		void publishPost_ShouldDoNothing_WhenAlreadyPublished()
		{
			Post mockPost = mock(Post.class);
			when(mockPost.isPublished()).thenReturn(true);
			postService.publishPost(mockPost);

			verify(mockPost, Mockito.never()).setPublishedAt(Mockito.any());
			verify(postRepository, Mockito.never()).save(mockPost);
		}
	}

	@Nested
	class HidePost
	{
		@Test
		void hidePost_ShouldSetPostPublishedAtToNull_WhenPublished()
		{
			Post mockPost = mock(Post.class);
			when(mockPost.isPublished()).thenReturn(true);
			postService.hidePost(mockPost);

			verify(mockPost).setPublishedAt(null);
			verify(postRepository).save(mockPost);
		}

		@Test
		void hidePost_ShouldDoNothing_WhenAlreadyUnpublished()
		{
			Post mockPost = mock(Post.class);
			when(mockPost.isPublished()).thenReturn(false);
			postService.hidePost(mockPost);

			verify(mockPost, Mockito.never()).setPublishedAt(null);
			verify(postRepository, Mockito.never()).save(mockPost);
		}
	}
}