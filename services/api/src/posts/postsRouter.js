import { Router } from "express"
import {
    createPost,
    getPublishedPosts,
    updatePost,
    deletePost,
    getPost,
} from "./postsController.js"
import passport from "passport"
import { strategies } from "../config/passport.js"

const router = Router()

router
    .route("/")
    .get(getPublishedPosts)
    .post(passport.authenticate(strategies.jwt, { session: false }), createPost)

router
    .route("/:id")
    .get(
        passport.authenticate(strategies.jwt, {
            session: false,
            failWithError: false,
        }),
        getPost
    )
    .put(passport.authenticate(strategies.jwt, { session: false }), updatePost)
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        deletePost
    )

export default router
