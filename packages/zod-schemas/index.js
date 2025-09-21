import { z } from "zod"

export const commentSchema = z.object({
    id: z.coerce.number().int().min(1),
    username: z.string().trim(),
    body: z.string().trim(),
})

export const postSchema = z.object({
    id: z.coerce.number().int().min(1),
    title: z.string().trim().min(1),
    body: z.string(),
    tags: z.array(z.union([tagSchema.shape.id, tagSchema.shape.slug])),
})

export const tagSchema = z.object({
    id: z.coerce.number().int().min(1),
    name: z.string().max(30),
    slug: z
        .string()
        .max(30)
        .regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/),
})

export const userSchema = z.object({
    id: z.coerce.number().int().min(1),
    name: z.string().trim().min(1).max(32),
    email: z.email(),
    password: z.string(),
})
