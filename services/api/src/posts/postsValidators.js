import { SortByValues } from "./postsService.js"
import { z } from "zod"

const postIdSchema = z.coerce.number().int().min(1)

export const getPublishedPostsValidator = z.object({
    query: z.object({
        sortBy: z
            .enum(Object.entries(SortByValues).map(([k, v]) => v))
            .optional(),
        page: z.coerce.number().int().min(0).optional(),
        pageSize: z.coerce.number().int().min(1).max(50).optional(),
    }),
})

export const createPostValidator = z.object({
    body: z.object({
        title: z.string().trim().min(1),
    }),
})

export const updatePostValidator = z.object({
    body: z.object({
        ...createPostValidator.shape.body.shape,
        body: z.string(),
    }),
    params: z.object({
        id: postIdSchema,
    }),
})

export const deletePostValidator = z.object({
    params: z.object({
        id: postIdSchema,
    }),
})

export const getPublishedPostValidator = z.object({
    params: z.object({
        id: postIdSchema,
    }),
})
