package com.blog.api.apispring.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextService
{
	public String getFirstWordsSubstring(String text, int wordCount)
	{
		wordCount = Math.max(1, wordCount);

		String substring = text;
		Pattern pattern = Pattern.compile("(^(?:\\S+\\s*){1," + wordCount + "}).*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(substring);
		if (matcher.find())
		{
			String first50WordsGroup = matcher.group(1);
			if (first50WordsGroup != null)
			{
				substring = first50WordsGroup.trim();
			}
		}

		return substring;
	}

	public int estimateReadingTime(String text)
	{
		Pattern pattern = Pattern.compile("\\S+");
		Matcher matcher = pattern.matcher(text);
		int count = 0;
		while (matcher.find())
		{
			count += 1;
		}

		return count > 0 ? Math.max(count / 200, 1) : 1;
	}

	public String sanitizeText(String text)
	{
		Cleaner cleaner = new Cleaner(Safelist.none());
		return cleaner.clean(Jsoup.parse(text))
					  .wholeText();
	}
}
