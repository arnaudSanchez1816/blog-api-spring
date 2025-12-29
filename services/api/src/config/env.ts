import { config } from "dotenv"
import z, { ZodError } from "zod"
import { pino } from "./pino.js"
config({
    path: [`.env.${process.env.NODE_ENV}`, ".env"],
})

try {
    z.parse(
        z.object({
            PORT: z.coerce.number().default(3000),
            NODE_ENV: z.string().default("development"),
            PINO_LOG_LEVEL: z.string().default("debug"),
            PASSWORD_SALT_LENGTH: z.coerce.number().int().min(1),
            DATABASE_URL: z.string(),
            SIGNED_COOKIE_SECRET: z.string(),
            JWT_ACCESS_SECRET: z.string(),
            JWT_REFRESH_SECRET: z.string(),
            CORS_ORIGIN_URL: z.string().optional(),
        }),
        process.env
    )
} catch (error) {
    const zodError = error as ZodError
    pino.error(`Failed to validate env variables!\n${zodError}`)
    throw error
}
