package com.blog.api.apispring.specs;

import com.blog.api.apispring.PostgresTestConfig;
import com.blog.api.apispring.dto.tag.TagIdOrSlug;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Post;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.TagRepository;
import com.blog.api.apispring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link PostSpecs}.
 * Tests the specification methods used for filtering posts in JPA queries.
 */
@SpringBootTest
@Import(PostgresTestConfig.class)
@WebAppConfiguration
@ExtendWith(ClearDatabaseExtension.class)
class PostSpecsTests
{
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	private Tag tag1;
	private Tag tag2;

	@BeforeEach
	void setUp()
	{
		tag1 = new Tag("Tag1", "tag-1");
		tag1 = tagRepository.save(tag1);

		tag2 = new Tag("Tag2", "tag-2");
		tag2 = tagRepository.save(tag2);
	}

	@Test
	void onlyPublished_ReturnsPublishedPosts_WhenTrue()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post publishedPost = new Post();
		publishedPost.setTitle("Published Post");
		publishedPost.setDescription("Published post description");
		publishedPost.setBody("Published post body");
		publishedPost.setReadingTime(5);
		publishedPost.setPublishedAt(OffsetDateTime.now());
		publishedPost.setAuthor(author);
		publishedPost = postRepository.save(publishedPost);

		Post draftPost = new Post();
		draftPost.setTitle("Draft Post");
		draftPost.setDescription("Draft post description");
		draftPost.setBody("Draft post body");
		draftPost.setReadingTime(3);
		draftPost.setPublishedAt(null);
		draftPost.setAuthor(author);
		draftPost = postRepository.save(draftPost);

		List<Post> publishedPosts = postRepository.findAll(PostSpecs.onlyPublished(true));

