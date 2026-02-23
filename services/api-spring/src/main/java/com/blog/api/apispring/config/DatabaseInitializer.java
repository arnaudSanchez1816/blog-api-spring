package com.blog.api.apispring.config;

import com.blog.api.apispring.model.*;
import com.blog.api.apispring.repository.PermissionRepository;
import com.blog.api.apispring.repository.PostRepository;
import com.blog.api.apispring.repository.RoleRepository;
import com.blog.api.apispring.security.authorities.PermissionType;
import com.blog.api.apispring.service.TagService;
import com.blog.api.apispring.service.UserService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;

@Component
@Profile(value = {"!test"})
public class DatabaseInitializer implements ApplicationRunner
{

	private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
	private final Environment environment;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private TagService tagService;
	private PermissionRepository permissionRepository;
	private RoleRepository roleRepository;
	private PostRepository postRepository;
	private Flyway flyway;
	private ApplicationArguments applicationArguments;

	@Value("classpath:seeding/markdown_test.md")
	private Resource markdownTestFile;
	@Value("classpath:seeding/markdown_lorem1.md")
	private Resource markdownLoremFile;

	public DatabaseInitializer(Environment environment, UserService userService, PasswordEncoder passwordEncoder)
	{
		this.environment = environment;
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired
	public void setPostRepository(PostRepository postRepository)
	{
		this.postRepository = postRepository;
	}

	@Autowired
	public void setPermissionRepository(PermissionRepository permissionRepository)
	{
		this.permissionRepository = permissionRepository;
	}

	@Autowired
	public void setRoleRepository(RoleRepository roleRepository)
	{
		this.roleRepository = roleRepository;
	}

	@Autowired
	public void setTagService(TagService tagService)
	{
		this.tagService = tagService;
	}

	@Autowired
	public void setFlyway(Flyway flyway)
	{
		this.flyway = flyway;
	}

	@Autowired
	public void setApplicationArguments(ApplicationArguments applicationArguments)
	{
		this.applicationArguments = applicationArguments;
	}

	@Override
	public void run(@NonNull ApplicationArguments args)
	{
		log.debug("Database initialization");
		if (applicationArguments.containsOption("reset-db"))
		{
			// Clear database
			log.info("Reset database...");
			try
			{
				flyway.clean();
				flyway.migrate();
			} catch (FlywayException e)
			{
				log.error("Failed to reset database\nMake sure you have the dev profile enabled\n{}", e.getMessage());
			}
		}

		if (!applicationArguments.containsOption("seed-db"))
		{
			return;
		}

		log.info("Seeding database...");
		// Permissions
		Permission read = createPermissionIfNotFound(PermissionType.READ);
		Permission create = createPermissionIfNotFound(PermissionType.CREATE);
		Permission delete = createPermissionIfNotFound(PermissionType.DELETE);
		Permission update = createPermissionIfNotFound(PermissionType.UPDATE);

		Set<Permission> allPermissions = new HashSet<>(Arrays.asList(read, create, delete, update));
		// Roles
		Role adminRole = createRoleIfNotFound("ROLE_ADMIN", allPermissions);
		Role userRole = createRoleIfNotFound("ROLE_USER", Collections.singleton(read));

		// Users
		String adminName = environment.getRequiredProperty("blog-api.seeding.users.admin-name");
		String adminEmail = environment.getRequiredProperty("blog-api.seeding.users.admin-email");
		String adminPassword = environment.getRequiredProperty("blog-api.seeding.users.admin-password");
		User adminUser = createUserIfNotFound(adminEmail,
				adminName,
				passwordEncoder.encode(adminPassword),
				Collections.singleton(adminRole));

		// Tags
		Tag jsTag = createTagIfNotFound("Javascript", "js");
		Tag springTag = createTagIfNotFound("Java Spring", "spring");

		// Posts
		Optional<Post> optionalPost = postRepository.findById(1L);
		if (optionalPost.isEmpty())
		{
			Post post = new Post();
			post.setTitle("Markdown display test");
			String body = readMarkdownFileResource(markdownTestFile, "# This is a blog post !");
			post.setBody(body);
			post.setDescription("This post display examples of supported markdown features.");
			post.setAuthor(adminUser);
			post.setReadingTime(5);
			post.setPublishedAt(OffsetDateTime.now());
			post.addTag(jsTag);
			postRepository.save(post);
		}

		Optional<Post> optionalPost2 = postRepository.findById(2L);
		if (optionalPost2.isEmpty())
		{
			Post post = new Post();
			post.setTitle("Munera parabat turis");
			String body = readMarkdownFileResource(markdownLoremFile,
					"Lorem markdownum. Cura spumis despexitque tegi Tartara");
			post.setBody(body);
			post.setDescription("Lorem markdownum. Cura spumis despexitque tegi Tartara");
			post.setAuthor(adminUser);
			post.setReadingTime(2);
			post.setPublishedAt(OffsetDateTime.now());
			post.addTag(jsTag);
			post.addTag(springTag);
			Comment comment = new Comment();
			comment.setUsername("username");
			comment.setCreatedAt(OffsetDateTime.now());
			comment.setBody("Comment body");
			post.addComment(comment);
			postRepository.save(post);
		}
		Optional<Post> optionalDraftPost = postRepository.findById(3L);
		if (optionalDraftPost.isEmpty())
		{
			Post post = new Post();
			post.setTitle("Draft post");
			post.setBody("# This is an unpublished post !\nYou can edit my content or publish me right away!");
			post.setDescription("Draft post, this post is not visible except to you!");
			post.setAuthor(adminUser);
			post.setReadingTime(1);
			postRepository.save(post);
		}

		log.info("Database seeding done!");
	}

	private String readMarkdownFileResource(Resource fileResource, String defaultText)
	{
		String body = defaultText;
		try
		{
			if (fileResource != null && fileResource.exists() && fileResource.isFile())
			{
				File file = fileResource.getFile();
				body = new String(Files.readAllBytes(file.toPath()));
			}
		} catch (IOException e)
		{
			log.error("Failed to read markdown resource file");
			body = defaultText;
		}
		return body;
	}

	private Permission createPermissionIfNotFound(PermissionType permissionType)
	{
		Optional<Permission> optionalPermission = permissionRepository.findByType(permissionType);
		if (optionalPermission.isEmpty())
		{
			Permission permission = new Permission(permissionType);
			return permissionRepository.save(permission);
		}
		return optionalPermission.get();
	}

	private Role createRoleIfNotFound(String name, Set<Permission> permissions)
	{
		Optional<Role> optionalRole = roleRepository.findByName(name);
		if (optionalRole.isEmpty())
		{
			Role role = new Role(name);
			role.setPermissions(permissions);
			return roleRepository.save(role);
		}
		return optionalRole.get();
	}

	private User createUserIfNotFound(String email, String name, String password, Set<Role> roles)
	{
		Optional<User> optionalUser = userService.findByEmail(email);
		if (optionalUser.isEmpty())
		{
			User user = new User(email, name, password);
			user.setRoles(roles);
			return userService.saveUser(user);
		}
		return optionalUser.get();
	}

	private Tag createTagIfNotFound(String name, String slug)
	{
		Optional<Tag> optionalTag = tagService.getTag(slug);
		if (optionalTag.isEmpty())
		{
			Tag tag = new Tag(name, slug);
			return tagService.saveTag(tag);
		}
		return optionalTag.get();
	}
}
