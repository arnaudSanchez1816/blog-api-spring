package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.TagRepository;
import com.blog.api.apispring.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

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
}
