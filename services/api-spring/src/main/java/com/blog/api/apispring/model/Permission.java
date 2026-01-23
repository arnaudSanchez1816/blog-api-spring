package com.blog.api.apispring.model;

import com.blog.api.apispring.security.authorities.PermissionType;
import jakarta.persistence.*;

import java.util.Set;

@Table(name = "permissions")
@Entity
public class Permission extends BaseEntity
{
	@Column(unique = true)
	@Enumerated(EnumType.STRING)
	private PermissionType type;

	@ManyToMany(mappedBy = "permissions")
	private Set<Role> roles;

	public Permission()
	{
	}

	public Permission(PermissionType type)
	{
		this.type = type;
	}

	public PermissionType getType()
	{
		return type;
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
