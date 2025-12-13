import { z } from "zod"
import { userSchema } from "@repo/zod-schemas"

const loginValidator = z.object({
    body: userSchema.pick({
        email: true,
        password: true,
    }),
})

export { loginValidator }
