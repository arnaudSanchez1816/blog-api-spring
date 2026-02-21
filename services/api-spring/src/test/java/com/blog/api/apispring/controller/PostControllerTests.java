package com.blog.api.apispring.controller;

import com.blog.api.apispring.PostgresTestConfig;
import com.blog.api.apispring.dto.posts.CreatePostRequest;
import com.blog.api.apispring.dto.posts.GetPostsRequestImpl;
import com.blog.api.apispring.dto.posts.PostDto;
import com.blog.api.apispring.dto.posts.UpdatePostRequest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Test class for PostController.
 * Tests the getPost method which retrieves a single post by ID.
 */
@SpringBootTest
@Import(PostgresTestConfig.class)
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

	@Nested
	@DisplayName("getPost")
	class GetPost
	{
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
	}

	@Nested
	@DisplayName("getPosts")
	class GetPosts
	{
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
			post.setPublishedAt(OffsetDateTime.now());
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
			post1.setPublishedAt(OffsetDateTime.now());
			post1 = postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Second Post");
			post2.setDescription("Description 2");
			post2.setBody("Body 2");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			post2 = postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Third Post");
			post3.setDescription("Description 3");
			post3.setBody("Body 3");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.now());
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

			Tag tag1 = new Tag("Java", "java");
			tag1 = tagRepository.save(tag1);

			for (int i = 0; i < 5; i++)
			{
				Post post = new Post();
				post.setTitle("Post " + i);
				post.setDescription("Description " + i);
				post.setBody("Body " + i);
				post.setAuthor(author);
				post.addTag(tag1);
				post.setPublishedAt(OffsetDateTime.now());
				postRepository.save(post);
			}

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&tags=java&sortBy=id")
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
									json.assertThat()
										.extractingPath("$.metadata.sortBy")
										.isEqualTo("id");
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
				post.setPublishedAt(OffsetDateTime.now());
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

		@Test
		void getPosts_IsOk_WithInvalidPage()
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
				post.setPublishedAt(OffsetDateTime.now());
				postRepository.save(post);
			}

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=-50&pageSize=10")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.convertTo(LIST)
										.hasSize(10);
									json.assertThat()
										.extractingPath("$.metadata.count")
										.isEqualTo(15);
									json.assertThat()
										.extractingPath("$.metadata.page")
										.isEqualTo(0);
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
			post.setPublishedAt(OffsetDateTime.now());
			post.addTag(tag1);
			post.addTag(tag2);
			post = postRepository.save(post);

			Post post2 = new Post();
			post2.setTitle("Post without Tags");
			post2.setDescription("Description");
			post2.setBody("Body");
			post2.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post2 = postRepository.save(post2);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?tags=java,spring")
											.exchange();

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
										.extractingPath("$.results[0].tags")
										.convertTo(LIST)
										.hasSize(2);
								});
		}

		@Test
		void getPosts_Is400_WithGivenInvalidTags()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Tag tag1 = new Tag("Java", "java");
			tag1 = tagRepository.save(tag1);

			Post post = new Post();
			post.setTitle("Post with Tags");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post.addTag(tag1);
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?tags=@alsdsqkdKL")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
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
			post.setPublishedAt(OffsetDateTime.now());
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
				post.setPublishedAt(OffsetDateTime.now());
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

		@Test
		void getPosts_IsOk_WithTooSmallCustomPageSize()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			for (int i = 0; i < GetPostsRequestImpl.DEFAULT_PAGE_SIZE; i++)
			{
				Post post = new Post();
				post.setTitle("Post " + i);
				post.setDescription("Description " + i);
				post.setBody("Body " + i);
				post.setAuthor(author);
				post.setPublishedAt(OffsetDateTime.now());
				postRepository.save(post);
			}

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=-30")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.convertTo(LIST)
										.hasSize(GetPostsRequestImpl.DEFAULT_PAGE_SIZE);
									json.assertThat()
										.extractingPath("$.metadata.pageSize")
										.isEqualTo(GetPostsRequestImpl.DEFAULT_PAGE_SIZE);
								});
		}

		@Test
		void getPosts_IsOk_WithTooBigCustomPageSize()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			for (int i = 0; i < GetPostsRequestImpl.MAX_PAGE_SIZE; i++)
			{
				Post post = new Post();
				post.setTitle("Post " + i);
				post.setDescription("Description " + i);
				post.setBody("Body " + i);
				post.setAuthor(author);
				post.setPublishedAt(OffsetDateTime.now());
				postRepository.save(post);
			}

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=999")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.convertTo(LIST)
										.hasSize(GetPostsRequestImpl.MAX_PAGE_SIZE);
									json.assertThat()
										.extractingPath("$.metadata.pageSize")
										.isEqualTo(GetPostsRequestImpl.MAX_PAGE_SIZE);
								});
		}

		@Test
		void getPosts_IsOk_WhenSortingByIdAsc()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			post1 = postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Second Post");
			post2.setDescription("Description 2");
			post2.setBody("Body 2");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			post2 = postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Third Post");
			post3.setDescription("Description 3");
			post3.setBody("Body 3");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.now());
			post3 = postRepository.save(post3);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?sortBy=id")
											.exchange();

			Post finalPost1 = post1;
			Post finalPost2 = post2;
			Post finalPost3 = post3;
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
										.extractingPath("$.results[*].id")
										.convertTo(LIST)
										.containsExactly(finalPost1.getId()
																   .intValue(),
												finalPost2.getId()
														  .intValue(),
												finalPost3.getId()
														  .intValue());
								});
		}

		@Test
		void getPosts_IsOk_WhenSortingByIdDesc()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			post1 = postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Second Post");
			post2.setDescription("Description 2");
			post2.setBody("Body 2");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			post2 = postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Third Post");
			post3.setDescription("Description 3");
			post3.setBody("Body 3");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.now());
			post3 = postRepository.save(post3);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?sortBy=-id")
											.exchange();

			Post finalPost1 = post1;
			Post finalPost2 = post2;
			Post finalPost3 = post3;
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
										.extractingPath("$.results[*].id")
										.convertTo(LIST)
										.containsExactly(finalPost3.getId()
																   .intValue(),
												finalPost2.getId()
														  .intValue(),
												finalPost1.getId()
														  .intValue());
								});
		}

		@Test
		void getPosts_IsOk_WhenSortingByPublicationDateAsc()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post1 = postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Second Post");
			post2.setDescription("Description 2");
			post2.setBody("Body 2");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post2 = postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Third Post");
			post3.setDescription("Description 3");
			post3.setBody("Body 3");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post3 = postRepository.save(post3);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?sortBy=publishedAt")
											.exchange();

			Post finalPost1 = post1;
			Post finalPost2 = post2;
			Post finalPost3 = post3;
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
										.extractingPath("$.results[*].id")
										.convertTo(LIST)
										.containsExactly(finalPost1.getId()
																   .intValue(),
												finalPost2.getId()
														  .intValue(),
												finalPost3.getId()
														  .intValue());
								});
		}

		@Test
		void getPosts_IsOk_WhenSortingByPublicationDateDesc()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post1 = postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Second Post");
			post2.setDescription("Description 2");
			post2.setBody("Body 2");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post2 = postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Third Post");
			post3.setDescription("Description 3");
			post3.setBody("Body 3");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
			post3 = postRepository.save(post3);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?sortBy=-publishedAt")
											.exchange();

			Post finalPost1 = post1;
			Post finalPost2 = post2;
			Post finalPost3 = post3;
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
										.extractingPath("$.results[*].id")
										.convertTo(LIST)
										.containsExactly(finalPost3.getId()
																   .intValue(),
												finalPost2.getId()
														  .intValue(),
												finalPost1.getId()
														  .intValue());
								});
		}

		@Test
		void getPosts_Is400_WhenInvalidSortBy()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			post1 = postRepository.save(post1);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?sortBy=invalidSortBy")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		/**
		 * Test getPosts returns 200 OK with posts matching the search query.
		 */
		@Test
		void getPosts_IsOk_WithSearchQuery()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("Java Programming");
			post1.setDescription("Description");
			post1.setBody("Body");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Spring Framework");
			post2.setDescription("Description");
			post2.setBody("Body");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post2);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&q=Java")
											.exchange();

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
										.extractingPath("$.results[0].title")
										.isEqualTo("Java Programming");
									json.assertThat()
										.extractingPath("$.metadata.count")
										.isEqualTo(1);
								});
		}

		/**
		 * Test getPosts returns 200 OK with empty results when no posts match search query.
		 */
		@Test
		void getPosts_IsOk_WithNoMatchingSearchQuery()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&q=Python")
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
								});
		}

		/**
		 * Test getPosts returns 200 OK with multiple posts matching search query.
		 */
		@Test
		void getPosts_IsOk_WithMultipleMatchingSearchQuery()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post1 = new Post();
			post1.setTitle("Java Programming Basics");
			post1.setDescription("Description");
			post1.setBody("Body");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post1);

			Post post2 = new Post();
			post2.setTitle("Advanced Java Concepts");
			post2.setDescription("Description");
			post2.setBody("Body");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post2);

			Post post3 = new Post();
			post3.setTitle("Spring Framework");
			post3.setDescription("Description");
			post3.setBody("Body");
			post3.setAuthor(author);
			post3.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post3);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&q=Java")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.convertTo(LIST)
										.hasSize(2);
									json.assertThat()
										.extractingPath("$.metadata.count")
										.isEqualTo(2);
								});
		}

		/**
		 * Test getPosts performs case-insensitive search.
		 */
		@Test
		void getPosts_IsOk_WithCaseInsensitiveSearch()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&q=java")
											.exchange();

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
										.extractingPath("$.results[0].title")
										.isEqualTo("Java Programming");
								});
		}

		/**
		 * Test getPosts returns 200 OK with partial title matches.
		 */
		@Test
		void getPosts_IsOk_WithPartialTitleMatch()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?page=0&pageSize=10&q=Program")
											.exchange();

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
										.extractingPath("$.results[0].title")
										.isEqualTo("Java Programming Basics");
								});
		}

		@Test
		void getPosts_ReturnNone_WhenNoPublishedPostExist()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?")
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
								});
		}

		@Test
		void getPosts_ReturnNone_WhenIncludeUnpublishedIsFalse()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?unpublished=false")
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
								});
		}

		@Test
		@WithMockUser(username = "admin")
		void getPosts_ReturnPost_WhenIncludeUnpublishedIsTrue()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?unpublished=true")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.asInstanceOf(LIST)
										.hasSize(1);
								});
		}

		@Test
		@WithMockUser(username = "admin")
		void getPosts_ReturnMultiple_WhenIncludeUnpublishedIsTrue()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			postRepository.save(post);

			Post post2 = new Post();
			post2.setTitle("Spring Programming Basics");
			post2.setDescription("Description");
			post2.setBody("Body");
			post2.setAuthor(author);
			post2.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post2);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?unpublished=true")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.asInstanceOf(LIST)
										.hasSize(2);
								});
		}

		@Test
		void getPosts_ReturnNone_WhenUnauthenticatedAndIncludeUnpublishedIsTrue()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts?unpublished=true")
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
								});
		}
	}

	@Nested
	@DisplayName("deletePost")
	class DeletePost
	{
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

		@Test
		void deletePost_Is401_WhenUnauthenticated()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.delete()
										  .uri("/posts/" + post.getId())
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
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
	}

	@Nested
	@DisplayName("createPost")
	class CreatePost
	{
		@Test
		void createPost_Is201_WhenGivenValidTitle()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			CreatePostRequest requestBody = new CreatePostRequest("Post title");

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author,
												  Set.of(new SimpleGrantedAuthority("CREATE")))))
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(JsonUtils.asJsonString(requestBody))
										  .uri("/posts")
										  .exchange();
			User finalAuthor = author;
			assertThat(result).hasStatus(HttpStatus.CREATED)
							  .hasContentType(MediaType.APPLICATION_JSON)
							  .bodyJson()
							  .convertTo(PostDto.class)
							  .satisfies(dto ->
							  {
								  assertThat(dto.getTitle()).isEqualTo("Post title");
								  assertThat(dto.getAuthor()
												.id()).isEqualTo(finalAuthor.getId());
							  });
		}

		@Test
		void createPost_Is400_WhenGivenNoTitle()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author,
												  Set.of(new SimpleGrantedAuthority("CREATE")))))
										  .contentType(MediaType.APPLICATION_JSON)
										  .content("")
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPost_Is400_WhenGivenTitleTooLong()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);
			StringBuilder title = new StringBuilder();
			title.append("t".repeat(256));

			String requestBody = String.format("""
					{
						"title" : %s
					}
					""", title);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author,
												  Set.of(new SimpleGrantedAuthority("CREATE")))))
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(requestBody)
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPost_Is400_WhenGivenBlankTitle()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			String requestBody = """
					{
						"title" : "        "
					}
					""";

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author,
												  Set.of(new SimpleGrantedAuthority("CREATE")))))
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(requestBody)
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPost_Is400_WhenGivenNullTitle()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			String requestBody = """
					{
						"title" : null
					}
					""";

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author,
												  Set.of(new SimpleGrantedAuthority("CREATE")))))
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(requestBody)
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPost_Is401_WhenUnauthenticated()
		{
			CreatePostRequest requestBody = new CreatePostRequest("Post title");

			MvcTestResult result = mockMvc.post()
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(JsonUtils.asJsonString(requestBody))
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
		}

		@Test
		@WithMockUser(username = "user")
		void createPost_Is403_WhenMissingAuthorities()
		{
			CreatePostRequest requestBody = new CreatePostRequest("Post title");

			MvcTestResult result = mockMvc.post()
										  .contentType(MediaType.APPLICATION_JSON)
										  .content(JsonUtils.asJsonString(requestBody))
										  .uri("/posts")
										  .exchange();
			assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
		}
	}

	@Nested
	@DisplayName("updatePost")
	class UpdatePost
	{
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
			Tag tag3 = new Tag("Js", "javascript");
			tag1 = tagRepository.save(tag1);
			tag2 = tagRepository.save(tag2);
			tag3 = tagRepository.save(tag3);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			String requestBody = """
					{
						"title": "Updated Title",
						"tags": ["java", "2", 3]
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
										.hasSize(3);
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

	@Nested
	@DisplayName("getPostComments")
	class GetPostComments
	{
		/**
		 * Test getPostComments returns 200 OK with empty list when post has no comments.
		 */
		@Test
		void getPostComments_IsOk_WhenPostHasNoComments()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Post without comments");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/" + post.getId() + "/comments")
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
								});
		}

		/**
		 * Test getPostComments returns 200 OK with single comment when post has one comment.
		 */
		@Test
		void getPostComments_IsOk_WhenPostHasOneComment()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Post with one comment");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			Comment comment = new Comment();
			comment.setBody("Test comment body");
			comment.setUsername("Test User");
			comment.setCreatedAt(OffsetDateTime.now());
			post.addComment(comment);
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.asInstanceOf(LIST)
										.hasSize(1);
									json.assertThat()
										.extractingPath("$.metadata.count")
										.isEqualTo(1);
								});
		}

		/**
		 * Test getPostComments returns 200 OK with multiple comments when post has several comments.
		 */
		@Test
		void getPostComments_IsOk_WhenPostHasMultipleComments()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Post with multiple comments");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			Comment comment1 = new Comment();
			comment1.setBody("First comment");
			comment1.setUsername("User 1");
			comment1.setCreatedAt(OffsetDateTime.now());
			Comment comment2 = new Comment();
			comment2.setBody("Second comment");
			comment2.setUsername("User 2");
			comment2.setCreatedAt(OffsetDateTime.now());
			Comment comment3 = new Comment();
			comment3.setBody("Third comment");
			comment3.setUsername("User 3");
			comment3.setCreatedAt(OffsetDateTime.now());
			post.addComment(comment1);
			post.addComment(comment2);
			post.addComment(comment3);
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.results")
										.asInstanceOf(LIST)
										.hasSize(3);
									json.assertThat()
										.extractingPath("$.results[?(@.title == 'First comment')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results[?(@.title == 'Second comment')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results[?(@.title == 'Third comment')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results[?(@.username == 'User 1')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results[?(@.username == 'User 2')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results[?(@.username == 'User 3')]")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.results..id")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.metadata.count")
										.isEqualTo(3);
								});
		}

		/**
		 * Test getPostComments returns 404 NOT FOUND when post ID does not exist.
		 */
		@Test
		void getPostComments_Is404_WhenPostIdDoesNotExist()
		{
			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/999999/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test getPostComments returns 404 NOT FOUND when given negative post ID.
		 */
		@Test
		void getPostComments_Is404_WhenGivenNegativePostId()
		{
			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/-1/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test getPostComments returns 404 NOT FOUND when given zero as post ID.
		 */
		@Test
		void getPostComments_Is404_WhenGivenZeroAsPostId()
		{
			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/0/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test getPostComments returns 403 FORBIDDEN when accessing unpublished post by non-owner.
		 */
		@Test
		void getPostComments_Is403_WhenAccessingUnpublishedPostByNonOwner()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			User otherUser = new User("other@example.com", "Other User", "password456");
			otherUser = userRepository.save(otherUser);

			Post post = new Post();
			post.setTitle("Unpublished post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(otherUser)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
		}

		/**
		 * Test getPostComments returns 200 OK when accessing unpublished post by owner.
		 */
		@Test
		void getPostComments_IsOk_WhenAccessingUnpublishedPostByOwner()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Unpublished post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/posts/" + post.getId() + "/comments")
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
								});
		}
	}

	@Nested
	@DisplayName("createPostComment")
	class CreatePostComment
	{
		/**
		 * Test createPostComment returns 200 OK when creating comment on published post without authentication.
		 */
		@Test
		void createPostComment_IsOk_WhenCreatingCommentOnPublishedPost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.id")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.body")
										.isEqualTo("Test comment body");
									json.assertThat()
										.extractingPath("$.username")
										.isEqualTo("Test User");
									json.assertThat()
										.extractingPath("$.createdAt")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.postId")
										.isNotNull();
								});
		}

		/**
		 * Test createPostComment returns 200 OK when creating comment on unpublished post by owner.
		 */
		@Test
		void createPostComment_IsOk_WhenCreatingCommentOnUnpublishedPostByOwner()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Unpublished post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatusOk()
								.hasContentType(MediaType.APPLICATION_JSON)
								.bodyJson()
								.satisfies(json ->
								{
									json.assertThat()
										.extractingPath("$.id")
										.isNotNull();
									json.assertThat()
										.extractingPath("$.body")
										.isEqualTo("Test comment body");
									json.assertThat()
										.extractingPath("$.username")
										.isEqualTo("Test User");
								});
		}

		/**
		 * Test createPostComment returns 403 FORBIDDEN when non-owner tries to create comment on unpublished post.
		 */
		@Test
		void createPostComment_Is403_WhenNonOwnerTriesToCreateCommentOnUnpublishedPost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			User otherUser = new User("other@example.com", "Other User", "password456");
			otherUser = userRepository.save(otherUser);

			Post post = new Post();
			post.setTitle("Unpublished post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.with(user(new SecurityUser(otherUser)))
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
		}

		/**
		 * Test createPostComment returns 404 NOT FOUND when post ID does not exist.
		 */
		@Test
		void createPostComment_Is404_WhenPostIdDoesNotExist()
		{
			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/999999/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test createPostComment returns 404 NOT FOUND when given negative post ID.
		 */
		@Test
		void createPostComment_Is404_WhenGivenNegativePostId()
		{
			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/-1/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test createPostComment returns 404 NOT FOUND when given zero as post ID.
		 */
		@Test
		void createPostComment_Is404_WhenGivenZeroAsPostId()
		{
			String requestBody = """
					{
						"body": "Test comment body",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/0/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
		}

		/**
		 * Test createPostComment returns 400 BAD REQUEST when username exceeds max length.
		 */
		@Test
		void createPostComment_Is400_WhenUsernameExceedsMaxLength()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body",
						"username": "%s"
					}
					""".formatted(StringUtils.repeat("a", 256));

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPostComment_Is400_WhenUsernameIsEmpty()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body",
						"username": ""
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void createPostComment_Is400_WhenUsernameIsMissing()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "Test comment body"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		/**
		 * Test createPostComment returns 400 BAD REQUEST when body is empty.
		 */
		@Test
		void createPostComment_Is400_WhenBodyIsEmpty()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"body": "",
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		/**
		 * Test createPostComment returns 400 BAD REQUEST when body is missing.
		 */
		@Test
		void createPostComment_Is400_WhenBodyIsMissing()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Published post");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			String requestBody = """
					{
						"username": "Test User"
					}
					""";

			MvcTestResult response = mockMvc.post()
											.contentType(MediaType.APPLICATION_JSON)
											.content(requestBody)
											.uri("/posts/" + post.getId() + "/comments")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}
	}

	@Nested
	@DisplayName("publishPost")
	class PublishPost
	{
		@Test
		void publishPost_Is204_WhenAuthorPublishPost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(null);
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author)))
										  .uri("/posts/" + post.getId() + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

			MvcTestResult getResult = mockMvc.get()
											 .with(user(new SecurityUser(author)))
											 .uri("/posts/" + post.getId())
											 .exchange();

			assertThat(getResult).hasStatusOk()
								 .hasContentType(MediaType.APPLICATION_JSON)
								 .bodyJson()
								 .convertTo(PostDto.class)
								 .satisfies(p ->
								 {
									 assertThat(p.getPublishedAt()).isNotNull();
								 });
		}

		@Test
		@WithMockUser(username = "admin", authorities = "UPDATE")
		void publishPost_Is204_WhenAdminPublishPost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(null);
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

			MvcTestResult getResult = mockMvc.get()
											 .uri("/posts/" + post.getId())
											 .exchange();

			assertThat(getResult).hasStatusOk()
								 .hasContentType(MediaType.APPLICATION_JSON)
								 .bodyJson()
								 .convertTo(PostDto.class)
								 .satisfies(p ->
								 {
									 assertThat(p.getPublishedAt()).isNotNull();
								 });
		}

		@Test
		@WithMockUser(username = "admin", authorities = "UPDATE")
		void publishPost_Is404_WhenPostDoesNotExists()
		{
			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + 9999L + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
		}

		@Test
		@WithMockUser(username = "user")
		void publishPost_Is403_WhenUserDoesNotHavePermissions()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(null);
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
		}

		@Test
		void publishPost_Is401_WhenUnauthenticated()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(null);
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void publishPost_Is409_WhenPostIsAlreadyPublished()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author)))
										  .uri("/posts/" + post.getId() + "/publish")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.CONFLICT);
		}
	}

	@Nested
	@DisplayName("hidePost")
	class HidePost
	{
		@Test
		void hidePost_Is204_WhenAuthorHidePost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author)))
										  .uri("/posts/" + post.getId() + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

			MvcTestResult getResult = mockMvc.get()
											 .with(user(new SecurityUser(author)))
											 .uri("/posts/" + post.getId())
											 .exchange();

			assertThat(getResult).hasStatusOk()
								 .hasContentType(MediaType.APPLICATION_JSON)
								 .bodyJson()
								 .convertTo(PostDto.class)
								 .satisfies(p ->
								 {
									 assertThat(p.getPublishedAt()).isNull();
								 });
		}

		@Test
		@WithMockUser(username = "admin", authorities = "UPDATE")
		void hidePost_Is204_WhenAdminPublishPost()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NO_CONTENT);

			MvcTestResult getResult = mockMvc.get()
											 .uri("/posts/" + post.getId())
											 .exchange();

			assertThat(getResult).hasStatusOk()
								 .hasContentType(MediaType.APPLICATION_JSON)
								 .bodyJson()
								 .convertTo(PostDto.class)
								 .satisfies(p ->
								 {
									 assertThat(p.getPublishedAt()).isNull();
								 });
		}

		@Test
		@WithMockUser(username = "admin", authorities = "UPDATE")
		void hidePost_Is404_WhenPostDoesNotExists()
		{
			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + 9999L + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
		}

		@Test
		@WithMockUser(username = "user")
		void hidePost_Is403_WhenUserDoesNotHavePermissions()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
		}

		@Test
		void hidePost_Is401_WhenUnauthenticated()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .uri("/posts/" + post.getId() + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void hidePost_Is409_WhenPostIsAlreadyHidden()
		{
			User author = new User("author@example.com", "Author Name", "password123");
			author = userRepository.save(author);

			Post post = new Post();
			post.setTitle("Original Title");
			post.setDescription("Original Description");
			post.setBody("Original Body");
			post.setAuthor(author);
			post.setPublishedAt(null);
			post = postRepository.save(post);

			MvcTestResult result = mockMvc.post()
										  .with(user(new SecurityUser(author)))
										  .uri("/posts/" + post.getId() + "/hide")
										  .exchange();

			assertThat(result).hasStatus(HttpStatus.CONFLICT);
		}
	}
}
