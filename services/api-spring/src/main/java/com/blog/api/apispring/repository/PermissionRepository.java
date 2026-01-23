package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Permission;
import com.blog.api.apispring.security.authorities.PermissionType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long>
{
	Optional<Permission> findByType(PermissionType type);
}
