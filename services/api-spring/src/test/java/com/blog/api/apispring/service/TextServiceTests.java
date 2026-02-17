package com.blog.api.apispring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TextServiceTests
{
	private TextService textService;

	@BeforeEach
	void setUp()
	{
		textService = new TextService();
	}

	@Nested
	@DisplayName("getFirstWordsSubstring with custom word count")
	class GetFirstWordsSubstringCustomCountTests
	{
		@Test
		@DisplayName("should return first N words when text has more than N words")
		void getFirstWordsSubstring_shouldReturnFirstNWords()
		{
			String text = "one two three four five six seven eight nine ten";

			String result = textService.getFirstWordsSubstring(text, 5);

			assertEquals("one two three four five", result);
		}

		@Test
		@DisplayName("should return entire text when text has less than N words")
		void getFirstWordsSubstring_shouldReturnEntireTextWhenLessThanNWords()
		{
			String text = "one two three";

			String result = textService.getFirstWordsSubstring(text, 10);

			assertEquals("one two three", result);
		}

		@Test
		@DisplayName("should return entire text when text has exactly N words")
		void getFirstWordsSubstring_shouldReturnEntireTextWhenExactlyNWords()
		{
			String text = "one two three four five";

			String result = textService.getFirstWordsSubstring(text, 5);

			assertEquals("one two three four five", result);
		}

		@Test
		@DisplayName("should treat negative word count as 1")
		void getFirstWordsSubstring_shouldTreatNegativeWordCountAsOne()
		{
			String text = "one two three four five";

			String result = textService.getFirstWordsSubstring(text, -5);

			assertEquals("one", result);
		}

		@Test
		@DisplayName("should treat zero word count as 1")
		void getFirstWordsSubstring_shouldTreatZeroWordCountAsOne()
		{
			String text = "one two three four five";

			String result = textService.getFirstWordsSubstring(text, 0);

			assertEquals("one", result);
		}

		@Test
		@DisplayName("should handle word count of 1")
		void getFirstWordsSubstring_shouldHandleWordCountOfOne()
		{
			String text = "one two three";

			String result = textService.getFirstWordsSubstring(text, 1);

			assertEquals("one", result);
		}

		@Test
		@DisplayName("should handle text with multiple spaces between words")
		void getFirstWordsSubstring_shouldHandleMultipleSpacesBetweenWords()
		{
			String text = "one  two   three    four";

			String result = textService.getFirstWordsSubstring(text, 2);

			assertEquals("one  two", result);
		}

		@Test
		@DisplayName("should handle text with newlines")
		void getFirstWordsSubstring_shouldHandleTextWithNewlines()
		{
			String text = "one\ntwo\nthree\nfour";

			String result = textService.getFirstWordsSubstring(text, 2);

			assertEquals("one\ntwo", result);
		}

		@Test
		@DisplayName("should handle text with tabs")
		void getFirstWordsSubstring_shouldHandleTextWithTabs()
		{
			String text = "one\ttwo\tthree\tfour";

			String result = textService.getFirstWordsSubstring(text, 2);

			assertEquals("one\ttwo", result);
		}
	}

	@Nested
	@DisplayName("estimateReadingTime")
	class EstimateReadingTime
	{
		@Test
		void estimateReadingTime_Returns2_When400Words()
		{
			StringBuilder text = new StringBuilder();
			for (int i = 1; i <= 400; i++)
			{
				text.append("word")
					.append(i)
					.append(" ");
			}
			int result = textService.estimateReadingTime(text.toString());

			assertThat(result).isEqualTo(2);
		}

		@Test
		void estimateReadingTime_Returns1_WhenLessThan200Words()
		{
			StringBuilder text = new StringBuilder();
			for (int i = 1; i <= 100; i++)
			{
				text.append("word")
					.append(i)
					.append(" ");
			}
			int result = textService.estimateReadingTime(text.toString());

			assertThat(result).isEqualTo(1);
		}

		@Test
		void estimateReadingTime_Returns1_WhenEmptyText()
		{
			String text = "";

			int result = textService.estimateReadingTime(text);

			assertThat(result).isEqualTo(1);
		}

		@Test
		void estimateReadingTime_HandleNewLines()
		{
			StringBuilder text = new StringBuilder();
			for (int i = 1; i <= 1000; i++)
			{
				text.append("word")
					.append(i)
					.append("\n");
			}
			int result = textService.estimateReadingTime(text.toString());

			assertThat(result).isEqualTo(1000 / 200);
		}

		@Test
		void estimateReadingTime_HandleTabs()
		{
			StringBuilder text = new StringBuilder();
			for (int i = 1; i <= 1000; i++)
			{
				text.append("word")
					.append(i)
					.append("\t");
			}
			int result = textService.estimateReadingTime(text.toString());

			assertThat(result).isEqualTo(1000 / 200);
		}
	}
}