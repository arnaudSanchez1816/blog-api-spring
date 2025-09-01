import { z } from "zod"

const commentIdSchema = z.coerce.number().int().min(1)
const commentContentSchema = z.object({
    username: z.string().trim(),
    body: z.string().trim(),
})

export const getCommentValidator = z.object({
    params: z.object({
        id: commentIdSchema,
    }),
})

export const deleteCommentValidator = z.object({
    ...getCommentValidator.shape,
})

export const editCommentValidator = z.object({
    ...getCommentValidator.shape,
    body: commentContentSchema,
})

export const createCommentValidator = z.object({
    body: commentContentSchema,
})
