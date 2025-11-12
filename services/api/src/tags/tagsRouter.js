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
import { validateRequest } from "../middlewares/validator.js"
import {
    createTagValidator,
    deleteTagValidator,
    editTagValidator,
    getTagValidator,
} from "./tagsValidators.js"
import { checkPermission } from "../middlewares/checkPermission.js"

const router = Router()

router.get("/", validateRequest(getTagValidator), getTags)
router.post(
    "/",
    passport.authenticate(strategies.jwt, { session: false }),
    checkPermission("CREATE"),
    validateRequest(createTagValidator),
    createTag
)
router
    .route("/:id")
    .get(getTag)
    .put(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission("UPDATE"),
        validateRequest(editTagValidator),
        updateTag
    )
    .delete(
        passport.authenticate(strategies.jwt, { session: false }),
        checkPermission("DELETE"),
        validateRequest(deleteTagValidator),
        deleteTag
    )

export default router
