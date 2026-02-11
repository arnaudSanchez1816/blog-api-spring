package com.blog.api.apispring.converter;

import com.blog.api.apispring.enums.PostSortBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StringToPostSortByConverter}.
 * This converter transforms string values into {@link PostSortBy} enum constants,
 * performing case-insensitive matching against the enum values.
 */
@DisplayName("StringToPostSortByConverter")
class StringToPostSortByConverterTests
{
	/**
	 * Tests the {@link StringToPostSortByConverter#convert(String)} method.
	 * This method converts a string representation to the corresponding PostSortBy enum value.
	 */

	@Test
	void convert_ReturnsNull_WhenSourceIsNull()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert(null);

		assertThat(result).isNull();
	}

	@Test
	void convert_ReturnsIdAsc_WhenSourceIsId()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("id");

		assertThat(result).isEqualTo(PostSortBy.ID_ASC);
	}

	@Test
	void convert_ReturnsIdDesc_WhenSourceIsMinusId()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("-id");

		assertThat(result).isEqualTo(PostSortBy.ID_DESC);
	}

	@Test
	void convert_ReturnsPublishedAtAsc_WhenSourceIsPublishedAt()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("publishedAt");

		assertThat(result).isEqualTo(PostSortBy.PUBLISHED_AT_ASC);
	}

	@Test
	void convert_ReturnsPublishedAtDesc_WhenSourceIsMinusPublishedAt()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("-publishedAt");

		assertThat(result).isEqualTo(PostSortBy.PUBLISHED_AT_DESC);
	}

	@Test
	void convert_ReturnsIdAsc_WhenSourceIsIdInUpperCase()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("ID");

		assertThat(result).isEqualTo(PostSortBy.ID_ASC);
	}

	@Test
	void convert_ReturnsPublishedAtAsc_WhenSourceIsPublishedAtInMixedCase()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("PuBlIsHeDaT");

		assertThat(result).isEqualTo(PostSortBy.PUBLISHED_AT_ASC);
	}

	@Test
	void convert_ReturnsNull_WhenSourceDoesNotMatchAnyValue()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("invalidValue");

		assertThat(result).isNull();
	}

	@Test
	void convert_ReturnsNull_WhenSourceIsEmptyString()
	{
		StringToPostSortByConverter converter = new StringToPostSortByConverter();

		PostSortBy result = converter.convert("");

		assertThat(result).isNull();
	}
}
