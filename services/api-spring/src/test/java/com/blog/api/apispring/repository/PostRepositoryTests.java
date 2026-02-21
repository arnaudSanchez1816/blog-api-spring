package com.blog.api.apispring.repository;

import com.blog.api.apispring.PostgresTestConfig;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndComments;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.projection.PostInfoWithAuthorTagsComments;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Import(PostgresTestConfig.class)
@ExtendWith(ClearDatabaseExtension.class)
class PostRepositoryTests
{
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	private User author;
	private Post post1;
	private Post post2;
	private Tag tag1;
	private Tag tag2;

	@BeforeEach
	void setUp()
	{
		author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		tag1 = new Tag("Java", "java");
		tag1 = tagRepository.save(tag1);

		tag2 = new Tag("Spring", "spring");
		tag2 = tagRepository.save(tag2);

		post1 = new Post();
		post1.setTitle("First Post");
		post1.setDescription("First post description");
		post1.setBody("First post body content");
		post1.setReadingTime(5);
		post1.setPublishedAt(OffsetDateTime.now());
		post1.setAuthor(author);
		Comment comment = new Comment();
		comment.setBody("Comment body");
		comment.setUsername("Username");
		comment.setCreatedAt(OffsetDateTime.now());
		post1.addComment(comment);
		post1.addTag(tag1);
		post1.addTag(tag2);
		post1 = postRepository.save(post1);

		post2 = new Post();
		post2.setTitle("Second Post");
		post2.setDescription("Second post description");
		post2.setBody("Second post body content");
		post2.setReadingTime(10);
		post2.setPublishedAt(OffsetDateTime.now());
		post2.setAuthor(author);
		post2 = postRepository.save(post2);
	}

	@Test
	void findInfoWithAuthorById_ReturnsPostInfoWithAuthor_WhenPostExists()
	{
		Optional<PostInfoWithAuthor> result = postRepository.findInfoWithAuthorById(post1.getId());

		assertThat(result).isPresent();
		PostInfoWithAuthor postInfo = result.get();
		assertThat(postInfo.getId()).isEqualTo(post1.getId());
		assertThat(postInfo.getTitle()).isEqualTo("First Post");
		assertThat(postInfo.getDescription()).isEqualTo("First post description");
		assertThat(postInfo.getBody()).isEqualTo("First post body content");
		assertThat(postInfo.getReadingTime()).isEqualTo(5);
		assertThat(postInfo.getPublishedAt()).isNotNull();
		assertThat(postInfo.getAuthor()).isNotNull();
		assertThat(postInfo.getAuthor()
						   .getId()).isEqualTo(author.getId());
		assertThat(postInfo.getAuthor()
						   .getName()).isEqualTo("Test Author");
	}

	@Test
	void findInfoWithAuthorById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthor> result = postRepository.findInfoWithAuthorById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void save_PersistsPost_WithAllFields()
	{
		Post newPost = new Post();
		newPost.setTitle("New Post");
		newPost.setDescription("New description");
		newPost.setBody("New body content");
		newPost.setReadingTime(8);
		newPost.setPublishedAt(OffsetDateTime.now());
		newPost.setAuthor(author);

		Post savedPost = postRepository.save(newPost);

		assertThat(savedPost.getId()).isNotNull();
		assertThat(savedPost.getTitle()).isEqualTo("New Post");
		assertThat(savedPost.getDescription()).isEqualTo("New description");
		assertThat(savedPost.getBody()).isEqualTo("New body content");
		assertThat(savedPost.getReadingTime()).isEqualTo(8);
		assertThat(savedPost.getPublishedAt()).isNotNull();
		assertThat(savedPost.getAuthor()
							.getId()).isEqualTo(author.getId());
	}

	@Test
	void delete_RemovesPost_WhenPostExists()
	{
		postRepository.delete(post1);

		Optional<PostInfoWithAuthor> result = postRepository.findInfoWithAuthorById(post1.getId());
		assertThat(result).isEmpty();
	}

