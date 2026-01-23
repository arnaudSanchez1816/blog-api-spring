package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long>
{
	Optional<Role> findByName(String name);
}
