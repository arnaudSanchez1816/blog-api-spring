import { z } from "zod"

const loginValidator = z.object({
    body: z.object({
        email: z.email(),
        password: z.string(),
    }),
})

export { loginValidator }
