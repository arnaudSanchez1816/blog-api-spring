import {
    createPostValidator,
    deletePostValidator,
    getPublishedPostsValidator,
    updatePostValidator,
} from "./postsValidators.js"
import postsService from "./postsService.js"
import { validateRequest } from "../middlewares/validator.js"
import createHttpError from "http-errors"

export const getPublishedPosts = [
    validateRequest(getPublishedPostsValidator),
    async (req, res, next) => {
        try {
            const { page, pageSize, sortBy } = req.query
            const posts = await postsService.getPublishedPosts({
                page,
                pageSize,
                sortBy,
            })

            const responseJson = {
                metadata: {
                    count: posts.length,
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

            const updatedPost = await postsService.updatePost({
                postId,
                title,
                body,
            })

            return res.status(200).json(updatedPost)
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

            const deletedPost = await postsService.deletePost(postId)

            return res.status(200).json(deletedPost)
        } catch (error) {
            next(error)
        }
    },
]