		assertThat(publishedPosts).hasSize(1);
		assertThat(publishedPosts.get(0)
								 .getId()).isEqualTo(publishedPost.getId());
		assertThat(publishedPosts.get(0)
								 .getPublishedAt()).isNotNull();
	}

	@Test
	void onlyPublished_ReturnsAllPosts_WhenFalse()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post publishedPost = new Post();
		publishedPost.setTitle("Published Post");
		publishedPost.setDescription("Published post description");
		publishedPost.setBody("Published post body");
		publishedPost.setReadingTime(5);
		publishedPost.setPublishedAt(OffsetDateTime.now());
		publishedPost.setAuthor(author);
		publishedPost = postRepository.save(publishedPost);

		Post draftPost = new Post();
		draftPost.setTitle("Draft Post");
		draftPost.setDescription("Draft post description");
		draftPost.setBody("Draft post body");
		draftPost.setReadingTime(3);
		draftPost.setPublishedAt(null);
		draftPost.setAuthor(author);
		draftPost = postRepository.save(draftPost);

		List<Post> draftPosts = postRepository.findAll(PostSpecs.onlyPublished(false));

		assertThat(draftPosts).hasSize(2);
		assertThat(draftPosts).extracting(Post::getId)
							  .containsExactlyInAnyOrder(publishedPost.getId(), draftPost.getId());
	}

	@Test
	void onlyPublished_ReturnsEmptyList_WhenNoPublishedPostsExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post draftPost = new Post();
		draftPost.setTitle("Draft Post");
		draftPost.setDescription("Draft post description");
		draftPost.setBody("Draft post body");
		draftPost.setReadingTime(3);
		draftPost.setPublishedAt(null);
		draftPost.setAuthor(author);
		draftPost = postRepository.save(draftPost);

		List<Post> publishedPosts = postRepository.findAll(PostSpecs.onlyPublished(true));

		assertThat(publishedPosts).isEmpty();
	}

	@Test
	void onlyPublished_ReturnsOnlyPublished_WhenNoDraftPostsExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post publishedPost = new Post();
		publishedPost.setTitle("Published Post");
		publishedPost.setDescription("Published post description");
		publishedPost.setBody("Published post body");
		publishedPost.setReadingTime(5);
		publishedPost.setPublishedAt(OffsetDateTime.now());
		publishedPost.setAuthor(author);
		publishedPost = postRepository.save(publishedPost);

		List<Post> allPosts = postRepository.findAll(PostSpecs.onlyPublished(false));

		assertThat(allPosts).hasSize(1);
		assertThat(allPosts.getFirst()
						   .getId()).isEqualTo(publishedPost.getId());
	}

	@Test
	void onlyPublished_ReturnsMultiplePublishedPosts_WhenMultipleExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post publishedPost1 = new Post();
		publishedPost1.setTitle("Published Post 1");
		publishedPost1.setDescription("Published post 1 description");
		publishedPost1.setBody("Published post 1 body");
		publishedPost1.setReadingTime(5);
		publishedPost1.setPublishedAt(OffsetDateTime.now());
		publishedPost1.setAuthor(author);
		publishedPost1 = postRepository.save(publishedPost1);

		Post publishedPost2 = new Post();
		publishedPost2.setTitle("Published Post 2");
		publishedPost2.setDescription("Published post 2 description");
		publishedPost2.setBody("Published post 2 body");
		publishedPost2.setReadingTime(7);
		publishedPost2.setPublishedAt(OffsetDateTime.now());
		publishedPost2.setAuthor(author);
		publishedPost2 = postRepository.save(publishedPost2);

		Post draftPost = new Post();
		draftPost.setTitle("Draft Post");
		draftPost.setDescription("Draft post description");
		draftPost.setBody("Draft post body");
		draftPost.setReadingTime(3);
		draftPost.setPublishedAt(null);
		draftPost.setAuthor(author);
		draftPost = postRepository.save(draftPost);

		List<Post> publishedPosts = postRepository.findAll(PostSpecs.onlyPublished(true));

		assertThat(publishedPosts).hasSize(2);
		assertThat(publishedPosts).extracting(Post::getId)
								  .containsExactlyInAnyOrder(publishedPost1.getId(), publishedPost2.getId());
		assertThat(publishedPosts).allMatch(post -> post.getPublishedAt() != null);
	}

	@Test
	void onlyPublished_ReturnsMultipleDraftPosts_WhenMultipleExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post draftPost1 = new Post();
		draftPost1.setTitle("Draft Post 1");
		draftPost1.setDescription("Draft post 1 description");
		draftPost1.setBody("Draft post 1 body");
		draftPost1.setReadingTime(3);
		draftPost1.setPublishedAt(null);
		draftPost1.setAuthor(author);
		draftPost1 = postRepository.save(draftPost1);

		Post draftPost2 = new Post();
		draftPost2.setTitle("Draft Post 2");
		draftPost2.setDescription("Draft post 2 description");
		draftPost2.setBody("Draft post 2 body");
		draftPost2.setReadingTime(4);
		draftPost2.setPublishedAt(null);
		draftPost2.setAuthor(author);
		draftPost2 = postRepository.save(draftPost2);

		Post publishedPost = new Post();
		publishedPost.setTitle("Published Post");
		publishedPost.setDescription("Published post description");
		publishedPost.setBody("Published post body");
		publishedPost.setReadingTime(5);
		publishedPost.setPublishedAt(OffsetDateTime.now());
		publishedPost.setAuthor(author);
		publishedPost = postRepository.save(publishedPost);

		List<Post> allPosts = postRepository.findAll(PostSpecs.onlyPublished(false));

		assertThat(allPosts).hasSize(3);
		assertThat(allPosts).extracting(Post::getId)
							.containsExactlyInAnyOrder(draftPost1.getId(), draftPost2.getId(), publishedPost.getId());
	}

	@Test
	void withAuthor_ReturnPost_WhenOneExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		List<Post> authorPosts = postRepository.findAll(PostSpecs.withAuthor(author.getId()));
		assertThat(authorPosts).hasSize(1);
		assertThat(authorPosts.getFirst()
							  .getId()).isEqualTo(post.getId());
	}

	@Test
	void withAuthor_ReturnEmpty_WhenNoneExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		User otherAuthor = new User("author2@blog.com", "Test Author 2", "password");
		otherAuthor = userRepository.save(otherAuthor);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		List<Post> authorPosts = postRepository.findAll(PostSpecs.withAuthor(otherAuthor.getId()));
		assertThat(authorPosts).hasSize(0);
	}

	@Test
	void withAuthor_ReturnMultiplePosts_WhenMultipleExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2 = postRepository.save(post2);

		List<Post> authorPosts = postRepository.findAll(PostSpecs.withAuthor(author.getId()));
		assertThat(authorPosts).hasSize(2);
		assertThat(authorPosts).extracting(Post::getId)
							   .containsExactlyInAnyOrder(post.getId(), post2.getId());
	}

	@Transactional
	@Test
	void withTags_ReturnPost_WhenOneExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post.addTag(tag2);
		post = postRepository.save(post);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of(TagIdOrSlug.fromId(tag1.getId()))));
		assertThat(postsWithTags).hasSize(1);
		assertThat(postsWithTags.getFirst()
								.getId()).isEqualTo(post.getId());
		assertThat(postsWithTags).allMatch(p -> p.getTags()
												 .stream()
												 .anyMatch(t -> t.getId()
																 .equals(tag1.getId())));
	}

	@Transactional
	@Test
	void withTags_ReturnMultiple_WhenMultipleExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post.addTag(tag2);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2.addTag(tag2);
		post2 = postRepository.save(post2);

		Post post3 = new Post();
		post3.setTitle("Post 3");
		post3.setDescription("post 3 description");
		post3.setBody("post 3 body");
		post3.setReadingTime(3);
		post3.setPublishedAt(null);
		post3.setAuthor(author);
		post3.addTag(tag1);
		post3 = postRepository.save(post3);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of(TagIdOrSlug.fromId(tag1.getId()),
				TagIdOrSlug.fromId(tag2.getId()))));
		assertThat(postsWithTags).hasSize(3);
		assertThat(postsWithTags).extracting(Post::getId)
								 .containsExactlyInAnyOrder(post.getId(), post2.getId(), post3.getId());
	}

	@Test
	void withTags_ReturnNone_WhenNoneExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post = postRepository.save(post);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of(TagIdOrSlug.fromId(tag2.getId()))));
		assertThat(postsWithTags).isEmpty();
	}

	@Transactional
	@Test
	void withTags_ReturnPost_WhenGivenSlug()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post = postRepository.save(post);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of(TagIdOrSlug.fromSlug(tag1.getSlug()))));
		assertThat(postsWithTags).hasSize(1);
		assertThat(postsWithTags.getFirst()
								.getId()).isEqualTo(post.getId());
		assertThat(postsWithTags).allMatch(p -> p.getTags()
												 .stream()
												 .anyMatch(t -> t.getSlug()
																 .equals(tag1.getSlug())));
	}

	@Transactional
	@Test
	void withTags_ReturnMultiple_WhenGivenIdsAndSlugs()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2.addTag(tag2);
		post2 = postRepository.save(post2);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of(TagIdOrSlug.fromSlug(tag1.getSlug()),
				TagIdOrSlug.fromId(tag2.getId()))));
		assertThat(postsWithTags).hasSize(2);
		assertThat(postsWithTags).extracting(Post::getId)
								 .containsExactlyInAnyOrder(post.getId(), post2.getId());
		Set<Long> foundTagIds = postsWithTags.stream()
											 .flatMap(p -> p.getTags()
															.stream())
											 .map(Tag::getId)
											 .collect(Collectors.toSet());

		assertThat(foundTagIds).containsAll(List.of(tag1.getId(), tag2.getId()));
	}

	@Test
	void withTags_ReturnAll_WhenEmptyCollection()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2.addTag(tag2);
		post2 = postRepository.save(post2);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(List.of()));
		assertThat(postsWithTags).hasSize(2);
	}

	@Test
	void withTags_ReturnAll_WhenNullCollection()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post.addTag(tag1);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2.addTag(tag2);
		post2 = postRepository.save(post2);

		List<Post> postsWithTags = postRepository.findAll(PostSpecs.withTags(null));
		assertThat(postsWithTags).hasSize(2);
	}

	@Test
	void titleContains_ReturnPost_WhenExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		List<Post> posts = postRepository.findAll(PostSpecs.titleContains("Post 1"));
		assertThat(posts).hasSize(1);
		assertThat(posts.getFirst()
						.getId()).isEqualTo(post.getId());
		assertThat(posts.getFirst()
						.getTitle()
						.toLowerCase()).containsSubsequence("Post 1".toLowerCase());
	}

	@Test
	void titleContains_ReturnMultiple_WhenMultipleExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		Post post2 = new Post();
		post2.setTitle("Post 2");
		post2.setDescription("post 2 description");
		post2.setBody("post 2 body");
		post2.setReadingTime(3);
		post2.setPublishedAt(null);
		post2.setAuthor(author);
		post2 = postRepository.save(post2);

		Post draft = new Post();
		draft.setTitle("Draft");
		draft.setDescription("Draft description");
		draft.setBody("Draft body");
		draft.setReadingTime(3);
		draft.setPublishedAt(null);
		draft.setAuthor(author);
		draft = postRepository.save(draft);

		List<Post> posts = postRepository.findAll(PostSpecs.titleContains("Post"));
		assertThat(posts).hasSize(2);
		assertThat(posts).extracting(Post::getId)
						 .containsExactlyInAnyOrder(post.getId(), post2.getId());
	}

	@Test
	void titleContains_ReturnNone_WhenNoneExist()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post draft = new Post();
		draft.setTitle("Draft");
		draft.setDescription("Draft description");
		draft.setBody("Draft body");
		draft.setReadingTime(3);
		draft.setPublishedAt(null);
		draft.setAuthor(author);
		draft = postRepository.save(draft);

		List<Post> posts = postRepository.findAll(PostSpecs.titleContains("Post"));
		assertThat(posts).hasSize(0);
	}

	@Test
	void titleContains_ReturnAll_WhenGivenNullString()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		Post draft = new Post();
		draft.setTitle("Draft");
		draft.setDescription("Draft description");
		draft.setBody("Draft body");
		draft.setReadingTime(3);
		draft.setPublishedAt(null);
		draft.setAuthor(author);
		draft = postRepository.save(draft);

		List<Post> posts = postRepository.findAll(PostSpecs.titleContains(null));
		assertThat(posts).hasSize(2);
	}

	@Test
	void titleContains_ReturnAll_WhenGivenBlankString()
	{
		User author = new User("author@blog.com", "Test Author", "password");
		author = userRepository.save(author);

		Post post = new Post();
		post.setTitle("Post 1");
		post.setDescription("post 1 description");
		post.setBody("post 1 body");
		post.setReadingTime(3);
		post.setPublishedAt(null);
		post.setAuthor(author);
		post = postRepository.save(post);

		Post draft = new Post();
		draft.setTitle("Draft");
		draft.setDescription("Draft description");
		draft.setBody("Draft body");
		draft.setReadingTime(3);
		draft.setPublishedAt(null);
		draft.setAuthor(author);
		draft = postRepository.save(draft);

		List<Post> posts = postRepository.findAll(PostSpecs.titleContains("    "));
		assertThat(posts).hasSize(2);
	}
}
