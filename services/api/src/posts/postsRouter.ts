import { Router } from "express"
import {
    createPost,
    getPosts,
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
import { validateRequest } from "../middlewares/validator.js"
import {
    createPostCommentValidator,
    createPostValidator,
    deletePostValidator,
    getPostCommentsValidator,
    getPostsValidator,
    getPublishedPostValidator,
    hidePostValidator,
    publishPostValidator,
    updatePostValidator,
} from "./postsValidators.js"
import { checkPermission } from "../middlewares/checkPermission.js"
import { PermissionType } from "@prisma/client"

const router: Router = Router()
const idRouter = Router({ mergeParams: true })

// /posts/:id/hide
idRouter
    .route("/hide")
    .put(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission(PermissionType.UPDATE),
        validateRequest(hidePostValidator),
        hidePost
    )

// /posts/:id/publish
idRouter
    .route("/publish")
    .put(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission(PermissionType.UPDATE),
        validateRequest(publishPostValidator),
        publishPost
    )

// /posts/:id/comments
idRouter
    .route("/comments")
    .all(
        passport.authenticate([strategies.jwt, strategies.anonymous], {
            session: false,
        })
    )
    .get(validateRequest(getPostCommentsValidator), getPostComments)
    .post(validateRequest(createPostCommentValidator), createPostComment)

// /posts/:id
idRouter
    .route("/")
    .get(
        passport.authenticate([strategies.jwt, strategies.anonymous], {
            session: false,
        }),
        validateRequest(getPublishedPostValidator),
        getPost
    )
    .put(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission(PermissionType.UPDATE),
        validateRequest(updatePostValidator),
        updatePost
    )
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission(PermissionType.DELETE),
        validateRequest(deletePostValidator),
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
        validateRequest(getPostsValidator),
        getPosts
    )
    .post(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission(PermissionType.CREATE),
        validateRequest(createPostValidator),
        createPost
    )

export default router
