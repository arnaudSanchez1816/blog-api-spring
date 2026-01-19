package com.blog.api.apispring.service;

import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService
{

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository)
	{
		this.userRepository = userRepository;
	}

	public Optional<User> findById(Long id)
	{
		return userRepository.findById(id);
	}

	public Optional<User> findByEmail(String email)
	{
		return userRepository.findByEmail(email);
	}

	@Transactional
	public void saveUser(User user)
	{
		if (user == null)
		{
			throw new IllegalArgumentException("Given user is null");
		}

		userRepository.save(user);
	}
}
