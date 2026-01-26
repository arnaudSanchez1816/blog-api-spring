package com.blog.api.apispring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity
{

	@Column(unique = true)
	@NotNull
	private String email;

	@NotNull
	private String name;

	@NotNull
	@JsonIgnore
	private String password;

	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<Post> posts = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	public User()
	{
	}

	public User(String email, String name, String password)
	{
		this.email = email;
		this.name = name;
		this.password = password;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public List<Post> getPosts()
	{
		return posts;
	}

	public void addPost(Post post)
	{
		getPosts().add(post);
	}

	public void addRole(Role role)
	{
		if (roles == null)
		{
			roles = new HashSet<>();
		}
		roles.add(role);
	}

	public void setRoles(Set<Role> roles)
	{
		this.roles = roles;
	}

	public Set<Role> getRoles()
	{
		return roles;
	}
}
