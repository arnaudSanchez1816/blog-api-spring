import { Router } from "express"
import {
    createPost,
    getPublishedPosts,
    updatePost,
    deletePost,
} from "./postsController.js"
import passport from "passport"
import { strategies } from "../config/passport.js"

const router = Router()

router
    .route("/")
    .get(getPublishedPosts)
    .post(passport.authenticate(strategies.jwt, { session: false }), createPost)
    .put(passport.authenticate(strategies.jwt, { session: false }), updatePost)
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        deletePost
    )
router.get("/", getPublishedPosts)

export default router
