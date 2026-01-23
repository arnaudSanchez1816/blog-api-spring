package com.blog.api.apispring.model;

import jakarta.persistence.*;

import java.util.Set;

//https://www.baeldung.com/role-and-privilege-for-spring-security-registration
@Table(name = "roles")
@Entity
public class Role extends BaseEntity
{
	@Column(unique = true)
	private String name;

	@ManyToMany
	@JoinTable(name = "roles_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private Set<Permission> permissions;

	@ManyToMany(mappedBy = "roles")
	private Set<User> users;

	public Role()
	{
	}

	public Role(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Set<Permission> getPermissions()
	{
		return permissions;
	}

	public Set<User> getUsers()
	{
		return users;
	}

	public void setPermissions(Set<Permission> permissions)
	{
		this.permissions = permissions;
	}
}
