package com.blog.api.apispring.controller;

import com.blog.api.apispring.dto.posts.GetPostsRequestImpl;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.TagRepository;
import com.blog.api.apispring.repository.UserRepository;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.json.JsonContent;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(ClearDatabaseExtension.class)
public class UserControllerTests
{
	@Autowired
	private MockMvcTester mockMvc;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	@Test
	void getUser_IsOk_WhenUserIsAuthenticated()
	{
		User user = new User("admin@blog.com", "admin", "password123");
		user = userRepository.save(user);

		MvcTestResult response = mockMvc.get()
										.with(user(new SecurityUser(user)))
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.returns(1, fromPath("$.id"))
							.returns("admin", fromPath("$.name"))
							.returns("admin@blog.com", fromPath("$.email"));
	}

	@Test
	void getUser_Is401_WhenNoUserAuthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.uri("/users/me")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Nested
	@DisplayName("getUserPosts")
	class GetUserPosts
	{
		private User author;

		@BeforeEach
		void setUp()
		{
			this.author = new User("author@example.com", "Author Name", "password123");
			this.author = userRepository.save(this.author);
		}

		@Test
		void getUserPosts_IsOk_WhenNoPostsExist()
		{
			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10")
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

		@Test
		void getUserPosts_IsOk_WhenOnePostExists()
		{
			Post post = new Post();
			post.setTitle("Test Post");
			post.setDescription("Test description");
			post.setBody("Test body");
			post.setReadingTime(5);
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10")
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

		@Test
		void getUserPosts_IsOk_WithMultiplePosts()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10")
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

		@Test
		void getUserPosts_IsOk_WithCorrectMetadata()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&tags=java&sortBy=id")
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

		@Test
		void getUserPosts_IsOk_WithPagination()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=1&pageSize=10")
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
		void getUserPosts_IsOk_WithInvalidPage()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=-50&pageSize=10")
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

		@Test
		void getUserPosts_IsOk_WithTags()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?tags=java,spring")
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
		void getUserPosts_Is400_WithGivenInvalidTags()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?tags=@alsdsqkdKL")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void getUserPosts_IsOk_WithCommentsCount()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10")
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

		@Test
		void getUserPosts_IsOk_WithCustomPageSize()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=3")
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
		void getUserPosts_IsOk_WithTooSmallCustomPageSize()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=-30")
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
		void getUserPosts_IsOk_WithTooBigCustomPageSize()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=999")
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
		void getUserPosts_IsOk_WhenSortingByIdAsc()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?sortBy=id")
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
		void getUserPosts_IsOk_WhenSortingByIdDesc()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?sortBy=-id")
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
		void getUserPosts_IsOk_WhenSortingByPublicationDateAsc()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?sortBy=publishedAt")
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
		void getUserPosts_IsOk_WhenSortingByPublicationDateDesc()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?sortBy=-publishedAt")
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
		void getUserPosts_Is400_WhenInvalidSortBy()
		{
			Post post1 = new Post();
			post1.setTitle("First Post");
			post1.setDescription("Description 1");
			post1.setBody("Body 1");
			post1.setAuthor(author);
			post1.setPublishedAt(OffsetDateTime.now());
			post1 = postRepository.save(post1);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?sortBy=invalidSortBy")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
		}

		@Test
		void getUserPosts_IsOk_WithSearchQuery()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&q=Java")
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

		@Test
		void getUserPosts_IsOk_WithNoMatchingSearchQuery()
		{
			Post post = new Post();
			post.setTitle("Java Programming");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&q=Python")
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

		@Test
		void getUserPosts_IsOk_WithMultipleMatchingSearchQuery()
		{
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
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&q=Java")
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

		@Test
		void getUserPosts_IsOk_WithCaseInsensitiveSearch()
		{
			Post post = new Post();
			post.setTitle("Java Programming");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&q=java")
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

		@Test
		void getUserPosts_IsOk_WithPartialTitleMatch()
		{
			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts?page=0&pageSize=10&q=Program")
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
		void getUserPosts_ReturnAll_WhenBothPublishedAndUnpublishedExist()
		{
			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			post = postRepository.save(post);

			Post draft = new Post();
			draft.setTitle("Draft");
			draft.setDescription("Description");
			draft.setBody("Body");
			draft.setAuthor(author);
			draft = postRepository.save(draft);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts")
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
		void getUserPosts_ReturnOwnPosts_WhenMultipleAuthorsExist()
		{
			User otherAuthor = new User("otherAuthor@email.com", "other author", "password");
			otherAuthor = userRepository.save(otherAuthor);

			Post post = new Post();
			post.setTitle("Java Programming Basics");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post = postRepository.save(post);

			Post post2 = new Post();
			post2.setTitle("Spring Programming Basics");
			post2.setDescription("Description");
			post2.setBody("Body");
			post2.setAuthor(otherAuthor);
			post2.setPublishedAt(OffsetDateTime.now());
			post2 = postRepository.save(post2);

			MvcTestResult response = mockMvc.get()
											.with(user(new SecurityUser(author)))
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts")
											.exchange();

			Post finalPost = post;
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
										.extractingPath("$.results[0].id")
										.isEqualTo(finalPost.getId()
															.intValue());
								});
		}

		@Test
		void getUserPosts_Is401_WhenUnauthenticated()
		{
			Post post = new Post();
			post.setTitle("Java Programming");
			post.setDescription("Description");
			post.setBody("Body");
			post.setAuthor(author);
			post.setPublishedAt(OffsetDateTime.now());
			postRepository.save(post);

			MvcTestResult response = mockMvc.get()
											.contentType(MediaType.APPLICATION_JSON)
											.uri("/users/me/posts")
											.exchange();

			assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
		}
	}

	private static Function<JsonContent, Object> fromPath(String path)
	{
		return jsonContent -> assertThat(jsonContent).extractingPath(path)
													 .actual();
	}
}
