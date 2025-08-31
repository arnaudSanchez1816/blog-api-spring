import { z } from "zod"

const PASSWORD_VALIDATION_MESSAGE =
    "The password field must be 8-32 characters long, contains one lower case letter, one upper case letter and one number"

const passwordSchema = z
    .string()
    .min(8, { error: PASSWORD_VALIDATION_MESSAGE })
    .max(20, { error: PASSWORD_VALIDATION_MESSAGE })
    .refine((password) => /[A-Z]/.test(password), {
        error: PASSWORD_VALIDATION_MESSAGE,
    })
    .refine((password) => /[a-z]/.test(password), {
        error: PASSWORD_VALIDATION_MESSAGE,
    })
    .refine((password) => /[0-9]/.test(password), {
        error: PASSWORD_VALIDATION_MESSAGE,
    })

const signupValidator = z.object({
    body: z
        .object({
            name: z.string().trim().min(1).max(32),
            email: z.email(),
            password: passwordSchema,
            passwordConfirmation: z.string(),
        })
        .refine((data) => data.password === data.passwordConfirmation, {
            error: "The Password and Password confirmation fields must be identical",
            path: ["passwordConfirmation"],
        }),
})

export { signupValidator }
