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
import passport from "./config/passport.js"
import { pino } from "./config/pino.js"
import { ZodError } from "zod"

const app = express()

app.use(pinoHttp())
app.disable("x-powered-by")
app.use(helmet())
app.use(express.json())
app.use(express.urlencoded({ extended: false }))
app.use(cookieParser())
// Passport
app.use(passport.initialize())

const v1Router = express.Router()
v1Router.use("/signup", signupRouter)
v1Router.use("/auth", authRouter)
v1Router.use("/posts", postsRouter)
v1Router.use("/users", usersRouter)
app.use("/api/v1", v1Router)

// 404 error
// eslint-disable-next-line
app.use((req, res, next) => {
    throw new createHttpError.NotFound()
})

// Error handler
// eslint-disable-next-line
app.use((error, req, res, next) => {
    let errors = error.message
    if (error instanceof ZodError) {
        error = error.issues.map((e) => {
            return {
                path: e.path,
                message: e.message,
            }
        })
        error.status = 400
    }

    pino.error(error)
    return res.status(error.status || 500).json({ errors })
})

export default app
