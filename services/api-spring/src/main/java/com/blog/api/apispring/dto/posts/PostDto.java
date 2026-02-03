package com.blog.api.apispring.dto.posts;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Tag;
import com.blog.api.apispring.projection.PostInfoWithAuthor;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndComments;
import com.blog.api.apispring.projection.PostInfoWithAuthorAndTags;
import com.blog.api.apispring.projection.PostInfoWithAuthorTagsComments;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class PostDto
{
	private long id;
	private String title;
	private String description;
	private String body;
	private OffsetDateTime publishedAt;
	private int readingTime;
	private Long commentsCount;
	private AuthorDto author;
	private Set<TagDto> tags;
	private Set<CommentDto> comments;

	private record AuthorDto(long id, String name)
	{
	}

	private record TagDto(long id, String name, String slug)
	{
	}

	private record CommentDto(long id, String username, String body, OffsetDateTime createdAt)
	{
	}

	public PostDto(PostInfoWithAuthor postInfo)
	{
		this.setFromPostInfo(postInfo);
	}

	public PostDto(PostInfoWithAuthorAndTags postInfo)
	{
		this.setFromPostInfo(postInfo);
		this.setTags(postInfo.getTags());
	}

	public PostDto(PostInfoWithAuthorAndComments postInfo)
	{
		this.setFromPostInfo(postInfo);
		this.setComments(postInfo.getComments());
	}

	public PostDto(PostInfoWithAuthorTagsComments postInfo)
	{
		this.setFromPostInfo(postInfo);
		this.setTags(postInfo.getTags());
		this.setComments(postInfo.getComments());
	}

	private void setFromPostInfo(PostInfoWithAuthor postInfo)
	{
		this.id = postInfo.getId();
		this.title = postInfo.getTitle();
		this.description = postInfo.getDescription();
		this.body = postInfo.getBody();
		this.publishedAt = postInfo.getPublishedAt();
		this.readingTime = postInfo.getReadingTime();
		PostInfoWithAuthor.UserInfo author = postInfo.getAuthor();
		this.author = new AuthorDto(author.getId(), author.getName());
	}

	public void setTags(Set<Tag> tags)
	{
		this.tags = tags.stream()
						.map(tag -> new TagDto(tag.getId(), tag.getName(), tag.getSlug()))
						.collect(Collectors.toUnmodifiableSet());
	}

	public void setComments(Set<Comment> comments)
	{
		this.comments = comments.stream()
								.map(comment -> new CommentDto(comment.getId(), comment.getUsername(),
										comment.getBody(), comment.getCreatedAt()))
								.collect(Collectors.toUnmodifiableSet());
		setCommentsCount(comments.size());
	}

	public void setCommentsCount(long commentsCount)
	{
		this.commentsCount = commentsCount;
	}
}
