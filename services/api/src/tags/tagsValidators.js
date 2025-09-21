import { z } from "zod"
import { tagSchema } from "@repo/zod-schemas"

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
