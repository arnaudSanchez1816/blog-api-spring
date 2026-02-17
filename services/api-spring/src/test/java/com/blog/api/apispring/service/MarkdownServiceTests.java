package com.blog.api.apispring.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkdownServiceTests
{
	@Mock
	private TextService textService;

	private MarkdownService markdownService;

	@BeforeEach
	void setUp()
	{
		markdownService = new MarkdownService(textService);
	}

	@Test
	void parseMarkdownToPlainText_ShouldParseMarkdownToPlainText()
	{
		try (MockedStatic<Parser> parserStaticMock = Mockito.mockStatic(Parser.class))
		{
			Parser parserMock = Mockito.mock(Parser.class);
			Parser.Builder parserBuilderMock = Mockito.mock(Parser.Builder.class);
			when(parserBuilderMock.build()).thenReturn(parserMock);
			parserStaticMock.when(Parser::builder)
							.thenReturn(parserBuilderMock);
			try (MockedStatic<TextContentRenderer> textContentRendererMock = Mockito.mockStatic(TextContentRenderer.class))
			{
				TextContentRenderer.Builder mockBuilder = Mockito.mock(TextContentRenderer.Builder.class);
				TextContentRenderer rendererMock = Mockito.mock(TextContentRenderer.class);
				textContentRendererMock.when(TextContentRenderer::builder)
									   .thenReturn(mockBuilder);
				when(mockBuilder.build()).thenReturn(rendererMock);

				String markdown = "# This is markdown";

				Node parsedMarkdown = Mockito.mock(Node.class);
				when(parserMock.parse(markdown)).thenReturn(parsedMarkdown);
				when(rendererMock.render(parsedMarkdown)).thenReturn("Plain text");

				String result = markdownService.parseMarkdownToPlainText(markdown);

				assertThat(result).isEqualTo("Plain text");
				verify(parserMock).parse(markdown);
				verify(rendererMock).render(parsedMarkdown);
			}
		}
	}
}