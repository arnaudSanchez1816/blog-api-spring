package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>
{
	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"roles", "roles.permissions"})
	Optional<User> findWithRolesById(long id);
}
