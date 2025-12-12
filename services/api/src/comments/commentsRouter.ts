import { Router } from "express"
import { deleteComment, editComment, getComment } from "./commentsController.js"
import passport from "passport"
import { strategies } from "../config/passport.js"
import { validateRequest } from "../middlewares/validator.js"
import {
    deleteCommentValidator,
    editCommentValidator,
    getCommentValidator,
} from "./commentsValidators.js"
import { checkPermission } from "../middlewares/checkPermission.js"
import { PermissionType } from "@prisma/client"

const router: Router = Router()
router
    .route("/:id")
    .all(passport.authenticate(strategies.jwt, { session: false }))
    .get(validateRequest(getCommentValidator), getComment)
    .put(
        checkPermission(PermissionType.UPDATE),
        validateRequest(editCommentValidator),
        editComment
    )
    .delete(
        checkPermission(PermissionType.DELETE),
        validateRequest(deleteCommentValidator),
        deleteComment
    )
export default router
