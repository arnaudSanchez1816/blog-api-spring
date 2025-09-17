import "dotenv/config"
import cookieParser from "cookie-parser"
import express from "express"
import helmet from "helmet"
import pinoHttp from "pino-http"
import createHttpError from "http-errors"
import signupRouter from "./signup/signupRouter.js"
import authRouter from "./auth/authRouter.js"
import postsRouter from "./posts/postsRouter.js"
import usersRouter from "./users/usersRouter.js"
import commentsRouter from "./comments/commentsRouter.js"
import tagsRouter from "./tags/tagsRouter.js"
import passport from "./config/passport.js"
import { pino } from "./config/pino.js"
import { ZodError } from "zod"
import cors from "cors"
import { AlreadyExistsError } from "./helpers/errors.js"
import { Prisma } from "@prisma/client"

const app = express()

app.use(pinoHttp())
app.disable("x-powered-by")
app.use(helmet())
app.use(express.json())
app.use(express.urlencoded({ extended: false }))
app.use(cookieParser())
app.use(cors({ origin: process.env.CORS_ORIGIN_URL }))
// Passport
app.use(passport.initialize())

const v1Router = express.Router()
// Not useful right now
// Regular user can't create posts and unsigned users can already create comments
//v1Router.use("/signup", signupRouter)
v1Router.use("/auth", authRouter)
v1Router.use("/posts", postsRouter)
v1Router.use("/users", usersRouter)
v1Router.use("/comments", commentsRouter)
v1Router.use("/tags", tagsRouter)
app.use("/api/v1", v1Router)

// 404 error
// eslint-disable-next-line
app.use((req, res, next) => {
    throw new createHttpError.NotFound()
})

// Error handler
// eslint-disable-next-line
app.use((error, req, res, next) => {
    pino.error(error)
    let errors = error.message

    if (error instanceof AlreadyExistsError) {
        error.status = 400
    }

    if (
        error instanceof Prisma.PrismaClientKnownRequestError &&
        error.code === "P2016"
    ) {
        const { details } = error
        if (details.includes("RecordNotFound")) {
            error.status = 404
            errors = "Not found"
        }
    }

    if (error instanceof ZodError) {
        error = error.issues.map((e) => {
            return {
                path: e.path,
                message: e.message,
            }
        })
        error.status = 400
    }

    return res.status(error.status || 500).json({ errors })
})

export default app
