package com.blog.api.apispring.annotation;

import com.blog.api.apispring.converter.PostIdConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to eagerly fetch comments of a Post inside {@link PostIdConverter}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithPostComments
{
}
