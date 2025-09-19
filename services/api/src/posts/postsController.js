import {
    createPostCommentValidator,
    createPostValidator,
    deletePostValidator,
    getPostCommentsValidator,
    getPublishedPostsValidator,
    getPublishedPostValidator,
    publishPostValidator,
    updatePostValidator,
} from "./postsValidators.js"
import postsService, { userCanViewPost } from "./postsService.js"
import commentsService from "../comments/commentsService.js"
import { validateRequest } from "../middlewares/validator.js"
import createHttpError from "http-errors"
import { checkPermission } from "../middlewares/checkPermission.js"
import { PermissionType } from "@prisma/client"

export const getPost = [
    validateRequest(getPublishedPostValidator),
    async (req, res, next) => {
        try {
            const { id } = req.params
            const { id: userId } = req.user || {}

            const post = await postsService.getPostDetails(id, {
                includeTags: true,
            })
            if (!post) {
                throw new createHttpError.NotFound()
            }
            if (!userCanViewPost(post, userId)) {
                throw new createHttpError.Forbidden()
            }
            return res.json(post)
        } catch (error) {
            next(error)
        }
    },
]

export const getPublishedPosts = [
    validateRequest(getPublishedPostsValidator),
    async (req, res, next) => {
        try {
            const { page, pageSize, sortBy, tags } = req.query

            const { posts, count } = await postsService.getPosts({
                page,
                pageSize,
                sortBy,
                publishedOnly: true,
                tags,
                includeBody: false,
            })

            const responseJson = {
                metadata: {
                    count: count,
                    page: page,
                    pageSize: pageSize,
                    sortBy: sortBy,
                    tags,
                },
                results: posts,
            }

            return res.json(responseJson)
        } catch (error) {
            next(error)
        }
    },
]

export const createPost = [
    checkPermission(PermissionType.CREATE),
    validateRequest(createPostValidator),
    async (req, res, next) => {
        try {
            const { title } = req.body
            const userId = req.user.id

            const createdPost = await postsService.createPost(title, userId)

            return res.status(201).json(createdPost)
        } catch (error) {
            next(error)
        }
    },
]

export const updatePost = [
    checkPermission(PermissionType.UPDATE),
    validateRequest(updatePostValidator),
    async (req, res, next) => {
        try {
            const { title, body, tags } = req.body
            const { id: postId } = req.params
            const { id: userId } = req.user

            const post = await postsService.getPostDetails(postId)
            if (!post) {
                throw new createHttpError.NotFound()
            }

            if (post.authorId !== userId) {
                throw new createHttpError.Forbidden()
            }

            await postsService.updatePost({
                postId,
                title,
                tags,
                body,
            })

            return res.status(204).send()
        } catch (error) {
            next(error)
        }
    },
]

export const deletePost = [
    checkPermission(PermissionType.DELETE),
    validateRequest(deletePostValidator),
    async (req, res, next) => {
        try {
            const { id: postId } = req.params
            const { id: userId } = req.user

            const post = await postsService.getPostDetails(postId)
            if (!post) {
                throw new createHttpError.NotFound()
            }

            if (post.authorId !== userId) {
                throw new createHttpError.Forbidden()
            }

            await postsService.deletePost(postId)

            return res.status(204).send()
        } catch (error) {
            next(error)
        }
    },
]

export const publishPost = [
    checkPermission(PermissionType.UPDATE),
    validateRequest(publishPostValidator),
    async (req, res, next) => {
        try {
            const { id: postId } = req.params
            const { id: userId } = req.user
            const post = await postsService.getPostDetails(postId)
            if (!post) {
                throw new createHttpError.NotFound()
            }
            if (post.authorId !== userId) {
                throw new createHttpError.Forbidden()
            }
            if (post.publishedAt) {
                return res.status(204).send()
            }

            await postsService.publishPost(postId)

            return res.status(204).send()
        } catch (error) {
            next(error)
        }
    },
]

export const getPostComments = [
    validateRequest(getPostCommentsValidator),
    async (req, res, next) => {
        try {
            const { id: postId } = req.params
            const { id: userId } = req.user || {}
            const post = await postsService.getPostDetails(postId, {
                includeComments: true,
            })
            if (!post) {
                throw new createHttpError.NotFound()
            }

            if (!userCanViewPost(post, userId)) {
                throw new createHttpError.Forbidden()
            }

            const { comments } = post

            return res.json({
                metadata: {
                    count: comments.length,
                },
                results: comments,
            })
        } catch (error) {
            next(error)
        }
    },
]

export const createPostComment = [
    validateRequest(createPostCommentValidator),
    async (req, res, next) => {
        try {
            const { id: postId } = req.params
            const { username, body } = req.body
            const { id: userId } = req.user || {}

            const post = await postsService.getPostDetails(postId)
            if (!post) {
                throw new createHttpError.NotFound()
            }

            if (!userCanViewPost(post, userId)) {
                throw new createHttpError.Forbidden()
            }

            const comment = await commentsService.createComment({
                postId,
                body,
                username,
            })

            return res.status(201).json(comment)
        } catch (error) {
            next(error)
        }
    },
]
