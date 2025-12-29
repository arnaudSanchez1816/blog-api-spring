import type { Prisma } from "@prisma/client"
import { prisma } from "../config/prisma.js"

export const getComment = async (commentId: number) => {
    const comment = await prisma.comment.findUnique({
        where: {
            id: commentId,
        },
    })

    return comment
}

type CreateCommentDto = Prisma.CommentGetPayload<{
    select: {
        postId: true
        username: true
        body: true
    }
}>

export const createComment = async ({
    postId,
    username,
    body,
}: CreateCommentDto) => {
    const createdComment = await prisma.comment.create({
        data: {
            postId,
            body,
            username,
        },
    })

    return createdComment
}

export const deleteComment = async (commentId: number) => {
    const deletedComment = await prisma.comment.delete({
        where: {
            id: commentId,
        },
    })

    return deletedComment
}

type UpdateCommentDto = Prisma.CommentGetPayload<{
    select: {
        id: true
        username: true
        body: true
    }
}>

export const updateComment = async ({
    id,
    username,
    body,
}: UpdateCommentDto) => {
    const updatedComment = await prisma.comment.update({
        where: {
            id,
        },
        data: {
            username,
            body,
        },
    })

    return updatedComment
}
