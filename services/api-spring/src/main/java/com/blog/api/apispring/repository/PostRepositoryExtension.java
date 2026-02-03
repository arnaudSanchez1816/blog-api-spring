package com.blog.api.apispring.repository;

import com.blog.api.apispring.model.Comment;
import com.blog.api.apispring.model.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PostRepositoryExtension
{
	Map<Long, Long> countCommentsByIds(@Param("postIds") List<Long> postIds);
}

class PostRepositoryExtensionImpl implements PostRepositoryExtension
{
	private final EntityManager em;

	public PostRepositoryExtensionImpl(JpaContext context)
	{
		this.em = context.getEntityManagerByManagedType(Post.class);
	}

	@Override
	public Map<Long, Long> countCommentsByIds(List<Long> postIds)
	{
		record PostCommentCount(Long id, Long count)
		{
		}

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		Metamodel m = em.getMetamodel();
		EntityType<Post> Post_ = m.entity(Post.class);
		EntityType<Comment> Comment_ = m.entity(Comment.class);

		CriteriaQuery<PostCommentCount> query = criteriaBuilder.createQuery(PostCommentCount.class);
		Root<Post> postRoot = query.from(Post_);
		SetJoin<Post, Comment> commentJoin = postRoot.join(Post_.getSet("comments", Comment.class), JoinType.LEFT);

		query.select(criteriaBuilder.construct(PostCommentCount.class, postRoot.get(Post_.getId(Long.class)),
				criteriaBuilder.count(commentJoin.get(Comment_.getId(Long.class)))));
		query.groupBy(postRoot.get(Post_.getId(Long.class)));
		query.orderBy(criteriaBuilder.asc(postRoot.get(Post_.getId(Long.class))));

		List<PostCommentCount> commentCounts = em.createQuery(query)
												 .getResultList();
		Map<Long, Long> map = new HashMap<>(postIds.size());
		postIds.forEach(id -> map.put(id, 0L));
		commentCounts.forEach(postCommentCount -> map.replace(postCommentCount.id(), postCommentCount.count()));

		return map;
	}
}