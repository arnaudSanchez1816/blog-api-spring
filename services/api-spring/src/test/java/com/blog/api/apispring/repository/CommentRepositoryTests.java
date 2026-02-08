package com.blog.api.apispring.repository;

import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.CommentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(ClearDatabaseExtension.class)
class CommentRepositoryTests
{
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CommentRepository commentRepository;

	private User author;
	private Post post;
	private Comment comment1;
	private Comment comment2;

	@BeforeEach
	void setUp()
	{
		author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		comment1 = new Comment();
		comment1.setBody("Comment body");
		comment1.setUsername("Username");
		comment1.setCreatedAt(OffsetDateTime.now());

		comment2 = new Comment();
		comment2.setBody("Comment body 2");
		comment2.setUsername("Username 2");
		comment2.setCreatedAt(OffsetDateTime.now());

		post = new Post();
		post.setTitle("First Post");
		post.setDescription("First post description");
		post.setBody("First post body content");
		post.setReadingTime(5);
		post.setPublishedAt(OffsetDateTime.now());
		post.setAuthor(author);
		post.addComment(comment1);
		post.addComment(comment2);
		post = postRepository.save(post);
	}

	@Test
	void findCommentInfoById_ReturnsCommentInfo_WhenCommentExists()
	{
		Optional<CommentInfo> result = commentRepository.findCommentInfoById(comment1.getId());

		assertThat(result).isPresent();
		CommentInfo commentInfo = result.get();
		assertThat(commentInfo.getId()).isEqualTo(comment1.getId());
		assertThat(commentInfo.getBody()).isEqualTo("Comment body");
		assertThat(commentInfo.getUsername()).isEqualTo("Username");
		assertThat(commentInfo.getCreatedAt()).isNotNull();
		assertThat(commentInfo.getPostId()).isEqualTo(post.getId());
	}

	@Test
	void findCommentInfoById_ReturnsEmpty_WhenCommentDoesNotExists()
	{
		Optional<CommentInfo> result = commentRepository.findCommentInfoById(123456789L);

		assertThat(result).isEmpty();
	}
}