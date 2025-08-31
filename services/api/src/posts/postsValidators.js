import { SortByValues } from "./postsService.js"
import { z } from "zod"

export const getPublishedPostsValidator = z.object({
    query: z.object({
        sortBy: z
            .enum(Object.entries(SortByValues).map(([k, v]) => v))
            .optional(),
        page: z.coerce.number().int().optional(),
        pageSize: z.coerce.number().int().optional(),
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
        id: z.coerce.number().int().min(),
    }),
})

export const deletePostValidator = z.object({
    params: z.object({
        id: z.coerce.number().int().min(),
    }),
})
