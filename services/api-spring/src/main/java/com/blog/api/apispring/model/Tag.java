package com.blog.api.apispring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o)
	{
		if (o == null || getClass() != o.getClass()) return false;
		Tag tag = (Tag) o;
		return Objects.equals(name, tag.name) && Objects.equals(slug, tag.slug);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, slug);
	}
}
