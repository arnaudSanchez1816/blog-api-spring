package com.blog.api.apispring.controller;

import com.blog.api.apispring.PostgresTestConfig;
import com.blog.api.apispring.dto.tag.CreateTagRequest;
import com.blog.api.apispring.dto.tag.GetTagsResponse;
import com.blog.api.apispring.dto.tag.UpdateTagRequest;
import com.blog.api.apispring.extensions.ClearDatabaseExtension;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.repository.TagRepository;
import com.blog.api.apispring.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(PostgresTestConfig.class)
@WebAppConfiguration
@AutoConfigureMockMvc
@ExtendWith(ClearDatabaseExtension.class)
class TagControllerTests
{
	// https://blog.jetbrains.com/idea/2025/04/a-practical-guide-to-testing-spring-controllers-with-mockmvctester/
	@Autowired
	private MockMvcTester mockMvc;

	@Autowired
	private TagRepository tagRepository;

	private Tag tag1;
	private Tag tag2;

	private final static String TAG_NAME_TOO_LONG = "\"NewTagabsdqsdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdsqdqsdqsdqsdsqdqsdqsdqsdsqdsqdqsdqsdqsddqdqsd dqsdqsdsqdsqdsqdsqd";
	private final static String TAG_SLUG_INVALID = "new_tag@badSl ug";
	private final static String TAG_SLUG_TOO_LONG = "new-tag-slug-very-long-slug-that-is-way-over-30-characters-ok";

	@BeforeEach
	void setUp()
	{
		tag1 = new Tag("tag1", "tag-1-slug");
		tag2 = new Tag("tag2", "tag-2-slug");
		tag1 = tagRepository.save(tag1);
		tag2 = tagRepository.save(tag2);
	}

