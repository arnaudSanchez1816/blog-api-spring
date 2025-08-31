import { loginValidator } from "./authValidators.js"
import passport from "passport"
import { strategies } from "../config/passport.js"
import authService from "./authService.js"
import { validateRequest } from "../middlewares/validator.js"

export const login = [
    validateRequest(loginValidator),
    passport.authenticate(strategies.local, { session: false }),
    async (req, res, next) => {
        try {
            const user = req.user
            const accessToken = await authService.generateAccessToken(user, {
                expiresIn: "1 day",
            })
            return res.json({
                accessToken: accessToken,
            })
        } catch (error) {
            next(error)
        }
    },
]
