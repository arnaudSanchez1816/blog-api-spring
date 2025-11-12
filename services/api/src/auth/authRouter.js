import { Router } from "express"
import { getAccessToken, login } from "./authController.js"
import passport from "passport"
import { strategies } from "../config/passport.js"
import { validateRequest } from "../middlewares/validator.js"
import { loginValidator } from "./authValidators.js"

const router = Router()

router.get(
    "/token",
    passport.authenticate(strategies.jwtRefresh, { session: false }),
    getAccessToken
)
router.post(
    "/login",
    validateRequest(loginValidator),
    passport.authenticate(strategies.local, {
        session: false,
        failWithError: true,
    }),
    login
)

export default router
