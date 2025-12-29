import * as postsService from "./postsService.js"
import * as commentsService from "../comments/commentsService.js"
import createHttpError from "http-errors"
import type { Request, Response, NextFunction } from "express"
import type z from "zod"
import type {
    createPostCommentValidator,
    createPostValidator,
    deletePostValidator,
    getPostCommentsValidator,
    getPostsValidator,
    getPublishedPostValidator,
    hidePostValidator,
    publishPostValidator,
    updatePostValidator,
} from "./postsValidators.js"
import type { ApiUser } from "../types/apiUser.js"

type GetPostSchema = z.infer<typeof getPublishedPostValidator>
export const getPost = async (
    req: Request<GetPostSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id } = req.params
        const { id: userId } = (req.user as ApiUser) || {}

        const post = await postsService.getPostDetails(id, {
            includeTags: true,
        })
        if (!post) {
            throw new createHttpError.NotFound()
        }
        if (!postsService.userCanViewPost(post, userId)) {
            throw new createHttpError.Forbidden()
        }
        return res.status(200).json(post)
    } catch (error) {
        next(error)
    }
}

type GetPostsSchema = z.infer<typeof getPostsValidator>
export const getPosts = async (
    req: Request<any, any, any, GetPostsSchema["query"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { q, page, pageSize, sortBy, tags, unpublished } = req.query
        const { id: userId } = (req.user as ApiUser) || {}
        const publishedOnly = userId ? !unpublished : true

        const { posts, count } = await postsService.getPosts({
            q,
            page,
            pageSize,
            sortBy,
            publishedOnly: publishedOnly,
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

        return res.status(200).json(responseJson)
    } catch (error) {
        next(error)
    }
}

type CreatePostSchema = z.infer<typeof createPostValidator>
export const createPost = async (
    req: Request<any, any, CreatePostSchema["body"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const user = req.user as ApiUser
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const { title } = req.body
        const { id: userId } = user

        const createdPost = await postsService.createPost(title, userId)

        return res.status(201).json(createdPost)
    } catch (error) {
        next(error)
    }
}

type UpdatePostSchema = z.infer<typeof updatePostValidator>
export const updatePost = async (
    req: Request<UpdatePostSchema["params"], any, UpdatePostSchema["body"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const user = req.user as ApiUser
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const { title, body, tags } = req.body
        const { id: postId } = req.params
        const { id: userId } = user

        const post = await postsService.getPostDetails(postId)
        if (!post) {
            throw new createHttpError.NotFound()
        }

        if (post.author.id !== userId) {
            throw new createHttpError.Forbidden()
        }

        const updatedPost = await postsService.updatePost({
            id: postId,
            title,
            tags,
            body,
        })

        return res.status(200).json(updatedPost)
    } catch (error) {
        next(error)
    }
}
type DeletePostSchema = z.infer<typeof deletePostValidator>
export const deletePost = async (
    req: Request<DeletePostSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const user = req.user as ApiUser
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const { id: postId } = req.params
        const { id: userId } = user

        const post = await postsService.getPostDetails(postId, {
            includeComments: true,
            includeTags: true,
        })
        if (!post) {
            throw new createHttpError.NotFound()
        }

        if (post.author.id !== userId) {
            throw new createHttpError.Forbidden()
        }

        const deletedPost = await postsService.deletePost(postId)

        return res.status(200).json(deletedPost)
    } catch (error) {
        next(error)
    }
}

type PublishPostSchema = z.infer<typeof publishPostValidator>
export const publishPost = async (
    req: Request<PublishPostSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const user = req.user as ApiUser
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const { id: postId } = req.params
        const { id: userId } = user

        const post = await postsService.getPostDetails(postId)
        if (!post) {
            throw new createHttpError.NotFound()
        }
        if (post.author.id !== userId) {
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
}

type HidePostSchema = z.infer<typeof hidePostValidator>
export const hidePost = async (
    req: Request<HidePostSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const user = req.user as ApiUser
        if (!user) {
            throw new createHttpError.Unauthorized()
        }

        const { id: postId } = req.params
        const { id: userId } = user

        const post = await postsService.getPostDetails(postId)
        if (!post) {
            throw new createHttpError.NotFound()
        }
        if (post.author.id !== userId) {
            throw new createHttpError.Forbidden()
        }
        if (!post.publishedAt) {
            return res.status(204).send()
        }

        await postsService.hidePost(postId)

        return res.status(204).send()
    } catch (error) {
        next(error)
    }
}

type GetPostCommentsSchema = z.infer<typeof getPostCommentsValidator>
export const getPostComments = async (
    req: Request<GetPostCommentsSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id: postId } = req.params
        const { id: userId } = (req.user as ApiUser) || {}

        const post = await postsService.getPostDetails(postId, {
            includeComments: true,
        })
        if (!post) {
            throw new createHttpError.NotFound()
        }

        if (!postsService.userCanViewPost(post, userId)) {
            throw new createHttpError.Forbidden()
        }

        const { comments } = post

        return res.status(200).json({
            metadata: {
                count: comments.length,
            },
            results: comments,
        })
    } catch (error) {
        next(error)
    }
}

type CreatePostCommentSchema = z.infer<typeof createPostCommentValidator>
export const createPostComment = async (
    req: Request<
        CreatePostCommentSchema["params"],
        any,
        CreatePostCommentSchema["body"]
    >,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id: postId } = req.params
        const { username, body } = req.body
        const { id: userId } = (req.user as ApiUser) || {}

        const post = await postsService.getPostDetails(postId)
        if (!post) {
            throw new createHttpError.NotFound()
        }

        if (!postsService.userCanViewPost(post, userId)) {
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
}