	@Test
	void getTags_IsOk()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(GetTagsResponse.class)
							.satisfies(body ->
							{
								assertThat(body.metadata()
											   .getCount()).isEqualTo(2);
								assertThat(body.results()
											   .size()).isEqualTo(2);
								assertThat(body.results()
											   .getFirst()).isEqualTo(tag1);
								assertThat(body.results()
											   .get(1)).isEqualTo(tag2);
							});
	}

	@Test
	void getTag_IsOk_WhenGivenAValidId()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags/1")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(Tag.class)
							.satisfies(body -> assertThat(body).isEqualTo(tag1));
	}

	@Test
	void getTag_IsOk_WhenGivenAValidSlug()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags/tag-2-slug")
										.exchange();
		assertThat(response).hasStatusOk()
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(Tag.class)
							.satisfies(body -> assertThat(body).isEqualTo(tag2));
	}

	@Test
	void getTag_Is400_WhenGivenAnInvalidSlugs()
	{
		assertThat(mockMvc.get()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/" + TAG_SLUG_INVALID)

		).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void getTag_Is404_WhenGivenAnInvalidId()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags/154545")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void getTag_Is404_WhenGivenASlugThatDoesNotExists()
	{
		MvcTestResult response = mockMvc.get()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags/tag-missing-slug")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_IsOk_WhenGivenValidNewTagData()
	{
		final String tagName = "NewTag";
		final String tagSlug = "new-tag-slug";
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);

		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.CREATED)
							.hasContentType(MediaType.APPLICATION_JSON)
							.bodyJson()
							.convertTo(Tag.class)
							.satisfies(tag ->
							{
								assertThat(tag.getName()).isEqualTo(tagName);
								assertThat(tag.getSlug()).isEqualTo(tagSlug);
							});
		String location = response.getResponse()
								  .getHeader("Location");
		assertThat(location).isNotNull();
		assertThat(mockMvc.get()
						  .uri(location)).hasStatusOk()
										 .bodyJson()
										 .convertTo(Tag.class)
										 .satisfies(t ->
										 {
											 assertThat(t.getName()).isEqualTo(tagName);
											 assertThat(t.getSlug()).isEqualTo(tagSlug);
										 });
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenGivenABadSlug()
	{
		final String tagName = "NewTag";
		final String tagSlug = TAG_SLUG_INVALID;
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenGivenANameTooLong()
	{
		final String tagName = TAG_NAME_TOO_LONG;
		final String tagSlug = "new-tag-slug";
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenGivenASlugTooLong()
	{
		final String tagName = "New tag";
		final String tagSlug = TAG_SLUG_TOO_LONG;
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenNotGivenASlug()
	{
		final String tagName = "New tag";
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, null);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenNotGivenAName()
	{
		final String tagSlug = "tag-slug";
		CreateTagRequest createTagRequest = new CreateTagRequest(null, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(value = "admin", authorities = "CREATE")
	void createTag_400_WhenRequestBodyIsEmpty()
	{
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.exchange();
		assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void createTag_401_WhenNotAuthenticated()
	{
		final String tagName = "New tag";
		final String tagSlug = "new-tag-slug";
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@WithMockUser(authorities = "READ")
	void createTag_403_WhenUserHasMissingPermissions()
	{
		final String tagName = "New tag";
		final String tagSlug = "new-tag-slug";
		CreateTagRequest createTagRequest = new CreateTagRequest(tagName, tagSlug);
		MvcTestResult response = mockMvc.post()
										.contentType(MediaType.APPLICATION_JSON)
										.uri("/tags")
										.content(JsonUtils.asJsonString(createTagRequest))
										.exchange();
		assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_IsOk_WhenGivenValidData()
	{
		final String tagName = "UpdatedTag";
		final String tagSlug = "updated-tag-slug";
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		MvcTestResult result = mockMvc.put()
									  .contentType(MediaType.APPLICATION_JSON)
									  .uri("/tags/1")
									  .content(JsonUtils.asJsonString(updateTagRequest))
									  .exchange();
		assertThat(result).hasStatusOk()
						  .hasContentType(MediaType.APPLICATION_JSON)
						  .bodyJson()
						  .convertTo(Tag.class)
						  .satisfies(t ->
						  {
							  assertThat(t.getName()).isEqualTo(tagName);
							  assertThat(t.getSlug()).isEqualTo(tagSlug);
						  });

		assertThat(mockMvc.get()
						  .uri("/tags/1")).hasStatusOk()
										  .bodyJson()
										  .convertTo(Tag.class)
										  .satisfies(t ->
										  {
											  assertThat(t.getName()).isEqualTo(tagName);
											  assertThat(t.getSlug()).isEqualTo(tagSlug);
										  });
	}

	@Test
	void updateTag_Is401_WhenNotAuthenticated()
	{
		final String tagName = "UpdatedTag";
		final String tagSlug = "updated-tag-slug";
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@WithMockUser(authorities = "READ")
	void updateTag_Is403_WhenMissingPermissions()
	{
		final String tagName = "UpdatedTag";
		final String tagSlug = "updated-tag-slug";
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenTagNameIsTooLong()
	{
		final String tagName = TAG_NAME_TOO_LONG;
		final String tagSlug = "updated-tag-slug";
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenTagSlugIsTooLong()
	{
		final String tagName = "TagName";
		final String tagSlug = TAG_SLUG_TOO_LONG;
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenTagSlugIsInvalid()
	{
		final String tagName = "TagName";
		final String tagSlug = TAG_SLUG_INVALID;
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, tagSlug);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenTagSlugIsMissing()
	{
		final String tagName = "TagName";
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(tagName, null);
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenTagNameIsMissing()
	{
		UpdateTagRequest updateTagRequest = new UpdateTagRequest(null, "slug");
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenRequestBodyIsEmpty()
	{
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/1")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is400_WhenSlugParamIsInvalid()
	{
		UpdateTagRequest updateTagRequest = new UpdateTagRequest("tagName", "tag-slug");
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/" + TAG_SLUG_INVALID)
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is404_WhenTagIdNotFound()
	{
		UpdateTagRequest updateTagRequest = new UpdateTagRequest("tagName", "tag-slug");
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/154154515")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	@WithMockUser(authorities = "UPDATE")
	void updateTag_Is404_WhenTagSlugNotFound()
	{
		UpdateTagRequest updateTagRequest = new UpdateTagRequest("tagName", "tag-slug");
		assertThat(mockMvc.put()
						  .contentType(MediaType.APPLICATION_JSON)
						  .uri("/tags/not-found-slug")
						  .content(JsonUtils.asJsonString(updateTagRequest))).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	@WithMockUser(authorities = "DELETE")
	void deleteTag_IsOk_WhenGivenValidId()
	{
		MvcTestResult result = mockMvc.delete()
									  .uri("/tags/1")
									  .exchange();
		assertThat(result).hasStatusOk()
						  .hasContentType(MediaType.APPLICATION_JSON)
						  .bodyJson()
						  .convertTo(Tag.class)
						  .satisfies(t ->
						  {
							  assertThat(t.getId()).isEqualTo(1);
						  });

		assertThat(mockMvc.get()
						  .uri("/tags/1")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	@WithMockUser(authorities = "DELETE")
	void deleteTag_IsOk_WhenGivenValidSlug()
	{
		MvcTestResult result = mockMvc.delete()
									  .uri("/tags/" + tag1.getSlug())
									  .exchange();
		assertThat(result).hasStatusOk()
						  .hasContentType(MediaType.APPLICATION_JSON)
						  .bodyJson()
						  .convertTo(Tag.class)
						  .satisfies(t ->
						  {
							  assertThat(t.getSlug()).isEqualTo(tag1.getSlug());
						  });

		assertThat(mockMvc.get()
						  .uri("/tags/" + tag1.getSlug())).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteTag_Is401_WhenUnauthenticated()
	{
		assertThat(mockMvc.delete()
						  .uri("/tags/1")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@WithMockUser(authorities = "READ")
	void deleteTag_Is403_WhenMissingPermissions()
	{
		assertThat(mockMvc.delete()
						  .uri("/tags/1")).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = "DELETE")
	void deleteTag_Is400_WhenSlugParamIsInvalid()
	{
		assertThat(mockMvc.delete()
						  .uri("/tags/" + TAG_SLUG_INVALID)).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	@WithMockUser(authorities = "DELETE")
	void deleteTag_Is404_WhenIdNotFound()
	{
		assertThat(mockMvc.delete()
						  .uri("/tags/1544545")).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	@WithMockUser(authorities = "DELETE")
	void deleteTag_Is404_WhenSlugNotFound()
	{
		assertThat(mockMvc.delete()
						  .uri("/tags/slug-not-found")).hasStatus(HttpStatus.NOT_FOUND);
	}
}
