import { Router } from "express"
import {
    createPost,
    getPublishedPosts,
    updatePost,
    deletePost,
    getPost,
    getPostComments,
    createPostComment,
    publishPost,
    hidePost,
} from "./postsController.js"
import passport from "passport"
import { strategies } from "../config/passport.js"

const router = Router()
const idRouter = Router({ mergeParams: true })

// /posts/:id/hide
idRouter
    .route("/hide")
    .put(passport.authenticate(strategies.jwt, { session: false }), hidePost)

// /posts/:id/publish
idRouter
    .route("/publish")
    .put(passport.authenticate(strategies.jwt, { session: false }), publishPost)

// /posts/:id/comments
idRouter
    .route("/comments")
    .all(
        passport.authenticate([strategies.jwt, strategies.anonymous], {
            session: false,
        })
    )
    .get(getPostComments)
    .post(createPostComment)

// /posts/:id
idRouter
    .route("/")
    .get(
        passport.authenticate([strategies.jwt, strategies.anonymous], {
            session: false,
        }),
        getPost
    )
    .put(passport.authenticate(strategies.jwt, { session: false }), updatePost)
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        deletePost
    )

router.use("/:id", idRouter)

// /posts
router
    .route("/")
    .get(
        passport.authenticate([strategies.jwt, strategies.anonymous], {
            session: false,
        }),
        getPublishedPosts
    )
    .post(passport.authenticate(strategies.jwt, { session: false }), createPost)

export default router
