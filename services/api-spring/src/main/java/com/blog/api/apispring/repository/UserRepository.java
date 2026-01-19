package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>
{
	Optional<User> findByEmail(String email);
}
