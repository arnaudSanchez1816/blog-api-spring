package com.blog.api.apispring.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService
{
	private final TextService textService;

	public MarkdownService(TextService textService)
	{
		this.textService = textService;
	}

	public String parseMarkdownToPlainText(String markdownText)
	{
		Parser parser = Parser.builder()
							  .build();
		Node parsedBodyMarkdown = parser.parse(markdownText);
		TextContentRenderer textRenderer = TextContentRenderer.builder()
															  .build();

		String plainTextBody = textRenderer.render(parsedBodyMarkdown);
		return plainTextBody;
	}
}
