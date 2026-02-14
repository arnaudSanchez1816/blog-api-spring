package com.blog.api.apispring.service;

import com.blog.api.apispring.model.Role;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService
{
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final PostService postService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PostService postService)
	{
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.postService = postService;
	}

	public Optional<User> findById(Long id)
	{
		return userRepository.findWithRolesById(id);
	}

	public Optional<User> findByEmail(String email)
	{
		return userRepository.findByEmail(email);
	}

	public User createUser(String email, String name, String password, Role role)
	{
		String encodedPassword = passwordEncoder.encode(password);

		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setPassword(encodedPassword);
		user.addRole(role);

		return userRepository.save(user);
	}

	@Transactional
	public User saveUser(User user)
	{
		if (user == null)
		{
			throw new IllegalArgumentException("Given user is null");
		}

		return userRepository.save(user);
	}
}
