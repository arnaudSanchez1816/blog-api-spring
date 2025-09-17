import { Router } from "express"
import passport from "passport"
import { strategies } from "../config/passport.js"
import {
    createTag,
    deleteTag,
    getTag,
    getTags,
    updateTag,
} from "./tagsController.js"

const router = Router()

router.get("/", getTags)
router.post(
    "/",
    passport.authenticate(strategies.jwt, { session: false }),
    createTag
)
router
    .route("/:id")
    .get(getTag)
    .put(passport.authenticate(strategies.jwt, { session: false }), updateTag)
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        deleteTag
    )

export default router
