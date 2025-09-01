import { Router } from "express"
import passport from "passport"
import { strategies } from "../config/passport.js"
import {
    createUser,
    getCurrentUser,
    getCurrentUserPosts,
} from "./usersController.js"

const router = Router()

// /users/me
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

// /users
router.post(
    "/",
    passport.authenticate(strategies.jwt, { session: false }),
    createUser
)

export default router
