import { Router } from "express"
import passport from "passport"
import { strategies } from "../config/passport.js"
import {
    createUser,
    getCurrentUser,
    getCurrentUserPosts,
} from "./usersController.js"
import { validateRequest } from "../middlewares/validator.js"
import {
    createUserValidator,
    getCurrentUserPostsValidator,
} from "./usersValidators.js"
import { checkPermission } from "../middlewares/checkPermission.js"
import { PermissionType } from "@prisma/client"

const router: Router = Router()

// /users/me
const meRouter = Router()
meRouter.get("/", getCurrentUser)
meRouter.get(
    "/posts",
    validateRequest(getCurrentUserPostsValidator),
    getCurrentUserPosts
)

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
    checkPermission(PermissionType.CREATE),
    validateRequest(createUserValidator),
    createUser
)

export default router
