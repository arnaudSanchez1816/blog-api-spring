import { z } from "zod"
import { userSchema } from "@repo/zod-schemas"

const PASSWORD_VALIDATION_MESSAGE =
    "The password field must be 8-32 characters long, contains one lower case letter, one upper case letter and one number"

const signupValidator = z.object({
    body: z
        .object({
            ...userSchema.omit({
                id: true,
                password: true,
            }).shape,
            password: userSchema.shape.password
                .min(8, PASSWORD_VALIDATION_MESSAGE)
                .max(32, PASSWORD_VALIDATION_MESSAGE)
                .refine(
                    (password) => /[A-Z]/.test(password),
                    PASSWORD_VALIDATION_MESSAGE
                )
                .refine(
                    (password) => /[a-z]/.test(password),
                    PASSWORD_VALIDATION_MESSAGE
                )
                .refine(
                    (password) => /[0-9]/.test(password),
                    PASSWORD_VALIDATION_MESSAGE
                ),
            passwordConfirmation: z.string(),
        })
        .refine((data) => data.password === data.passwordConfirmation, {
            error: "The Password and Password confirmation fields must be identical",
            path: ["passwordConfirmation"],
        }),
})

export { signupValidator }
