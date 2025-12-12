import type z from "zod"
import * as commentsService from "./commentsService.js"
import createHttpError from "http-errors"
import type {
    deleteCommentValidator,
    editCommentValidator,
    getCommentValidator,
} from "./commentsValidators.js"
import type { Request, Response, NextFunction } from "express"

type GetCommentSchema = z.infer<typeof getCommentValidator>
export const getComment = async (
    req: Request<GetCommentSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id: commentId } = req.params
        const comment = await commentsService.getComment(commentId)
        if (!comment) {
            throw new createHttpError.NotFound()
        }

        return res.status(200).json(comment)
    } catch (error) {
        next(error)
    }
}

type DeleteCommentSchema = z.infer<typeof deleteCommentValidator>
export const deleteComment = async (
    req: Request<DeleteCommentSchema["params"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id: commentId } = req.params
        const comment = await commentsService.getComment(commentId)
        if (!comment) {
            throw new createHttpError.NotFound()
        }

        const deletedComment = await commentsService.deleteComment(commentId)

        return res.status(200).json(deletedComment)
    } catch (error) {
        next(error)
    }
}

type EditCommentSchema = z.infer<typeof editCommentValidator>
export const editComment = async (
    req: Request<EditCommentSchema["params"], any, EditCommentSchema["body"]>,
    res: Response,
    next: NextFunction
) => {
    try {
        const { id } = req.params
        const { username, body } = req.body

        const comment = await commentsService.getComment(id)
        if (!comment) {
            throw new createHttpError.NotFound()
        }

        const updatedComment = await commentsService.updateComment({
            id,
            username,
            body,
        })

        return res.status(200).json(updatedComment)
    } catch (error) {
        next(error)
    }
}
