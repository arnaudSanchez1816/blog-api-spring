import { prisma } from "../config/prisma.js"

export const getComment = async (commentId) => {
    const comment = await prisma.comment.findUnique({
        where: {
            id: commentId,
        },
    })

    return comment
}

export const createComment = async ({ postId, username, body }) => {
    const createdComment = await prisma.comment.create({
        data: {
            postId,
            body,
            username,
        },
    })

    return createdComment
}

export const deleteComment = async (commentId) => {
    const deletedComment = await prisma.comment.delete({
        where: {
            id: commentId,
        },
    })

    return deletedComment
}

export const updateComment = async ({ commentId, username, body }) => {
    const updatedComment = await prisma.comment.update({
        where: {
            id: commentId,
        },
        data: {
            username,
            body,
        },
    })

    return updatedComment
}
