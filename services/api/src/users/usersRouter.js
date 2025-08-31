import { Router } from "express"
import passport from "passport"
import { strategies } from "../config/passport.js"
import { getCurrentUser, getCurrentUserPosts } from "./usersController.js"

const router = Router()

const meRouter = Router()
meRouter.get("/", getCurrentUser)
meRouter.get("/posts", getCurrentUserPosts)

router.use(
    "/me",
    passport.authenticate(strategies.jwt, {
        session: false,
    }),
    meRouter
)

export default router
