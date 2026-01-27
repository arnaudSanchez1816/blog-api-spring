package com.blog.api.apispring.repository;

import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndComments;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.projection.PostInfoWithAuthorTagsComments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
	void findById_ReturnsPostInfoWithAuthor_WhenPostExists()
	{
		Optional<PostInfoWithAuthor> result = postRepository.findWithAuthorById(post1.getId());

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
	void findById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthor> result = postRepository.findWithAuthorById(99999L);

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

		Optional<PostInfoWithAuthor> result = postRepository.findWithAuthorById(post1.getId());
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
		Optional<PostInfoWithAuthorAndComments> result = postRepository.findWithCommentsById(post1.getId());

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
	void findWithCommentsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorAndComments> result = postRepository.findWithCommentsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findWithTagsById_ReturnsPostWithTags_WhenPostExists()
	{
		Optional<PostInfoWithAuthorAndTags> result = postRepository.findWithTagsById(post1.getId());

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
	void findWithTagsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorAndTags> result = postRepository.findWithTagsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findWithTagsAndCommentsById_ReturnsPostWithTagsAndComments_WhenPostExists()
	{
		Optional<PostInfoWithAuthorTagsComments> result = postRepository.findWithTagsAndCommentsById(post1.getId());

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
	void findWithTagsAndCommentsById_ReturnsEmpty_WhenPostDoesNotExist()
	{
		Optional<PostInfoWithAuthorTagsComments> result = postRepository.findWithTagsAndCommentsById(99999L);

		assertThat(result).isEmpty();
	}

	@Test
	void findAllWithTags_ReturnsPagedPostsWithAuthorAndTags()
	{
		Pageable pageable = PageRequest.of(0, 10);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllWithTags(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getContent()).hasSize(2);
		PostInfoWithAuthorAndTags firstPost = result.getContent()
													.get(0);
		assertThat(firstPost.getId()).isNotNull();
		assertThat(firstPost.getTitle()).isNotNull();
		assertThat(firstPost.getAuthor()).isNotNull();
		assertThat(firstPost.getAuthor()
							.getName()).isEqualTo("Test Author");
	}

	@Test
	void findAllWithTags_ReturnsEmptyPage_WhenNoPostsExist()
	{
		postRepository.deleteAll();
		Pageable pageable = PageRequest.of(0, 10);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllWithTags(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(0);
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void findAllWithTags_ReturnsPaginatedResults_WithMultiplePages()
	{
		Pageable firstPage = PageRequest.of(0, 1);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllWithTags(firstPage);

		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.hasNext()).isTrue();
	}

	@Test
	void findAllWithTags_ReturnsCorrectPageSize()
	{
		Pageable pageable = PageRequest.of(0, 1);

		Page<PostInfoWithAuthorAndTags> result = postRepository.findAllWithTags(pageable);

		assertThat(result.getSize()).isEqualTo(1);
		assertThat(result.getContent()).hasSize(1);
	}
}