	@Test
	void findAll_ReturnsAllPosts()
	{
		Iterable<Post> posts = postRepository.findAll();

		assertThat(posts).hasSize(2);
	}

	@Test
	void count_ReturnsCorrectCount()
	{
		long count = postRepository.count();

		assertThat(count).isEqualTo(2);
	}

	@Test
	void existsById_ReturnsTrue_WhenPostExists()
	{
		boolean exists = postRepository.existsById(post1.getId());

		assertThat(exists).isTrue();
	}

	@Test
	void existsById_ReturnsFalse_WhenPostDoesNotExist()
	{
		boolean exists = postRepository.existsById(99999L);

		assertThat(exists).isFalse();
	}

	@Test
	void findWithCommentsById_ReturnsPostWithComments_WhenPostExists()
	{
		Optional<Post> result = postRepository.findWithCommentsById(post1.getId());

		assertThat(result).isPresent();
		Post post = result.get();
		assertThat(post.getId()).isEqualTo(post1.getId());
		assertThat(post.getTitle()).isEqualTo("First Post");
		assertThat(post.getComments()).hasSize(1);
		Comment comment = post.getComments()
							  .iterator()
							  .next();
		assertThat(comment.getBody()).isEqualTo("Comment body");
		assertThat(comment.getUsername()).isEqualTo("Username");
	}

	@Test
	void findWithCommentsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<Post> result = postRepository.findWithCommentsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findInfoWithCommentsById_ReturnsPostWithComments_WhenPostExists()
	{
		Optional<PostInfoWithAuthorAndComments> result = postRepository.findInfoWithCommentsById(post1.getId());

		assertThat(result).isPresent();
		PostInfoWithAuthorAndComments postInfo = result.get();
		assertThat(postInfo.getId()).isEqualTo(post1.getId());
		assertThat(postInfo.getTitle()).isEqualTo("First Post");
		assertThat(postInfo.getAuthor()).isNotNull();
		assertThat(postInfo.getComments()).hasSize(1);
		Comment comment = postInfo.getComments()
								  .iterator()
								  .next();
		assertThat(comment.getBody()).isEqualTo("Comment body");
		assertThat(comment.getUsername()).isEqualTo("Username");
	}

	@Test
	void findInfoWithCommentsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorAndComments> result = postRepository.findInfoWithCommentsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findInfoWithTagsById_ReturnsPostWithTags_WhenPostExists()
	{
		Optional<PostInfoWithAuthorAndTags> result = postRepository.findInfoWithTagsById(post1.getId());

		assertThat(result).isPresent();
		PostInfoWithAuthorAndTags postInfo = result.get();
		assertThat(postInfo.getId()).isEqualTo(post1.getId());
		assertThat(postInfo.getTitle()).isEqualTo("First Post");
		assertThat(postInfo.getAuthor()).isNotNull();
		assertThat(postInfo.getTags()).hasSize(2);
		assertThat(postInfo.getTags()).extracting(Tag::getName)
									  .containsExactlyInAnyOrder("Java", "Spring");
	}

	@Test
	void findInfoWithTagsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorAndTags> result = postRepository.findInfoWithTagsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findInfoWithTagsAndCommentsById_ReturnsPostWithTagsAndComments_WhenPostExists()
	{
		Optional<PostInfoWithAuthorTagsComments> result = postRepository.findInfoWithTagsAndCommentsById(post1.getId());

		assertThat(result).isPresent();
		PostInfoWithAuthorTagsComments postInfo = result.get();
		assertThat(postInfo.getId()).isEqualTo(post1.getId());
		assertThat(postInfo.getTitle()).isEqualTo("First Post");
		assertThat(postInfo.getAuthor()).isNotNull();
		assertThat(postInfo.getTags()).hasSize(2);
		assertThat(postInfo.getTags()).extracting(Tag::getName)
									  .containsExactlyInAnyOrder("Java", "Spring");
		assertThat(postInfo.getComments()).hasSize(1);
	}

