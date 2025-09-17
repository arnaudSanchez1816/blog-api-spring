import { z } from "zod"

const tagSchema = z.object({
    id: z.coerce.number().int().min(1),
    name: z.string().max(30),
    slug: z
        .string()
        .max(30)
        .regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/),
})

export const getTagValidator = z.object({
    params: z.object({
        id: z.union([tagSchema.shape.id, tagSchema.shape.slug]),
    }),
})

export const createTagValidator = z.object({
    body: tagSchema.pick({
        name: true,
        slug: true,
    }),
})

export const deleteTagValidator = z.object({
    params: tagSchema.pick({
        id: true,
    }),
})

export const editTagValidator = z.object({
    params: tagSchema.pick({
        id: true,
    }),
    body: tagSchema.pick({
        name: true,
        slug: true,
    }),
})
