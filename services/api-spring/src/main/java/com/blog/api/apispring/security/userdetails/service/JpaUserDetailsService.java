package com.blog.api.apispring.security.userdetails.service;

import com.blog.api.apispring.exception.UserNotFoundException;
import com.blog.api.apispring.model.Permission;
import com.blog.api.apispring.model.Role;
import com.blog.api.apispring.model.User;
import com.blog.api.apispring.repository.UserRepository;
import com.blog.api.apispring.security.userdetails.SecurityUser;
import com.blog.api.apispring.service.UserService;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class JpaUserDetailsService implements BlogUserDetailsService
{
	private final UserService userService;

	public JpaUserDetailsService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	@NullMarked
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
	{
		Optional<User> optionalUser = userService.findByEmail(email);
		if (optionalUser.isEmpty())
		{
			throw new UsernameNotFoundException(String.format("User not found:%s", email));
		}
		User user = optionalUser.get();
		Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRoles());

		return new SecurityUser(user, authorities);
	}

	@Override
	public UserDetails loadUserById(@NotNull Long id) throws UserNotFoundException
	{
		Optional<User> optionalUser = userService.findById(id);
		if (optionalUser.isEmpty())
		{
			throw UserNotFoundException.fromId(id);
		}
		User user = optionalUser.get();
		Collection<? extends GrantedAuthority> authorities = getAuthorities(user.getRoles());

		return new SecurityUser(user, authorities);
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles)
	{
		return getGrantedAuthorities(getPrivileges(roles));
	}

	private List<String> getPrivileges(Collection<Role> roles)
	{

		List<String> privileges = new ArrayList<>();
		List<Permission> collection = new ArrayList<>();
		for (Role role : roles)
		{
			privileges.add(role.getName());
			collection.addAll(role.getPermissions());
		}
		for (Permission item : collection)
		{
			privileges.add(item.getType()
							   .name());
		}
		return privileges;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges)
	{
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String privilege : privileges)
		{
			authorities.add(new SimpleGrantedAuthority(privilege));
		}
		return authorities;
	}
}
