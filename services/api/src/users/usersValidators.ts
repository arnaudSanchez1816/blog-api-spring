import z from "zod"
import { signupValidator } from "../signup/signupValidators.js"
import { prisma } from "../config/prisma.js"
import { getPostsValidator } from "../posts/postsValidators.js"

export const createUserValidator = z.object({
    body: signupValidator.shape.body.safeExtend({
        role: z
            .string()
            .trim()
            .lowercase()
            .nonempty()
            .max(20)
            .refine(async (value) => {
                // Check role exists in database
                const role = await prisma.role.findUnique({
                    where: {
                        name: value,
                    },
                })
                return !!role
            }),
    }),
})

export const getCurrentUserPostsValidator = getPostsValidator.extend({})
