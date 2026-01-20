package com.blog.api.apispring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity
{

	@NotNull
	@Column(length = 64)
	private String name;

	@NotNull
	@Column(unique = true, length = 64)
	private String slug;

	public Tag()
	{
	}

	public Tag(String name, String slug)
	{
		this.name = name;
		this.slug = slug;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSlug()
	{
		return slug;
	}

	public void setSlug(String slug)
	{
		this.slug = slug;
	}
}
