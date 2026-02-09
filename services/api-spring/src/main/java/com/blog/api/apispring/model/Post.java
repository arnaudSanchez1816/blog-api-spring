package com.blog.api.apispring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Length;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.*;

@Table(name = "posts")
@Entity
public class Post extends BaseEntity
{
	@NotBlank(message = "Post title cannot be empty.")
	private String title;

	@NotNull
	@ColumnDefault("")
	@Column(length = Length.LONG32)
	private String description = "";

	@NotNull
	@Column(length = Length.LONG32)
	private String body = "New post body";

	@ColumnDefault("1")
	@NotNull
	private int readingTime = 1;

	@Column(name = "published_at")
	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	private OffsetDateTime publishedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private User author;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Comment> comments = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "posts_tags",
			   joinColumns = @JoinColumn(name = "post_id"),
			   inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new LinkedHashSet<>();

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public int getReadingTime()
	{
		return readingTime;
	}

	public void setReadingTime(int readingTime)
	{
		this.readingTime = readingTime;
	}

	public OffsetDateTime getPublishedAt()
	{
		return publishedAt;
	}

	public void setPublishedAt(OffsetDateTime publishedAt)
	{
		this.publishedAt = publishedAt;
	}

	public User getAuthor()
	{
		return author;
	}

	public void setAuthor(User author)
	{
		this.author = author;
	}

	public Set<Comment> getComments()
	{
		return comments;
	}

	public Set<Tag> getTags()
	{
		return tags;
	}

	public void setTags(Set<Tag> tags)
	{
		this.tags = tags;
	}

	public void addTag(Tag newTag)
	{
		getTags().add(newTag);
	}

	public void addComment(Comment comment)
	{
		getComments().add(comment);
		comment.setPost(this);
	}

	public boolean isPublished()
	{
		return this.publishedAt != null;
	}
}
