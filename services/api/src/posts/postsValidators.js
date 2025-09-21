import { createCommentValidator } from "../comments/commentsValidators.js"
import { SortByValues } from "./postsService.js"
import { z } from "zod"
import { postSchema } from "@repo/zod-schemas"

export const getPublishedPostsValidator = z.object({
    query: z.object({
        q: z.string().optional(),
        sortBy: z
            .enum(Object.entries(SortByValues).map(([k, v]) => v))
            .optional()
            .default(SortByValues.publishedAtDesc)
            .catch(SortByValues.publishedAtDesc),
        page: z.coerce.number().int().min(0).optional().default(1).catch(1),
        pageSize: z.coerce
            .number()
            .int()
            .min(1)
            .max(50)
            .optional()
            .default(20)
            .catch(20),
        tags: z
            .string()
            .transform((val) => val.split(","))
            .pipe(postSchema.shape.tags)
            .optional()
            .default([])
            .catch([]),
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
