package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.dto.posts.UpdatePostRequest;
import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.TagRepository;
import com.blog.api.apispring.repository.UserRepository;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Test class for PostController.
 * Tests the getPost method which retrieves a single post by ID.
 */
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(ClearDatabaseExtension.class)
class PostControllerTests
{
	@Autowired
	private MockMvcTester mockMvc;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	/**
	 * Test getPost returns 200 OK with valid post data when given a valid post ID.
	 */
	@Test
	void getPost_IsOk_WhenGivenValidPostId()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Test Post Title");
		post.setDescription("Test post description");
		post.setBody("Test post body content");
		post.setReadingTime(5);
		post.setAuthor(author);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/" + post.getId())
										.exchange();

		Post finalPost = post;
		User finalAuthor = author;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(PostDto.class)
							.satisfies(dto ->
							{
								assertThat(dto.getId()).isEqualTo(finalPost.getId());
								assertThat(dto.getTitle()).isEqualTo("Test Post Title");
								assertThat(dto.getDescription()).isEqualTo("Test post description");
								assertThat(dto.getBody()).isEqualTo("Test post body content");
								assertThat(dto.getReadingTime()).isEqualTo(5);
								assertThat(dto.getPublishedAt()).isNull();
								assertThat(dto.getAuthor()).isNotNull();
								assertThat(dto.getAuthor()
											  .id()).isEqualTo(finalAuthor.getId());
								assertThat(dto.getAuthor()
											  .name()).isEqualTo("Author Name");
								assertThat(dto.getCommentsCount()).isEqualTo(0L);
							});
	}

	/**
	 * Test getPost returns 200 OK with post including tags when post has associated tags.
	 */
	@Test
	void getPost_IsOk_WhenPostHasTags()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Tag tag1 = new Tag("Java", "java");
		Tag tag2 = new Tag("Spring", "spring");
		tag1 = tagRepository.save(tag1);
		tag2 = tagRepository.save(tag2);

		Post post = new Post();
		post.setTitle("Post with Tags");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		post.addTag(tag1);
		post.addTag(tag2);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/" + post.getId())
										.exchange();

		Post finalPost = post;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(PostDto.class)
							.satisfies(dto ->
							{
								assertThat(dto.getId()).isEqualTo(finalPost.getId());
								assertThat(dto.getTags()).isNotNull();
								assertThat(dto.getTags()).hasSize(2);
							});
	}

	/**
	 * Test getPost returns 200 OK with correct comments count when post has comments.
	 */
	@Test
	void getPost_IsOk_WithCorrectCommentsCount()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post with Comments");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		Comment comment = new Comment();
		comment.setBody("Comment body");
		comment.setUsername("Comment username");
		comment.setCreatedAt(OffsetDateTime.now());
		post.addComment(comment);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(PostDto.class)
							.satisfies(dto ->
							{
								assertThat(dto.getCommentsCount()).isEqualTo(1L);
							});
	}

	/**
	 * Test getPost returns 404 NOT FOUND when given a non-existent post ID.
	 */
	@Test
	void getPost_Is404_WhenPostIdDoesNotExist()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/999999")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test getPost returns 404 NOT FOUND when given an invalid negative post ID.
	 */
	@Test
	void getPost_Is404_WhenGivenNegativePostId()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/-1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test getPost returns 404 NOT FOUND when given zero as post ID.
	 */
	@Test
	void getPost_Is404_WhenGivenZeroAsPostId()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/0")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test getPosts returns 200 OK with empty list when no posts exist.
	 */
	@Test
	void getPosts_IsOk_WhenNoPostsExist()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results")
									.asInstanceOf(LIST)
									.hasSize(0);
								json.assertThat()
									.extractingPath("$.metadata.count")
									.isEqualTo(0);
								json.assertThat()
									.extractingPath("$.metadata.page")
									.isEqualTo(0);
								json.assertThat()
									.extractingPath("$.metadata.pageSize")
									.isEqualTo(10);
							});
	}

	/**
	 * Test getPosts returns 200 OK with single post when one post exists.
	 */
	@Test
	void getPosts_IsOk_WhenOnePostExists()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Test Post");
		post.setDescription("Test description");
		post.setBody("Test body");
		post.setReadingTime(5);
		post.setAuthor(author);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		Post finalPost = post;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results")
									.convertTo(LIST)
									.hasSize(1);
								json.assertThat()
									.extractingPath("$.results[0].id")
									.isEqualTo(finalPost.getId()
														.intValue());
								json.assertThat()
									.extractingPath("$.results[0].title")
									.isEqualTo("Test Post");
							});
	}

	/**
	 * Test getPosts returns 200 OK with multiple posts in correct order.
	 */
	@Test
	void getPosts_IsOk_WithMultiplePosts()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post1 = new Post();
		post1.setTitle("First Post");
		post1.setDescription("Description 1");
		post1.setBody("Body 1");
		post1.setAuthor(author);
		post1 = postRepository.save(post1);

		Post post2 = new Post();
		post2.setTitle("Second Post");
		post2.setDescription("Description 2");
		post2.setBody("Body 2");
		post2.setAuthor(author);
		post2 = postRepository.save(post2);

		Post post3 = new Post();
		post3.setTitle("Third Post");
		post3.setDescription("Description 3");
		post3.setBody("Body 3");
		post3.setAuthor(author);
		post3 = postRepository.save(post3);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results")
									.convertTo(LIST)
									.hasSize(3);
							});
	}

	/**
	 * Test getPosts returns correct metadata including count, page, and pageSize.
	 */
	@Test
	void getPosts_IsOk_WithCorrectMetadata()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		for (int i = 0; i < 5; i++)
		{
			Post post = new Post();
			post.setTitle("Post " + i);
			post.setDescription("Description " + i);
			post.setBody("Body " + i);
			post.setAuthor(author);
			postRepository.save(post);
		}

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.metadata.count")
									.isEqualTo(5);
								json.assertThat()
									.extractingPath("$.metadata.page")
									.isEqualTo(0);
								json.assertThat()
									.extractingPath("$.metadata.pageSize")
									.isEqualTo(10);
							});
	}

	/**
	 * Test getPosts handles pagination correctly by returning second page of results.
	 */
	@Test
	void getPosts_IsOk_WithPagination()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		for (int i = 0; i < 15; i++)
		{
			Post post = new Post();
			post.setTitle("Post " + i);
			post.setDescription("Description " + i);
			post.setBody("Body " + i);
			post.setAuthor(author);
			postRepository.save(post);
		}

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=1&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results")
									.convertTo(LIST)
									.hasSize(5);
								json.assertThat()
									.extractingPath("$.metadata.count")
									.isEqualTo(15);
								json.assertThat()
									.extractingPath("$.metadata.page")
									.isEqualTo(1);
								json.assertThat()
									.extractingPath("$.metadata.pageSize")
									.isEqualTo(10);
							});
	}

	/**
	 * Test getPosts includes tags when posts have associated tags.
	 */
	@Test
	void getPosts_IsOk_WithTags()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Tag tag1 = new Tag("Java", "java");
		Tag tag2 = new Tag("Spring", "spring");
		tag1 = tagRepository.save(tag1);
		tag2 = tagRepository.save(tag2);

		Post post = new Post();
		post.setTitle("Post with Tags");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		post.addTag(tag1);
		post.addTag(tag2);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results[0].tags")
									.convertTo(LIST)
									.hasSize(2);
							});
	}

	/**
	 * Test getPosts includes correct comments count when posts have comments.
	 */
	@Test
	void getPosts_IsOk_WithCommentsCount()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post with Comments");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		Comment comment1 = new Comment();
		comment1.setBody("Comment 1");
		comment1.setUsername("User 1");
		comment1.setCreatedAt(OffsetDateTime.now());
		Comment comment2 = new Comment();
		comment2.setBody("Comment 2");
		comment2.setUsername("User 2");
		comment2.setCreatedAt(OffsetDateTime.now());
		post.addComment(comment1);
		post.addComment(comment2);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=10")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results[0].commentsCount")
									.isEqualTo(2);
							});
	}

	/**
	 * Test getPosts respects custom pageSize parameter.
	 */
	@Test
	void getPosts_IsOk_WithCustomPageSize()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		for (int i = 0; i < 10; i++)
		{
			Post post = new Post();
			post.setTitle("Post " + i);
			post.setDescription("Description " + i);
			post.setBody("Body " + i);
			post.setAuthor(author);
			postRepository.save(post);
		}

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts?page=0&pageSize=3")
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.results")
									.convertTo(LIST)
									.hasSize(3);
								json.assertThat()
									.extractingPath("$.metadata.pageSize")
									.isEqualTo(3);
							});
	}

	/**
	 * Test deletePost returns 200 OK when authenticated user deletes their own post.
	 */
	@Test
	void deletePost_IsOk_WhenAuthenticatedUserDeletesOwnPost()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post to Delete");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.delete()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/" + post.getId())
										.exchange();

		Post finalPost = post;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(PostDto.class)
							.satisfies(dto ->
							{
								assertThat(dto.getId()).isEqualTo(finalPost.getId());
								assertThat(dto.getTitle()).isEqualTo("Post to Delete");
							});

		assertThat(postRepository.findById(finalPost.getId())).isEmpty();
	}

	/**
	 * Test deletePost returns 404 NOT FOUND when post ID does not exist.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "DELETE")
	void deletePost_Is404_WhenPostIdDoesNotExist()
	{
		MvcTestResult response = mockMvc.delete()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/999999")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test deletePost returns 403 FORBIDDEN when authenticated user tries to delete another user's post.
	 */
	@Test
	// TODO : @WithMockUser custom implementation that instantiate a SecurityUser instead of User
	void deletePost_Is403_WhenUserTriesToDeleteAnotherUsersPost()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		User otherUser = new User("other@example.com", "Other User", "password456");
		otherUser = userRepository.save(otherUser);

		Post post = new Post();
		post.setTitle("Post to Delete");
		post.setDescription("Description");
		post.setBody("Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		MvcTestResult response = mockMvc.delete()
										.with(user(new SecurityUser(otherUser)))
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
	}

	/**
	 * Test deletePost returns 404 NOT FOUND when given negative post ID.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "DELETE")
	void deletePost_Is404_WhenGivenNegativePostId()
	{
		MvcTestResult response = mockMvc.delete()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/-1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test deletePost returns 404 NOT FOUND when given zero as post ID.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "DELETE")
	void deletePost_Is404_WhenGivenZeroAsPostId()
	{
		MvcTestResult response = mockMvc.delete()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/posts/0")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test updatePost returns 200 OK when authenticated user updates their own post.
	 */
	@Test
	void updatePost_IsOk_WhenAuthenticatedUserUpdatesOwnPost()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"title": "Updated Title",
					"body": "Updated Body"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		Post finalPost = post;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.id")
									.isEqualTo(finalPost.getId()
														.intValue());
								json.assertThat()
									.extractingPath("$.title")
									.isEqualTo("Updated Title");
								json.assertThat()
									.extractingPath("$.body")
									.isEqualTo("Updated Body");
							});
	}

	/**
	 * Test updatePost returns 200 OK when user has UPDATE authority.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "UPDATE")
	void updatePost_IsOk_WhenUserHasUpdateAuthority()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"title": "Updated Title",
					"body": "Updated Body"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		Post finalPost = post;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.id")
									.isEqualTo(finalPost.getId()
														.intValue());
								json.assertThat()
									.extractingPath("$.title")
									.isEqualTo("Updated Title");
							});
	}

	/**
	 * Test updatePost returns 404 NOT FOUND when post ID does not exist.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "UPDATE")
	void updatePost_Is404_WhenPostIdDoesNotExist()
	{
		String requestBody = """
				{
					"title": "Updated Title"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/999999")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test updatePost returns 403 FORBIDDEN when user tries to update another user's post.
	 */
	@Test
	void updatePost_Is403_WhenUserTriesToUpdateAnotherUsersPost()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		User otherUser = new User("other@example.com", "Other User", "password456");
		otherUser = userRepository.save(otherUser);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"title": "Updated Title"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(otherUser)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
	}

	/**
	 * Test updatePost returns 404 NOT FOUND when given negative post ID.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "UPDATE")
	void updatePost_Is404_WhenGivenNegativePostId()
	{
		String requestBody = """
				{
					"title": "Updated Title"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/-1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test updatePost returns 404 NOT FOUND when given zero as post ID.
	 */
	@Test
	@WithMockUser(value = "admin", authorities = "UPDATE")
	void updatePost_Is404_WhenGivenZeroAsPostId()
	{
		String requestBody = """
				{
					"title": "Updated Title"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/0")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	/**
	 * Test updatePost returns 400 BAD REQUEST when given an empty request body.
	 */
	@Test
	void updatePost_Is400_WhenGivenEmptyRequestBody()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Test updatePost returns 400 BAD REQUEST when given an unvalid title.
	 */
	@Test
	void updatePost_Is400_WhenGivenInvalidTitle()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		UpdatePostRequest request = new UpdatePostRequest(StringUtils.repeat("a", 256), null, null);

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(JsonUtils.asJsonString(request))
										.uri("/posts/1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void updatePost_Is400_WhenGivenInvalidTags()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"tags": ["java", "not@ValidSlug!", "1"]
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/1")
										.exchange();

		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Test updatePost returns 200 OK with tags when updating post with tags.
	 */
	@Test
	void updatePost_IsOk_WithTags()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Tag tag1 = new Tag("Java", "java");
		Tag tag2 = new Tag("Spring", "spring");
		tag1 = tagRepository.save(tag1);
		tag2 = tagRepository.save(tag2);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"title": "Updated Title",
					"tags": ["java", "spring"]
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.tags")
									.convertTo(LIST)
									.hasSize(2);
							});
	}

	/**
	 * Test updatePost returns 200 OK when only title is changed.
	 */
	@Test
	void updatePost_IsOk_WhenOnlyTitleChanged()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"title": "Updated Title Only"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.title")
									.isEqualTo("Updated Title Only");
								json.assertThat()
									.extractingPath("$.body")
									.isEqualTo("Original Body");
							});
	}

	/**
	 * Test updatePost returns 200 OK when only body is changed.
	 */
	@Test
	void updatePost_IsOk_WhenOnlyBodyChanged()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"body": "Updated Body Only"
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.title")
									.isEqualTo("Original Title");
								json.assertThat()
									.extractingPath("$.body")
									.isEqualTo("Updated Body Only");
							});
	}

	/**
	 * Test updatePost returns 200 OK when only tags are changed.
	 */
	@Test
	void updatePost_IsOk_WhenOnlyTagsChanged()
	{
		User author = new User("author@example.com", "Author Name", "password123");
		author = userRepository.save(author);

		Tag tag1 = new Tag("Java", "java");
		tag1 = tagRepository.save(tag1);

		Post post = new Post();
		post.setTitle("Original Title");
		post.setDescription("Original Description");
		post.setBody("Original Body");
		post.setAuthor(author);
		post = postRepository.save(post);

		String requestBody = """
				{
					"tags": ["java"]
				}
				""";

		MvcTestResult response = mockMvc.put()
										.with(user(new SecurityUser(author)))
										.contentType(MediaType.APPLICATION_JSON)
										.content(requestBody)
										.uri("/posts/" + post.getId())
										.exchange();

		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.title")
									.isEqualTo("Original Title");
								json.assertThat()
									.extractingPath("$.tags")
									.convertTo(LIST)
									.hasSize(1);
							});
	}
}
