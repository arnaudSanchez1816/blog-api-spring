package com.blog.api.apispring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Length;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Table(name = "comments")
@Entity
public class Comment extends BaseEntity {

	@NotNull
	private String username;

	@NotNull
	@Column(length = Length.LONG32)
	private String body;

	@CurrentTimestamp(source = SourceType.VM)
	@NotNull
	@Column(name = "created_at")
	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	private OffsetDateTime createdAt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "post_id", nullable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Post post;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}
}
