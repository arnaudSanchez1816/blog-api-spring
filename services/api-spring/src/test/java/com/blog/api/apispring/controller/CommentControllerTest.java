package com.blog.api.apispring.controller;

import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.CommentInfo;
import com.blog.api.apispring.repository.CommentRepository;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(ClearDatabaseExtension.class)
class CommentControllerTest
{
	@Autowired
	private MockMvcTester mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentRepository commentRepository;

	private Post post;

	@BeforeEach
	void setUp()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		post = new Post();
		post.setTitle("First Post");
		post.setDescription("First post description");
		post.setBody("First post body content");
		post.setReadingTime(5);
		post.setPublishedAt(OffsetDateTime.now());
		post.setAuthor(author);
		post = postRepository.save(post);
	}

	@Test
	@WithMockUser(username = "Admin", roles = "ADMIN")
	void getComment_IsOk_WhenGivenValidCommentId()
	{
		OffsetDateTime now = OffsetDateTime.now();
		Comment comment = new Comment();
		comment.setBody("Body");
		comment.setUsername("Username");
		comment.setCreatedAt(now);
		comment.setPost(post);
		comment = commentRepository.save(comment);

		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/comments/" + comment.getId())
										.exchange();
		Comment finalComment = comment;
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.satisfies(json ->
							{
								json.assertThat()
									.extractingPath("$.id")
									.convertTo(Long.class)
									.isEqualTo(finalComment.getId());
								json.assertThat()
									.extractingPath("$.body")
									.isEqualTo("Body");
								json.assertThat()
									.extractingPath("$.username")
									.isEqualTo("Username");
								json.assertThat()
									.extractingPath("$.createdAt")
									.isNotNull();
								json.assertThat()
									.extractingPath("$.postId")
									.convertTo(Long.class)
									.isEqualTo(post.getId());
							});
	}

	@Test
	@WithMockUser(username = "Admin", roles = "ADMIN")
	void getComment_Is404_WhenWrongCommentId()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/comments/" + 9999L)
										.exchange();
		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void getComment_Is401_WhenUnauthenticated()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/comments/" + 1L)
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}
}