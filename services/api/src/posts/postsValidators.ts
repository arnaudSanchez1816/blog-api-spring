import { createCommentValidator } from "../comments/commentsValidators.js"
import { SortByValues } from "./postsService.js"
import { z } from "zod"
import { postSchema } from "@repo/zod-schemas"

export const getPostsValidator = z.object({
    query: z.object({
        q: z.string().optional(),
        sortBy: z
            .enum(Object.entries(SortByValues).map(([k, v]) => v))
            .optional(),
        page: z.coerce.number().int().min(0).optional(),
        pageSize: z.coerce.number().int().min(1).max(50).optional(),
        tags: z
            .preprocess((val) => {
                if (typeof val === "string") {
                    return val.split(",")
                }
                return val
            }, postSchema.shape.tags)
            .optional(),
        unpublished: z.transform(() => true).optional(),
    }),
})

export const createPostValidator = z.object({
    body: postSchema.pick({
        title: true,
    }),
})

export const updatePostValidator = z.object({
    body: postSchema
        .omit({
            id: true,
        })
        .partial()
        .refine(
            ({ title, body, tags }) =>
                title !== undefined || body !== undefined || tags !== undefined,
            { error: "At least one field must be provided." }
        ),
    params: z.object({
        id: postSchema.shape.id,
    }),
})

export const deletePostValidator = z.object({
    params: postSchema.pick({
        id: true,
    }),
})

export const publishPostValidator = z.object({
    params: postSchema.pick({
        id: true,
    }),
})

export const hidePostValidator = z.object({
    params: postSchema.pick({
        id: true,
    }),
})

export const getPublishedPostValidator = z.object({
    params: postSchema.pick({ id: true }),
})

export const getPostCommentsValidator = z.object({
    params: postSchema.pick({ id: true }),
})

export const createPostCommentValidator = z.object({
    params: postSchema.pick({ id: true }),
    ...createCommentValidator.shape,
})
