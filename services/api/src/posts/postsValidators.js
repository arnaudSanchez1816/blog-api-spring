import { createCommentValidator } from "../comments/commentsValidators.js"
import { tagSchema } from "../tags/tagsValidators.js"
import { SortByValues } from "./postsService.js"
import { z } from "zod"

const postSchema = z.object({
    id: z.coerce.number().int().min(1),
    title: z.string().trim().min(1),
    body: z.string(),
    tags: z
        .string()
        .transform((val) => val.split(","))
        .pipe(z.array(z.union([tagSchema.shape.id, tagSchema.shape.slug]))),
})

export const getPublishedPostsValidator = z.object({
    query: z.object({
        sortBy: z
            .enum(Object.entries(SortByValues).map(([k, v]) => v))
            .optional()
            .default(SortByValues.publishedAtDesc),
        page: z.coerce.number().int().min(0).optional().default(1),
        pageSize: z.coerce.number().int().min(1).max(50).optional().default(20),
        tags: postSchema.shape.tags.optional().default([]),
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
        .partial(),
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
