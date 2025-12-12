import { z } from "zod"
import { commentSchema } from "@repo/zod-schemas"

export const getCommentValidator = z.object({
    params: commentSchema.pick({ id: true }),
})

export const deleteCommentValidator = z.object({
    params: commentSchema.pick({ id: true }),
})

export const editCommentValidator = z.object({
    params: commentSchema.pick({ id: true }),
    body: commentSchema.omit({
        id: true,
    }),
})

export const createCommentValidator = z.object({
    body: commentSchema.omit({
        id: true,
    }),
})
