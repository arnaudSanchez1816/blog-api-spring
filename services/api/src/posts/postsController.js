import {
    createPostValidator,
    deletePostValidator,
    getPublishedPostsValidator,
    getPublishedPostValidator,
    updatePostValidator,
} from "./postsValidators.js"
import postsService from "./postsService.js"
import { validateRequest } from "../middlewares/validator.js"
import createHttpError from "http-errors"

export const getPost = [
    validateRequest(getPublishedPostValidator),
    async (req, res, next) => {
        try {
            const { id } = req.params
            const { id: userId } = req.user

            const post = await postsService.getPostDetails(id)
            if (!post) {
                throw new createHttpError.NotFound()
            }
            if (!post.publishedAt) {
                if (!userId || post.authorId !== userId) {
                    throw new createHttpError.Forbidden()
                }
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
            const { page, pageSize, sortBy } = req.query

            const { posts, count } = await postsService.getPosts({
                page,
                pageSize,
                sortBy,
                publishedOnly: true,
            })

            const responseJson = {
                metadata: {
                    count: count,
                    page: page,
                    pageSize: pageSize,
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
    validateRequest(updatePostValidator),
    async (req, res, next) => {
        try {
            const { title, body } = req.body
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
                body,
            })

            return res.status(204).send()
        } catch (error) {
            next(error)
        }
    },
]

export const deletePost = [
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
