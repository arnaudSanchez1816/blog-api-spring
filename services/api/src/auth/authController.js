import { loginValidator } from "./authValidators.js"
import passport from "passport"
import { REFRESH_TOKEN_COOKIE, strategies } from "../config/passport.js"
import authService from "./authService.js"
import { validateRequest } from "../middlewares/validator.js"
import { AuthenticationError } from "../helpers/errors.js"
import createHttpError from "http-errors"
import _ from "lodash"

const COOKIE_REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60 * 1000

export const login = [
    validateRequest(loginValidator),
    passport.authenticate(strategies.local, {
        session: false,
        failWithError: true,
    }),
    async (req, res, next) => {
        try {
            const user = req.user
            const generateAccessToken = authService.generateAccessToken(user, {
                expiresIn: "1 day",
            })
            const generateRefreshToken = authService.generateRefreshToken(
                user,
                { expiresIn: "30 days" }
            )
            const [accessToken, refreshToken] = await Promise.all([
                generateAccessToken,
                generateRefreshToken,
            ])

            res.cookie(REFRESH_TOKEN_COOKIE, refreshToken, {
                maxAge: COOKIE_REFRESH_TOKEN_MAX_AGE,
                httpOnly: true,
                secure: true,
                signed: true,
            })

            return res.json({
                user: _.omit(user, "password"),
                accessToken: accessToken,
            })
        } catch (error) {
            next(error)
        }
    },

    (error, req, res, next) => {
        if (error instanceof AuthenticationError) {
            return next(createHttpError.Unauthorized(error.message))
        }
        next(error)
    },
]

export const getAccessToken = async (req, res, next) => {
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
}
