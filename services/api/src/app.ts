import "./config/env.js"
import cookieParser from "cookie-parser"
import express from "express"
import helmet from "helmet"
import { pinoHttp } from "pino-http"
import { pino } from "./config/pino.js"
import createHttpError from "http-errors"
import signupRouter from "./signup/signupRouter.js"
import authRouter from "./auth/authRouter.js"
import postsRouter from "./posts/postsRouter.js"
import usersRouter from "./users/usersRouter.js"
import commentsRouter from "./comments/commentsRouter.js"
import tagsRouter from "./tags/tagsRouter.js"
import passport from "./config/passport.js"
import cors from "cors"
import { errorHandler } from "./middlewares/errorHandler.js"

const app: express.Express = express()

app.use(pinoHttp({ logger: pino }))
app.disable("x-powered-by")
app.use(helmet())
app.use(express.json())
app.use(express.urlencoded({ extended: false }))
app.use(cookieParser(process.env.SIGNED_COOKIE_SECRET))
app.use(
    cors({ origin: process.env.CORS_ORIGIN_URL?.split(","), credentials: true })
)
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
app.use(errorHandler)

export default app