	@Test
	void findInfoWithTagsAndCommentsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorTagsComments> result = postRepository.findInfoWithTagsAndCommentsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findAllInfoWithTags_ReturnsPagedPostsWithAuthorAndTags()
	{
		Pageable pageable = PageRequest.of(0,
				10,
				Sort.by("id")
					.ascending());

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllInfoWithTags(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getContent()).hasSize(2);
		PostInfoWithAuthorAndTags firstPost = result.getContent()
													.getFirst();
		assertThat(firstPost.getId()).isNotNull();
		assertThat(firstPost.getId()).isEqualTo(post1.getId());
		assertThat(firstPost.getTitle()).isNotNull();
		assertThat(firstPost.getAuthor()).isNotNull();
		assertThat(firstPost.getAuthor()
							.getName()).isEqualTo("Test Author");
		assertThat(firstPost.getTags()).hasSize(2);
		assertThat(firstPost.getTags()).extracting(Tag::getName)
									   .containsExactlyInAnyOrder("Java", "Spring");
	}

	@Test
	void findAllInfoWithTags_ReturnsEmptyPage_WhenNoPostsExist()
	{
		postRepository.deleteAll();
		Pageable pageable = PageRequest.of(0, 10);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllInfoWithTags(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(0);
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void findAllInfoWithTags_ReturnsPaginatedResults_WithMultiplePages()
	{
		Pageable firstPage = PageRequest.of(0, 1);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllInfoWithTags(firstPage);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.hasNext()).isTrue();
	}

	@Test
	void findAllInfoWithTags_ReturnsCorrectPageSize()
	{
		Pageable pageable = PageRequest.of(0, 1);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllInfoWithTags(pageable);

		assertThat(result.getSize()).isEqualTo(1);
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void countCommentsById_ReturnCorrectValue()
	{
		long commentsCount = postRepository.countCommentsById(post1.getId());
		assertThat(commentsCount).isEqualTo(1L);

		commentsCount = postRepository.countCommentsById(post2.getId());
		assertThat(commentsCount).isEqualTo(0L);
	}

	@Test
	void countCommentsById_ReturnZeroNoPostExists()
	{
		long commentsCount = postRepository.countCommentsById(99999L);
		assertThat(commentsCount).isEqualTo(0);
	}

	@Test
	void countCommentsByIds_ReturnCorrectNumberOfComments()
	{
		Map<Long, Long> comments = postRepository.countCommentsByIds(Arrays.asList(post1.getId(), post2.getId()));
		assertThat(comments.size()).isEqualTo(2);
		assertThat(comments.containsKey(post1.getId())).isTrue();
		assertThat(comments.get(post1.getId())).isEqualTo(1);
		assertThat(comments.containsKey(post2.getId())).isTrue();
		assertThat(comments.get(post2.getId())).isEqualTo(0);
	}

	@Test
	void countCommentsByIds_OnlyReturnCountForListedPost()
	{
		Map<Long, Long> comments = postRepository.countCommentsByIds(Collections.singletonList(post1.getId()));
		assertThat(comments.size()).isEqualTo(1);
		assertThat(comments.containsKey(post1.getId())).isTrue();
		assertThat(comments.get(post1.getId())).isEqualTo(1);
	}

	@Test
	void countCommentsByIds_ReturnEmptyWhenGivenEmptyList()
	{
		Map<Long, Long> comments = postRepository.countCommentsByIds(Collections.emptyList());
		assertThat(comments.size()).isEqualTo(0);
	}

	@Test
	void deleteById_RemovesPostFromDatabase_WhenPostExists()
	{
		long postId = post1.getId();

		postRepository.deleteById(postId);

		Optional<PostInfoWithAuthor> result = postRepository.findInfoWithAuthorById(postId);
		assertThat(result).isEmpty();
		assertThat(postRepository.count()).isEqualTo(1);
	}
}
